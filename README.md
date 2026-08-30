# student360-support-service

All new functionality of Student 360° (port **8084**, schema **`support`**): wellbeing entries,
the convergent risk rule, alerts, intervention plans, advisor reports and support requests. It is
the **orchestrating** service: to decide whether a wellbeing entry is a risk situation it calls
`core-service` and `lms-service` synchronously and composes a decision from all three sources.

## CQRS

Every use case is either a **command** (`application/command`, a write, emits an outbox event,
audited `STATE_CHANGE`) or a **query** (`application/query`, a read, audited `DATA_ACCESS`). A
controller only builds the command/query and hands it to its handler; handlers depend on
`domain/port` interfaces, never on JPA, Feign or Spring MVC directly — swapping an adapter never
touches a handler. Read-only handlers return records from `application/query/model`, shaped for
the API, not JPA entities.

## Endpoints (see `docs/api-contract-v2.md` in `student360-infra` for full payloads)

| Method | Path | Handler |
|---|---|---|
| `POST` `PUT` | `/students/{id}/wellbeing-entries[/{entryId}]` | `RecordWellbeingEntryCommand` |
| `GET` | `/students/{id}/wellbeing-entries/draft` | `GetWellbeingDraftQuery` |
| `GET` | `/students/{id}/wellbeing-summary` | `GetWellbeingSummaryQuery` |
| `GET` | `/students/{id}/case` | `GetStudentCaseQuery` (a student's own "vista 360°") |
| `GET` | `/advisors/me/students` | `GetAdvisorStudentsOverviewQuery` ("Mis estudiantes") |
| `GET` | `/advisors/me/students/{id}` | `GetStudentCaseQuery` |
| `POST` | `/advisors/me/students/{id}/intervention-plans` | `CreateInterventionPlanCommand` |
| `POST` | `/advisors/me/students/{id}/alerts` | `CreateManualAlertCommand` |
| `POST` | `/advisors/me/students/{id}/requests` | `CreateSupportRequestCommand` |
| `GET` `PATCH` | `/advisors/me/alerts[/{id}]` | inbox / `UpdateAlertStatusCommand` |
| `POST` | `/advisors/me/alerts/{id}/reports` | `AddSupportReportCommand` |
| `GET` `PATCH` | `/advisors/me/intervention-plans[/{id}]` | list / `UpdateInterventionPlanStatusCommand` |
| `GET` | `/advisors/me/reports` | `GetSupportReportsQuery` |
| `GET` `PATCH` | `/advisors/me/requests[/{id}]` | list / `UpdateSupportRequestStatusCommand` |

## The rule (`ConvergentRiskRule`) — unchanged reasoning, richer inputs

```
LOW_WELLBEING   = min(mood across the 3 dimensions) ≤ 2      (only a SENT entry is evaluated)
DISENGAGED      = daysSinceLastAccess > 14  OR  onTimeSubmissionRate < 0.6   (lms-service /signals)
OVERDUE_BALANCE = overdueBalance > 0                                        (core-service /financial-status)

HIGH   → INTEGRAL_SUPPORT     when LOW_WELLBEING and DISENGAGED and OVERDUE_BALANCE
MEDIUM → ACADEMIC_FOLLOW_UP   when LOW_WELLBEING and exactly one of the other two
```

An advisor may also raise an alert by judgement (`Alert.raisedBy`, source `ADVISOR`,
`triggeringSignals.firedConditions = ["ADVISOR_JUDGEMENT"]`, the reason kept alongside). Every
alert stores what it saw; a source that is down degrades the evaluation and is listed in
`unavailableSources` rather than blocking the entry.

## Wellbeing entries: three dimensions, drafts

An entry has `ECONOMIC`, `ACADEMIC`, `EMOTIONAL` dimensions, each with a `mood` (`DIFFICULT` 1 ..
`VERY_GOOD` 4), a set of `needs` codes and an optional note. A `DRAFT` may hold any subset of
dimensions and is never evaluated; sending (`status: SENT`) requires all three and runs the rule.
Editing an already-sent entry is rejected (`409`) — it is part of the record.

## Authorization

* `StudentCaseAccessPolicy` — who may **read** a student's support information: the student
  (`SELF`), an actively assigned advisor (`ASSIGNMENT`), or an admin (`ADMIN_ROLE`).
* Wellbeing entries are **written** only by the student themself (`assertIsSelf`) — nobody records
  a feeling on someone else's behalf.
* `AssignmentAccessPolicy` — every advisor **write** (plans, manual alerts, requests, reports,
  status changes) requires an active assignment to the student, recorded as `ASSIGNMENT`.

## Privacy

Wellbeing entries are stored under an **HMAC-SHA256 pseudonym** of the student id
(`HmacPseudonymizer`): deterministic, one-way, never reversed. Free-text notes never appear in a
log line or an outbox payload.

## Outbox → data warehouse

`WELLBEING_ENTRY_RECORDED`, `ALERT_GENERATED`, `ALERT_STATUS_CHANGED`, `INTERVENTION_PLAN_CREATED`,
`INTERVENTION_PLAN_UPDATED`, `SUPPORT_REPORT_ADDED`, `SUPPORT_REQUEST_CREATED`,
`SUPPORT_REQUEST_UPDATED` are written to `support.outbox_event` in the same transaction as the
business change, with the full envelope a Pub/Sub subscriber would receive. Stage 2 adds a relay
that sets `published_at` and a BigQuery subscription.

## Seed

`A-2001` → `S-1001`, `S-1003`, `S-1004`, `S-1005` (active); `A-2002` → `S-1002`, `S-1006` (active)
and `S-1003` (**expired** — does not authorize). Negative scenario A is `A-2002` opening `S-1003`.

## Run · Verify

```bash
cd ../student360-infra && make up && make build-common && make run-support-service
mvn verify   # rule unit tests + Testcontainers/MockWebServer flow tests
```
