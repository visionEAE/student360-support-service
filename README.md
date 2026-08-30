# student360-support-service

All new functionality of Student 360° (port **8084**, schema **`support`**): wellbeing entries,
the risk rule, alerts, intervention plans and advisor reports. It is the **orchestrating**
service: to decide whether an entry is a risk situation it calls `core-service` and
`lms-service` synchronously and composes a decision from all three sources.

| Method | Path | Roles (gateway) | Audit action |
|---|---|---|---|
| `POST` | `/api/support/students/{id}/wellbeing-entries` | `STUDENT` (self), `ADVISOR` | `RECORD_WELLBEING_ENTRY` |
| `GET` | `/api/support/advisors/me/alerts` | `ADVISOR`, `ADMIN` | `LIST_ALERT_INBOX` |
| `GET` | `/api/support/advisors/me/alerts/{id}` | assigned `ADVISOR`, `ADMIN` | `READ_ALERT_DETAIL` (basis `ASSIGNMENT`) |
| `POST` | `/api/support/advisors/me/alerts/{id}/reports` | assigned `ADVISOR`, `ADMIN` | `CREATE_SUPPORT_REPORT` |

## The rule (`ConvergentRiskRule`)

One explainable rule instead of five opaque ones:

```
LOW_WELLBEING   = level ≤ 2
DISENGAGED      = daysSinceLastAccess > 14  OR  onTimeSubmissionRate < 0.6      (from lms-service /signals)
OVERDUE_BALANCE = overdueBalance > 0                                            (from core-service /financial-status)

HIGH   → INTEGRAL_SUPPORT     when LOW_WELLBEING and DISENGAGED and OVERDUE_BALANCE
MEDIUM → ACADEMIC_FOLLOW_UP   when LOW_WELLBEING and exactly one of the other two
```

Every alert stores **`triggering_signals`** (the values seen and the conditions that fired). A
source that is down does not reject the entry: the rule evaluates in degraded mode and records
`unavailableSources`. Thresholds are configuration (`student360.support.rule.*`).

## What each outbound call carries

`DownstreamRequestInterceptor`: a service token for the target audience (`ServiceTokenProvider`
from `student360-common`), the user identity (`X-User-*`) so the target applies its own
fine-grained rule and audits the real actor, and `X-Request-Id` so one request's audit trail
spans every service it touched. `feign-micrometer` propagates the W3C `traceparent`.

## Privacy

Wellbeing entries are stored under an **HMAC-SHA256 pseudonym** of the student id computed with
a secret only this service holds (`HmacPseudonymizer`): deterministic, one-way, never reversed.
The free-text comment never appears in a log line or an outbox payload.

## Outbox

`WELLBEING_ENTRY_RECORDED`, `ALERT_GENERATED`, `INTERVENTION_PLAN_CREATED` are written to
`support.outbox_event` in the same transaction as the business change, with the full envelope a
Pub/Sub subscriber would receive (`eventId`, `eventType`, `aggregateType/Id`, `occurredAt`,
`requestId`, `traceId`, `data`). Stage 2 adds a relay that sets `published_at`.

## Seed

`A-2001` → `S-1003` and `S-1001` (active); `A-2002` → `S-1002` (active) and `S-1003`
(**expired** — does not authorize). Negative scenario A is `A-2002` opening `S-1003`'s alert.

## Run · Verify

```bash
cd ../student360-infra && make up && make build-common && make run-support-service
mvn verify   # rule unit tests + Testcontainers/MockWebServer flow tests = phase gate 5
```
