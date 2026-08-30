package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.SupportRequest;
import co.edu.icesi.student360.support.domain.port.SupportRequestRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** A request to another office (financial wellbeing, psychology, tutoring…) for a student. */
public class CreateSupportRequestCommandHandler {

  private final SupportRequestRepository requests;
  private final AssignmentAccessPolicy assignments;
  private final EventPublisher events;
  private final Clock clock;

  public CreateSupportRequestCommandHandler(
      SupportRequestRepository requests,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    this.requests = requests;
    this.assignments = assignments;
    this.events = events;
    this.clock = clock;
  }

  @Audited(
      action = "CREATE_SUPPORT_REQUEST",
      subjectType = "STUDENT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public UUID handle(CreateSupportRequestCommand command) {
    assignments.assertAssigned(command.studentReference(), "STUDENT", command.studentReference());
    Instant now = clock.instant();
    String advisor = IdentityContext.require().externalReference();
    SupportRequest request =
        requests.save(
            SupportRequest.open(
                advisor,
                command.studentReference(),
                command.alertId(),
                command.type(),
                command.description(),
                now));
    events.publish(
        new DomainEvent(
            SupportEvents.SUPPORT_REQUEST_CREATED,
            SupportEvents.AGGREGATE_REQUEST,
            request.getId().toString(),
            now,
            Map.of(
                "studentReference", command.studentReference(),
                "type", request.getType().name(),
                "createdBy", advisor)));
    return request.getId();
  }
}
