package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.common.api.exception.AuthenticationFailedException;
import co.edu.icesi.student360.common.identity.Identity;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.support.api.dto.AlertDetailResponse;
import co.edu.icesi.student360.support.api.dto.AlertSummaryResponse;
import co.edu.icesi.student360.support.api.dto.SupportReportRequest;
import co.edu.icesi.student360.support.domain.model.SupportReport;
import co.edu.icesi.student360.support.domain.service.AlertService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support/advisors/me/alerts")
public class AdvisorAlertController {

  private final AlertService alerts;

  public AdvisorAlertController(AlertService alerts) {
    this.alerts = alerts;
  }

  @GetMapping
  public List<AlertSummaryResponse> inbox() {
    return alerts.inbox(advisorReference()).stream().map(AlertSummaryResponse::from).toList();
  }

  @GetMapping("/{id}")
  public AlertDetailResponse detail(@PathVariable UUID id) {
    return AlertDetailResponse.from(alerts.detail(id));
  }

  @PostMapping("/{id}/reports")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> report(
      @PathVariable UUID id, @Valid @RequestBody SupportReportRequest body) {
    SupportReport report = alerts.report(id, body.content());
    return Map.of("id", report.getId().toString(), "alertId", id.toString());
  }

  private static String advisorReference() {
    Identity caller = IdentityContext.require();
    if (caller.externalReference() == null) {
      throw new AuthenticationFailedException("Caller has no advisor reference");
    }
    return caller.externalReference();
  }
}
