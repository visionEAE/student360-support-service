package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.Alert;
import java.time.Instant;
import java.util.List;

public record AlertSummaryResponse(
    String id,
    String studentId,
    String severity,
    String status,
    Instant generatedAt,
    List<String> firedConditions) {

  public static AlertSummaryResponse from(Alert alert) {
    return new AlertSummaryResponse(
        alert.getId().toString(),
        alert.getStudentReference(),
        alert.getSeverity().name(),
        alert.getStatus().name(),
        alert.getGeneratedAt(),
        alert.getTriggeringSignals().firedConditions());
  }
}
