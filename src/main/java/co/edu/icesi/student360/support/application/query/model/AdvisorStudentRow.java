package co.edu.icesi.student360.support.application.query.model;

import java.math.BigDecimal;
import java.time.Instant;

public record AdvisorStudentRow(
    String studentId,
    String code,
    String fullName,
    String initials,
    String program,
    Integer currentSemester,
    String academicStatus,
    String financialStatus,
    String emotionalStatus,
    String overallRisk,
    String openAlertId,
    BigDecimal outstandingBalance,
    Boolean overdue,
    Instant lastUpdatedAt) {}
