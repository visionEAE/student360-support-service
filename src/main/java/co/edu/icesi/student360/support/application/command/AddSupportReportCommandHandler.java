package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.api.exception.NotFoundException;
import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.SupportReport;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** A support report on an alert; writing one acknowledges the alert. */
public class AddSupportReportCommandHandler {

  private final AlertRepository alerts;
  private final SupportReportRepository reports;
  private final AssignmentAccessPolicy assignments;
  private final EventPublisher events;
  private final Clock clock;

  public AddSupportReportCommandHandler(
      AlertRepository alerts,
      SupportReportRepository reports,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    this.alerts = alerts;
    this.reports = reports;
    this.assignments = assignments;
    this.events = events;
    this.clock = clock;
  }

  @Audited(
      action = "CREATE_SUPPORT_REPORT",
      subjectType = "ALERT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public UUID handle(AddSupportReportCommand command) {
    Alert alert =
        alerts
            .findById(command.alertId())
            .orElseThrow(() -> new NotFoundException("Alert", command.alertId().toString()));
    assignments.assertAssigned(alert.getStudentReference(), "ALERT", alert.getId().toString());
    Instant now = clock.instant();
    String advisor = IdentityContext.require().externalReference();
    alert.acknowledge(now);
    SupportReport report =
        reports.save(SupportReport.write(alert.getId(), advisor, command.content(), now));
    events.publish(
        new DomainEvent(
            SupportEvents.SUPPORT_REPORT_ADDED,
            SupportEvents.AGGREGATE_ALERT,
            alert.getId().toString(),
            now,
            Map.of(
                "reportId", report.getId().toString(),
                "studentReference", alert.getStudentReference(),
                "advisorReference", advisor,
                "alertStatus", alert.getStatus().name())));
    return report.getId();
  }
}
