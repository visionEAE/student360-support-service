package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.support.api.dto.UpdateRequestStatusRequest;
import co.edu.icesi.student360.support.application.command.UpdateSupportRequestStatusCommand;
import co.edu.icesi.student360.support.application.command.UpdateSupportRequestStatusCommandHandler;
import co.edu.icesi.student360.support.application.query.GetSupportRequestsQuery;
import co.edu.icesi.student360.support.application.query.GetSupportRequestsQueryHandler;
import co.edu.icesi.student360.support.application.query.model.SupportRequestView;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support/advisors/me/requests")
public class AdvisorRequestsController {

  private final GetSupportRequestsQueryHandler requests;
  private final UpdateSupportRequestStatusCommandHandler updateStatus;

  public AdvisorRequestsController(
      GetSupportRequestsQueryHandler requests,
      UpdateSupportRequestStatusCommandHandler updateStatus) {
    this.requests = requests;
    this.updateStatus = updateStatus;
  }

  @GetMapping
  public List<SupportRequestView> list() {
    return requests.handle(
        GetSupportRequestsQuery.forAdvisor(AdvisorStudentsController.advisorReference()));
  }

  @PatchMapping("/{id}")
  public void updateStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateRequestStatusRequest body) {
    updateStatus.handle(
        new UpdateSupportRequestStatusCommand(id, body.status(), body.resolution()));
  }
}
