package co.edu.icesi.student360.support.application.query.model;

import java.time.Instant;
import java.util.List;

public record AlertSummaryView(
    String id,
    String studentId,
    String severity,
    String status,
    Instant generatedAt,
    List<String> firedConditions) {}
