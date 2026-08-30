package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.support.api.dto.SupportReportRequest;
import co.edu.icesi.student360.support.api.dto.UpdateAlertStatusRequest;
import co.edu.icesi.student360.support.application.command.AddSupportReportCommand;
import co.edu.icesi.student360.support.application.command.AddSupportReportCommandHandler;
import co.edu.icesi.student360.support.application.command.UpdateAlertStatusCommand;
import co.edu.icesi.student360.support.application.command.UpdateAlertStatusCommandHandler;
import co.edu.icesi.student360.support.application.query.GetAlertDetailQuery;
import co.edu.icesi.student360.support.application.query.GetAlertDetailQueryHandler;
import co.edu.icesi.student360.support.application.query.GetAlertInboxQuery;
import co.edu.icesi.student360.support.application.query.GetAlertInboxQueryHandler;
import co.edu.icesi.student360.support.application.query.model.AlertDetailView;
import co.edu.icesi.student360.support.application.query.model.AlertSummaryView;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support/advisors/me/alerts")
public class AdvisorAlertController {

  private final GetAlertInboxQueryHandler inbox;
  private final GetAlertDetailQueryHandler detail;
  private final AddSupportReportCommandHandler addReport;
  private final UpdateAlertStatusCommandHandler updateStatus;

  public AdvisorAlertController(
      GetAlertInboxQueryHandler inbox,
      GetAlertDetailQueryHandler detail,
      AddSupportReportCommandHandler addReport,
      UpdateAlertStatusCommandHandler updateStatus) {
    this.inbox = inbox;
    this.detail = detail;
    this.addReport = addReport;
    this.updateStatus = updateStatus;
  }

  @GetMapping
  public List<AlertSummaryView> inbox() {
    return inbox.handle(new GetAlertInboxQuery(AdvisorStudentsController.advisorReference()));
  }

  @GetMapping("/{id}")
  public AlertDetailView detail(@PathVariable UUID id) {
    return detail.handle(new GetAlertDetailQuery(id));
  }

  @PatchMapping("/{id}")
  public void updateStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateAlertStatusRequest body) {
    updateStatus.handle(new UpdateAlertStatusCommand(id, body.status()));
  }

  @PostMapping("/{id}/reports")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> report(
      @PathVariable UUID id, @Valid @RequestBody SupportReportRequest body) {
    UUID reportId = addReport.handle(new AddSupportReportCommand(id, body.content()));
    return Map.of("id", reportId.toString(), "alertId", id.toString());
  }
}
