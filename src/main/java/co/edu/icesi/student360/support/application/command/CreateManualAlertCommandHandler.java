package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.TriggeringSignals;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** An alert raised by an advisor's judgement; explainable through its reason. */
public class CreateManualAlertCommandHandler {

  private final AlertRepository alerts;
  private final AssignmentAccessPolicy assignments;
  private final EventPublisher events;
  private final Clock clock;

  public CreateManualAlertCommandHandler(
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
      action = "CREATE_MANUAL_ALERT",
      subjectType = "STUDENT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public UUID handle(CreateManualAlertCommand command) {
    assignments.assertAssigned(command.studentReference(), "STUDENT", command.studentReference());
    Instant now = clock.instant();
    String advisor = IdentityContext.require().externalReference();
    Alert alert =
        alerts.save(
            Alert.raisedBy(
                advisor,
                command.studentReference(),
                command.severity(),
                TriggeringSignals.advisorJudgement(command.reason()),
                now));
    events.publish(
        new DomainEvent(
            SupportEvents.ALERT_GENERATED,
            SupportEvents.AGGREGATE_ALERT,
            alert.getId().toString(),
            now,
            Map.of(
                "studentReference", command.studentReference(),
                "severity", alert.getSeverity().name(),
                "source", alert.getSource(),
                "createdBy", advisor)));
    return alert.getId();
  }
}
