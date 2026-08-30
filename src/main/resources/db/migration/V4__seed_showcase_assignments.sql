-- Assignments for the four showcase advisees (docs/api-contract-v2.md): split between the two
-- advisors so both overviews are populated, matching the design's "several advisors, one caseload
-- each" picture rather than concentrating everyone under A-2001.
INSERT INTO support.advisor_assignment (advisor_reference, student_reference, valid_from, valid_to) VALUES
    ('A-2001', 'S-1007', DATE '2026-02-01', NULL),
    ('A-2002', 'S-1008', DATE '2026-02-01', NULL),
    ('A-2002', 'S-1009', DATE '2026-02-01', NULL),
    ('A-2001', 'S-1010', DATE '2026-02-01', NULL);
