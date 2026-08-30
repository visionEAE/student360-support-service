package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.api.exception.NotFoundException;
import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

public class UpdateAlertStatusCommandHandler {

  private final AlertRepository alerts;
  private final AssignmentAccessPolicy assignments;
  private final EventPublisher events;
  private final Clock clock;

  public UpdateAlertStatusCommandHandler(
      AlertRepository alerts,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    this.alerts = alerts;
    this.assignments = assignments;
    this.events = events;
    this.clock = clock;
  }

  @Audited(
      action = "UPDATE_ALERT_STATUS",
      subjectType = "ALERT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public void handle(UpdateAlertStatusCommand command) {
    Alert alert =
        alerts
            .findById(command.alertId())
            .orElseThrow(() -> new NotFoundException("Alert", command.alertId().toString()));
    assignments.assertAssigned(alert.getStudentReference(), "ALERT", alert.getId().toString());
    Instant now = clock.instant();
    alert.changeStatus(command.status(), now);
    events.publish(
        new DomainEvent(
            SupportEvents.ALERT_STATUS_CHANGED,
            SupportEvents.AGGREGATE_ALERT,
            alert.getId().toString(),
            now,
            Map.of(
                "studentReference",
                alert.getStudentReference(),
                "status",
                alert.getStatus().name())));
  }
}
