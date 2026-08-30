package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.api.exception.NotFoundException;
import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

/** "Aceptar ruta" (→ ACTIVE) and closing a plan (→ COMPLETED). */
public class UpdateInterventionPlanStatusCommandHandler {

  private final InterventionPlanRepository plans;
  private final AssignmentAccessPolicy assignments;
  private final EventPublisher events;
  private final Clock clock;

  public UpdateInterventionPlanStatusCommandHandler(
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
      action = "UPDATE_INTERVENTION_PLAN",
      subjectType = "INTERVENTION_PLAN",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public void handle(UpdateInterventionPlanStatusCommand command) {
    InterventionPlan plan =
        plans
            .findById(command.planId())
            .orElseThrow(
                () -> new NotFoundException("Intervention plan", command.planId().toString()));
    assignments.assertAssigned(
        plan.getStudentReference(), "INTERVENTION_PLAN", plan.getId().toString());
    Instant now = clock.instant();
    plan.changeStatus(command.status(), now);
    events.publish(
        new DomainEvent(
            SupportEvents.INTERVENTION_PLAN_UPDATED,
            SupportEvents.AGGREGATE_PLAN,
            plan.getId().toString(),
            now,
            Map.of(
                "studentReference",
                plan.getStudentReference(),
                "status",
                plan.getStatus().name())));
  }
}
