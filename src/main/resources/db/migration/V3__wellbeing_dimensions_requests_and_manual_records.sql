-- Contract v2 (docs/api-contract-v2.md): wellbeing entries with three dimensions and drafts,
-- support requests, manual alerts and intervention plans that exist without an alert.

-- Entries: a draft is invisible to advisors and never evaluated by the rule.
ALTER TABLE support.wellbeing_entry
    ADD COLUMN status     TEXT        NOT NULL DEFAULT 'SENT',
    ADD COLUMN sent_at    TIMESTAMPTZ,
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD CONSTRAINT chk_wellbeing_entry_status CHECK (status IN ('DRAFT', 'SENT'));
UPDATE support.wellbeing_entry SET sent_at = recorded_at WHERE status = 'SENT';
ALTER TABLE support.wellbeing_entry DROP CONSTRAINT chk_wellbeing_entry_level;
ALTER TABLE support.wellbeing_entry ADD CONSTRAINT chk_wellbeing_entry_level CHECK (level BETWEEN 1 AND 4);
-- The free-text comment now lives per dimension.
ALTER TABLE support.wellbeing_entry DROP COLUMN comment;

CREATE TABLE support.wellbeing_entry_dimension (
    id         BIGSERIAL PRIMARY KEY,
    entry_id   UUID     NOT NULL REFERENCES support.wellbeing_entry (id) ON DELETE CASCADE,
    dimension  TEXT     NOT NULL,                  -- ECONOMIC | ACADEMIC | EMOTIONAL
    mood       SMALLINT NOT NULL,                  -- 1 DIFFICULT .. 4 VERY_GOOD
    needs      TEXT[]   NOT NULL DEFAULT '{}',
    note       TEXT,                               -- free text; never logged nor published
    CONSTRAINT uq_wellbeing_entry_dimension UNIQUE (entry_id, dimension),
    CONSTRAINT chk_wellbeing_entry_dimension CHECK (dimension IN ('ECONOMIC', 'ACADEMIC', 'EMOTIONAL')),
    CONSTRAINT chk_wellbeing_entry_mood CHECK (mood BETWEEN 1 AND 4)
);

CREATE INDEX idx_wellbeing_entry_status ON support.wellbeing_entry (student_pseudonym, status, recorded_at DESC);

-- Alerts can also be raised by an advisor's judgement; status changes are managed.
ALTER TABLE support.alert
    ADD COLUMN created_by TEXT,                    -- advisor reference for manual alerts, NULL for the rule
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Plans may be created by an advisor without an alert ("Nueva intervención").
ALTER TABLE support.intervention_plan
    ALTER COLUMN alert_id DROP NOT NULL,
    ADD COLUMN student_reference TEXT,
    ADD COLUMN created_by TEXT,
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
UPDATE support.intervention_plan p SET student_reference = a.student_reference
FROM support.alert a WHERE a.id = p.alert_id AND p.student_reference IS NULL;
ALTER TABLE support.intervention_plan ALTER COLUMN student_reference SET NOT NULL;
CREATE INDEX idx_intervention_plan_student ON support.intervention_plan (student_reference, created_at DESC);

-- Requests: what the support team asks of another office on behalf of a student.
CREATE TABLE support.support_request (
    id                 UUID PRIMARY KEY,
    student_reference  TEXT        NOT NULL,
    alert_id           UUID REFERENCES support.alert (id),
    type               TEXT        NOT NULL,
    description        TEXT        NOT NULL,
    status             TEXT        NOT NULL,       -- OPEN | IN_PROGRESS | RESOLVED
    resolution         TEXT,
    created_by         TEXT        NOT NULL,       -- advisor reference
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_support_request_type CHECK (type IN ('FINANCIAL_WELLBEING_REFERRAL', 'PSYCHOLOGICAL_SUPPORT_REFERRAL', 'TUTORING', 'WORKLOAD_ADJUSTMENT', 'PROFESSOR_MEETING', 'OTHER')),
    CONSTRAINT chk_support_request_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED'))
);

CREATE INDEX idx_support_request_student ON support.support_request (student_reference, created_at DESC);
CREATE INDEX idx_support_request_creator ON support.support_request (created_by, created_at DESC);
