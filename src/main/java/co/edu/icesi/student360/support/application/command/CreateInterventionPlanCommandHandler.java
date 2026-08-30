package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Nueva intervención": an advisor opens a plan for an assigned student, with or without an alert.
 */
public class CreateInterventionPlanCommandHandler {

  private final InterventionPlanRepository plans;
  private final AssignmentAccessPolicy assignments;
  private final EventPublisher events;
  private final Clock clock;

  public CreateInterventionPlanCommandHandler(
      InterventionPlanRepository plans,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    this.plans = plans;
    this.assignments = assignments;
    this.events = events;
    this.clock = clock;
  }

  @Audited(
      action = "CREATE_INTERVENTION_PLAN",
      subjectType = "STUDENT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public UUID handle(CreateInterventionPlanCommand command) {
    assignments.assertAssigned(command.studentReference(), "STUDENT", command.studentReference());
    Instant now = clock.instant();
    String advisor = IdentityContext.require().externalReference();
    InterventionPlan plan =
        plans.save(
            InterventionPlan.createdBy(
                advisor,
                command.alertId(),
                command.studentReference(),
                command.type(),
                command.description(),
                now));
    events.publish(
        new DomainEvent(
            SupportEvents.INTERVENTION_PLAN_CREATED,
            SupportEvents.AGGREGATE_PLAN,
            plan.getId().toString(),
            now,
            Map.of(
                "studentReference", command.studentReference(),
                "type", plan.getType().name(),
                "createdBy", advisor)));
    return plan.getId();
  }
}
