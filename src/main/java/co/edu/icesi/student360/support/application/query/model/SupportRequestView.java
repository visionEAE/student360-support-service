package co.edu.icesi.student360.support.application.query.model;

import java.time.Instant;

public record SupportRequestView(
    String id,
    String studentId,
    String alertId,
    String type,
    String description,
    String status,
    String resolution,
    String createdBy,
    Instant createdAt,
    Instant updatedAt) {}
