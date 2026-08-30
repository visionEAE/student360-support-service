package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.support.application.query.model.AlertSummaryView;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/** Only alerts about students the caller is actively assigned to — the inbox leaks nothing. */
public class GetAlertInboxQueryHandler {

  private final AlertRepository alerts;
  private final AssignmentAccessPolicy assignments;

  public GetAlertInboxQueryHandler(AlertRepository alerts, AssignmentAccessPolicy assignments) {
    this.alerts = alerts;
    this.assignments = assignments;
  }

  @Audited(action = "LIST_ALERT_INBOX", subjectType = "ADVISOR")
  @Transactional(readOnly = true)
  public List<AlertSummaryView> handle(GetAlertInboxQuery query) {
    AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
    List<String> students = assignments.activelyAssignedStudents(query.advisorReference());
    if (students.isEmpty()) {
      return List.of();
    }
    return alerts.findByStudentReferenceInOrderByGeneratedAtDesc(students).stream()
        .map(GetAlertInboxQueryHandler::toView)
        .toList();
  }

  static AlertSummaryView toView(Alert alert) {
    return new AlertSummaryView(
        alert.getId().toString(),
        alert.getStudentReference(),
        alert.getSeverity().name(),
        alert.getStatus().name(),
        alert.getGeneratedAt(),
        alert.getTriggeringSignals().firedConditions());
  }
}
