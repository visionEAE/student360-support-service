package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.AlertDetail;
import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import co.edu.icesi.student360.support.domain.model.SupportReport;
import co.edu.icesi.student360.support.domain.model.TriggeringSignals;
import java.time.Instant;
import java.util.List;

public record AlertDetailResponse(
    String id,
    String studentId,
    String severity,
    String status,
    String source,
    Instant generatedAt,
    TriggeringSignals triggeringSignals,
    PlanResponse interventionPlan,
    List<ReportResponse> reports) {

  public record PlanResponse(String id, String type, String description, String status) {
    static PlanResponse from(InterventionPlan plan) {
      return new PlanResponse(
          plan.getId().toString(),
          plan.getType().name(),
          plan.getDescription(),
          plan.getStatus().name());
    }
  }

  public record ReportResponse(String id, String advisorId, String content, Instant createdAt) {
    static ReportResponse from(SupportReport report) {
      return new ReportResponse(
          report.getId().toString(),
          report.getAdvisorReference(),
          report.getContent(),
          report.getCreatedAt());
    }
  }

  public static AlertDetailResponse from(AlertDetail detail) {
    return new AlertDetailResponse(
        detail.alert().getId().toString(),
        detail.alert().getStudentReference(),
        detail.alert().getSeverity().name(),
        detail.alert().getStatus().name(),
        detail.alert().getSource(),
        detail.alert().getGeneratedAt(),
        detail.alert().getTriggeringSignals(),
        detail.plan().map(PlanResponse::from).orElse(null),
        detail.reports().stream().map(ReportResponse::from).toList());
  }
}
