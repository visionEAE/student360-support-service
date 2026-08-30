package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.common.api.exception.AuthenticationFailedException;
import co.edu.icesi.student360.common.identity.Identity;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.support.api.dto.CreateInterventionPlanRequest;
import co.edu.icesi.student360.support.api.dto.CreateManualAlertRequest;
import co.edu.icesi.student360.support.api.dto.CreateSupportRequestRequest;
import co.edu.icesi.student360.support.api.dto.IdResponse;
import co.edu.icesi.student360.support.application.command.CreateInterventionPlanCommand;
import co.edu.icesi.student360.support.application.command.CreateInterventionPlanCommandHandler;
import co.edu.icesi.student360.support.application.command.CreateManualAlertCommand;
import co.edu.icesi.student360.support.application.command.CreateManualAlertCommandHandler;
import co.edu.icesi.student360.support.application.command.CreateSupportRequestCommand;
import co.edu.icesi.student360.support.application.command.CreateSupportRequestCommandHandler;
import co.edu.icesi.student360.support.application.query.GetAdvisorStudentsOverviewQuery;
import co.edu.icesi.student360.support.application.query.GetAdvisorStudentsOverviewQueryHandler;
import co.edu.icesi.student360.support.application.query.GetStudentCaseQuery;
import co.edu.icesi.student360.support.application.query.GetStudentCaseQueryHandler;
import co.edu.icesi.student360.support.application.query.GetSupportRequestsQuery;
import co.edu.icesi.student360.support.application.query.GetSupportRequestsQueryHandler;
import co.edu.icesi.student360.support.application.query.model.AdvisorStudentsOverviewView;
import co.edu.icesi.student360.support.application.query.model.StudentCaseView;
import co.edu.icesi.student360.support.application.query.model.SupportRequestView;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** "Mis estudiantes": overview, one student's full case, and the writes an advisor makes on it. */
@RestController
@RequestMapping("/api/support/advisors/me")
public class AdvisorStudentsController {

  private final GetAdvisorStudentsOverviewQueryHandler overview;
  private final GetStudentCaseQueryHandler studentCase;
  private final GetSupportRequestsQueryHandler requestsQuery;
  private final CreateInterventionPlanCommandHandler createPlan;
  private final CreateManualAlertCommandHandler createAlert;
  private final CreateSupportRequestCommandHandler createRequest;

  public AdvisorStudentsController(
      GetAdvisorStudentsOverviewQueryHandler overview,
      GetStudentCaseQueryHandler studentCase,
      GetSupportRequestsQueryHandler requestsQuery,
      CreateInterventionPlanCommandHandler createPlan,
      CreateManualAlertCommandHandler createAlert,
      CreateSupportRequestCommandHandler createRequest) {
    this.overview = overview;
    this.studentCase = studentCase;
    this.requestsQuery = requestsQuery;
    this.createPlan = createPlan;
    this.createAlert = createAlert;
    this.createRequest = createRequest;
  }

  @GetMapping("/students")
  public AdvisorStudentsOverviewView students() {
    return overview.handle(new GetAdvisorStudentsOverviewQuery(advisorReference()));
  }

  @GetMapping("/students/{id}")
  public StudentCaseView student(@PathVariable String id) {
    return studentCase.handle(new GetStudentCaseQuery(id));
  }

  @GetMapping("/students/{id}/requests")
  public List<SupportRequestView> studentRequests(@PathVariable String id) {
    return requestsQuery.handle(GetSupportRequestsQuery.forStudent(advisorReference(), id));
  }

  @PostMapping("/students/{id}/intervention-plans")
  @ResponseStatus(HttpStatus.CREATED)
  public IdResponse createPlan(
      @PathVariable String id, @Valid @RequestBody CreateInterventionPlanRequest body) {
    UUID alertId = body.alertId() == null ? null : UUID.fromString(body.alertId());
    UUID planId =
        createPlan.handle(
            new CreateInterventionPlanCommand(id, alertId, body.type(), body.description()));
    return new IdResponse(planId.toString());
  }

  @PostMapping("/students/{id}/alerts")
  @ResponseStatus(HttpStatus.CREATED)
  public IdResponse createAlert(
      @PathVariable String id, @Valid @RequestBody CreateManualAlertRequest body) {
    UUID alertId =
        createAlert.handle(new CreateManualAlertCommand(id, body.severity(), body.reason()));
    return new IdResponse(alertId.toString());
  }

  @PostMapping("/students/{id}/requests")
  @ResponseStatus(HttpStatus.CREATED)
  public IdResponse createRequest(
      @PathVariable String id, @Valid @RequestBody CreateSupportRequestRequest body) {
    UUID alertId = body.alertId() == null ? null : UUID.fromString(body.alertId());
    UUID requestId =
        createRequest.handle(
            new CreateSupportRequestCommand(id, alertId, body.type(), body.description()));
    return new IdResponse(requestId.toString());
  }

  static String advisorReference() {
    Identity caller = IdentityContext.require();
    if (caller.externalReference() == null) {
      throw new AuthenticationFailedException("Caller has no advisor reference");
    }
    return caller.externalReference();
  }
}
