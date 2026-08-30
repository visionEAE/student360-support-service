package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.support.api.dto.UpdatePlanStatusRequest;
import co.edu.icesi.student360.support.application.command.UpdateInterventionPlanStatusCommand;
import co.edu.icesi.student360.support.application.command.UpdateInterventionPlanStatusCommandHandler;
import co.edu.icesi.student360.support.application.query.GetInterventionPlansQuery;
import co.edu.icesi.student360.support.application.query.GetInterventionPlansQueryHandler;
import co.edu.icesi.student360.support.application.query.model.InterventionPlanView;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** "Intervenciones": every plan for the advisor's currently assigned students. */
@RestController
@RequestMapping("/api/support/advisors/me/intervention-plans")
public class AdvisorInterventionPlanController {

  private final GetInterventionPlansQueryHandler plans;
  private final UpdateInterventionPlanStatusCommandHandler updateStatus;

  public AdvisorInterventionPlanController(
      GetInterventionPlansQueryHandler plans,
      UpdateInterventionPlanStatusCommandHandler updateStatus) {
    this.plans = plans;
    this.updateStatus = updateStatus;
  }

  @GetMapping
  public List<InterventionPlanView> list() {
    return plans.handle(
        new GetInterventionPlansQuery(AdvisorStudentsController.advisorReference()));
  }

  @PatchMapping("/{id}")
  public void updateStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdatePlanStatusRequest body) {
    updateStatus.handle(new UpdateInterventionPlanStatusCommand(id, body.status()));
  }
}
