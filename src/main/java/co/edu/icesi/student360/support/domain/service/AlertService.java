package co.edu.icesi.student360.support.domain.service;

import co.edu.icesi.student360.common.api.exception.NotFoundException;
import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.identity.Identity;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.AlertDetail;
import co.edu.icesi.student360.support.domain.model.SupportReport;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** The advisor's side: an inbox filtered by active assignment, alert detail, support reports. */
public class AlertService {

  static final String ALERT = "ALERT";

  private final AlertRepository alerts;
  private final InterventionPlanRepository plans;
  private final SupportReportRepository reports;
  private final AssignmentAccessPolicy assignmentPolicy;
  private final Clock clock;

  public AlertService(
      AlertRepository alerts,
      InterventionPlanRepository plans,
      SupportReportRepository reports,
      AssignmentAccessPolicy assignmentPolicy,
      Clock clock) {
    this.alerts = alerts;
    this.plans = plans;
    this.reports = reports;
    this.assignmentPolicy = assignmentPolicy;
    this.clock = clock;
  }

  /** Only alerts about students the caller is actively assigned to — the inbox leaks nothing. */
  @Audited(action = "LIST_ALERT_INBOX", subjectType = "ADVISOR")
  @Transactional(readOnly = true)
  public List<Alert> inbox(String advisorReference) {
    AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
    List<String> students = assignmentPolicy.activelyAssignedStudents(advisorReference);
    if (students.isEmpty()) {
      return List.of();
    }
    return alerts.findByStudentReferenceInOrderByGeneratedAtDesc(students);
  }

  /** Authorization before existence: an unassigned advisor gets 403 whether the alert exists. */
  @Audited(action = "READ_ALERT_DETAIL", subjectType = "ALERT")
  @Transactional(readOnly = true)
  public AlertDetail detail(UUID alertId) {
    Alert alert = authorizedAlert(alertId);
    return new AlertDetail(
        alert, plans.findByAlertId(alertId), reports.findByAlertIdOrderByCreatedAtDesc(alertId));
  }

  @Audited(
      action = "CREATE_SUPPORT_REPORT",
      subjectType = "ALERT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public SupportReport report(UUID alertId, String content) {
    Alert alert = authorizedAlert(alertId);
    Identity advisor = IdentityContext.require();
    alert.acknowledge();
    return reports.save(
        SupportReport.write(alertId, advisor.externalReference(), content, clock.instant()));
  }

  private Alert authorizedAlert(UUID alertId) {
    Alert alert =
        alerts
            .findById(alertId)
            .orElseThrow(() -> new NotFoundException("Alert", alertId.toString()));
    assignmentPolicy.assertAssigned(alert.getStudentReference(), ALERT, alertId.toString());
    return alert;
  }
}
