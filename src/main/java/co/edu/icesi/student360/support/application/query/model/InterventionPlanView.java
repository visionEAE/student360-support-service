package co.edu.icesi.student360.support.application.query.model;

import java.time.Instant;

public record InterventionPlanView(
    String id,
    String alertId,
    String studentId,
    String type,
    String description,
    String status,
    String createdBy,
    Instant createdAt) {}
