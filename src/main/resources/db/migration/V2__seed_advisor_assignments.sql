-- A-2001 (Carlos Mejía) is assigned to the at-risk student S-1003 and to S-1001; A-2002 (Diana
-- Pérez) only to S-1002. Negative scenario A of the demonstration thread is A-2002 opening S-1003.
INSERT INTO support.advisor_assignment (advisor_reference, student_reference, valid_from, valid_to) VALUES
    ('A-2001', 'S-1003', DATE '2026-01-15', NULL),
    ('A-2001', 'S-1001', DATE '2026-01-15', NULL),
    ('A-2002', 'S-1002', DATE '2026-01-15', NULL),
    -- an expired assignment must not authorize anything
    ('A-2002', 'S-1003', DATE '2025-01-15', DATE '2025-06-30');

-- Contract v2 extra students (S-1004/1005/1006 seeded by core-service and lms-service).
INSERT INTO support.advisor_assignment (advisor_reference, student_reference, valid_from, valid_to) VALUES
    ('A-2001', 'S-1004', DATE '2026-02-01', NULL),
    ('A-2001', 'S-1005', DATE '2026-02-01', NULL),
    ('A-2002', 'S-1006', DATE '2026-02-01', NULL);
