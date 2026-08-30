package co.edu.icesi.student360.support.application.query.model;

import java.time.Instant;

public record SupportReportView(
    String id,
    String alertId,
    String studentId,
    String advisorId,
    String content,
    Instant createdAt) {}
