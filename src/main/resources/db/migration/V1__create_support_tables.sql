-- support schema: everything new in Student 360. Owned by support_user, migrated only here.

-- Wellbeing entries are stored under a pseudonym: the mapping student -> pseudonym is an HMAC
-- computed with a secret that never leaves this service. Even a full dump of this table does not
-- say who felt low.
CREATE TABLE support.wellbeing_entry (
    id                 UUID PRIMARY KEY,
    student_pseudonym  TEXT        NOT NULL,
    level              SMALLINT    NOT NULL,       -- 1 (very low) .. 5 (very good)
    comment            TEXT,                       -- free text; never logged
    recorded_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_wellbeing_entry_level CHECK (level BETWEEN 1 AND 5)
);

CREATE INDEX idx_wellbeing_entry_pseudonym ON support.wellbeing_entry (student_pseudonym, recorded_at DESC);

-- The relationship that authorizes an advisor to see a student. valid_to NULL = open-ended.
CREATE TABLE support.advisor_assignment (
    id                 SERIAL PRIMARY KEY,
    advisor_reference  TEXT NOT NULL,
    student_reference  TEXT NOT NULL,
    valid_from         DATE NOT NULL,
    valid_to           DATE,
    CONSTRAINT chk_advisor_assignment_validity CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_advisor_assignment_advisor ON support.advisor_assignment (advisor_reference, student_reference);
CREATE INDEX idx_advisor_assignment_student ON support.advisor_assignment (student_reference);

CREATE TABLE support.alert (
    id                  UUID PRIMARY KEY,
    student_reference   TEXT        NOT NULL,
    severity            TEXT        NOT NULL,      -- MEDIUM | HIGH
    source              TEXT        NOT NULL,      -- which rule produced it
    triggering_signals  JSONB       NOT NULL,      -- why it fired: the alert is explainable, not a black box
    generated_at        TIMESTAMPTZ NOT NULL,
    status              TEXT        NOT NULL,      -- OPEN | ACKNOWLEDGED | CLOSED
    CONSTRAINT chk_alert_severity CHECK (severity IN ('MEDIUM', 'HIGH')),
    CONSTRAINT chk_alert_status   CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'CLOSED'))
);

CREATE INDEX idx_alert_student ON support.alert (student_reference, generated_at DESC);

CREATE TABLE support.intervention_plan (
    id           UUID PRIMARY KEY,
    alert_id     UUID NOT NULL REFERENCES support.alert (id),
    type         TEXT NOT NULL,                    -- ACADEMIC_FOLLOW_UP | INTEGRAL_SUPPORT
    description  TEXT NOT NULL,
    status       TEXT NOT NULL,                    -- PROPOSED | ACTIVE | COMPLETED
    CONSTRAINT chk_intervention_plan_status CHECK (status IN ('PROPOSED', 'ACTIVE', 'COMPLETED'))
);

CREATE INDEX idx_intervention_plan_alert ON support.intervention_plan (alert_id);

CREATE TABLE support.support_report (
    id                 UUID PRIMARY KEY,
    alert_id           UUID        NOT NULL REFERENCES support.alert (id),
    advisor_reference  TEXT        NOT NULL,
    content            TEXT        NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_support_report_alert ON support.support_report (alert_id, created_at DESC);

-- Transactional outbox (declared assumption 6): written in the same transaction as the business
-- change, holding the exact envelope a Pub/Sub subscriber would receive. Stage 2 adds a relay
-- that sets published_at.
CREATE TABLE support.outbox_event (
    id              UUID PRIMARY KEY,
    event_type      TEXT        NOT NULL,
    aggregate_type  TEXT        NOT NULL,
    aggregate_id    TEXT        NOT NULL,
    payload         JSONB       NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_event_unpublished ON support.outbox_event (created_at) WHERE published_at IS NULL;
