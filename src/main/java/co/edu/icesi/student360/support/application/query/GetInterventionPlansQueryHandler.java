package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.support.application.query.model.InterventionPlanView;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/** "Intervenciones": every plan for the advisor's currently assigned students. */
public class GetInterventionPlansQueryHandler {

  private final InterventionPlanRepository plans;
  private final AssignmentAccessPolicy assignments;

  public GetInterventionPlansQueryHandler(
      InterventionPlanRepository plans, AssignmentAccessPolicy assignments) {
    this.plans = plans;
    this.assignments = assignments;
  }

  @Audited(action = "LIST_INTERVENTION_PLANS", subjectType = "ADVISOR")
  @Transactional(readOnly = true)
  public List<InterventionPlanView> handle(GetInterventionPlansQuery query) {
    AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
    List<String> students = assignments.activelyAssignedStudents(query.advisorReference());
    if (students.isEmpty()) {
      return List.of();
    }
    return plans.findByStudentReferenceInOrderByCreatedAtDesc(students).stream()
        .map(
            plan ->
                new InterventionPlanView(
                    plan.getId().toString(),
                    plan.getAlertId() == null ? null : plan.getAlertId().toString(),
                    plan.getStudentReference(),
                    plan.getType().name(),
                    plan.getDescription(),
                    plan.getStatus().name(),
                    plan.getCreatedBy(),
                    plan.getCreatedAt()))
        .toList();
  }
}
