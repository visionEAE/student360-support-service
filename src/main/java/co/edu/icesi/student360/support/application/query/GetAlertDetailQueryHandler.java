package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.api.exception.NotFoundException;
import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.support.application.query.model.AlertDetailView;
import co.edu.icesi.student360.support.application.query.model.InterventionPlanView;
import co.edu.icesi.student360.support.application.query.model.SupportReportView;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import co.edu.icesi.student360.support.domain.model.SupportReport;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import org.springframework.transaction.annotation.Transactional;

/** Authorization before existence: an unassigned advisor gets 403 whether the alert exists. */
public class GetAlertDetailQueryHandler {

  private final AlertRepository alerts;
  private final InterventionPlanRepository plans;
  private final SupportReportRepository reports;
  private final AssignmentAccessPolicy assignments;

  public GetAlertDetailQueryHandler(
      AlertRepository alerts,
      InterventionPlanRepository plans,
      SupportReportRepository reports,
      AssignmentAccessPolicy assignments) {
    this.alerts = alerts;
    this.plans = plans;
    this.reports = reports;
    this.assignments = assignments;
  }

  @Audited(action = "READ_ALERT_DETAIL", subjectType = "ALERT")
  @Transactional(readOnly = true)
  public AlertDetailView handle(GetAlertDetailQuery query) {
    Alert alert = authorizedAlert(query.alertId());
    InterventionPlanView plan =
        plans
            .findFirstByAlertIdOrderByCreatedAtDesc(alert.getId())
            .map(this::toPlanView)
            .orElse(null);
    var reportViews =
        reports.findByAlertIdOrderByCreatedAtDesc(alert.getId()).stream()
            .map(r -> toReportView(r, alert.getStudentReference()))
            .toList();
    return new AlertDetailView(
        alert.getId().toString(),
        alert.getStudentReference(),
        alert.getSeverity().name(),
        alert.getStatus().name(),
        alert.getSource(),
        alert.getGeneratedAt(),
        alert.getTriggeringSignals(),
        plan,
        reportViews);
  }

  Alert authorizedAlert(java.util.UUID alertId) {
    Alert alert =
        alerts
            .findById(alertId)
            .orElseThrow(() -> new NotFoundException("Alert", alertId.toString()));
    assignments.assertAssigned(alert.getStudentReference(), "ALERT", alertId.toString());
    return alert;
  }

  InterventionPlanView toPlanView(InterventionPlan plan) {
    return new InterventionPlanView(
        plan.getId().toString(),
        plan.getAlertId() == null ? null : plan.getAlertId().toString(),
        plan.getStudentReference(),
        plan.getType().name(),
        plan.getDescription(),
        plan.getStatus().name(),
        plan.getCreatedBy(),
        plan.getCreatedAt());
  }

  static SupportReportView toReportView(SupportReport report, String studentId) {
    return new SupportReportView(
        report.getId().toString(),
        report.getAlertId().toString(),
        studentId,
        report.getAdvisorReference(),
        report.getContent(),
        report.getCreatedAt());
  }
}
