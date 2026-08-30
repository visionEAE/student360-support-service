package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.api.exception.NotFoundException;
import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.SupportRequest;
import co.edu.icesi.student360.support.domain.port.SupportRequestRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

public class UpdateSupportRequestStatusCommandHandler {

  private final SupportRequestRepository requests;
  private final AssignmentAccessPolicy assignments;
  private final EventPublisher events;
  private final Clock clock;

  public UpdateSupportRequestStatusCommandHandler(
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
      action = "UPDATE_SUPPORT_REQUEST",
      subjectType = "SUPPORT_REQUEST",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public void handle(UpdateSupportRequestStatusCommand command) {
    SupportRequest request =
        requests
            .findById(command.requestId())
            .orElseThrow(
                () -> new NotFoundException("Support request", command.requestId().toString()));
    assignments.assertAssigned(
        request.getStudentReference(), "SUPPORT_REQUEST", request.getId().toString());
    Instant now = clock.instant();
    request.changeStatus(command.status(), command.resolution(), now);
    events.publish(
        new DomainEvent(
            SupportEvents.SUPPORT_REQUEST_UPDATED,
            SupportEvents.AGGREGATE_REQUEST,
            request.getId().toString(),
            now,
            Map.of(
                "studentReference",
                request.getStudentReference(),
                "status",
                request.getStatus().name())));
  }
}
