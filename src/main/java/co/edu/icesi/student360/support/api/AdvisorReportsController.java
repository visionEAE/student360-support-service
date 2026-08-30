package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.support.application.query.GetSupportReportsQuery;
import co.edu.icesi.student360.support.application.query.GetSupportReportsQueryHandler;
import co.edu.icesi.student360.support.application.query.model.SupportReportView;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** "Reportes": every support report this advisor has written. */
@RestController
@RequestMapping("/api/support/advisors/me/reports")
public class AdvisorReportsController {

  private final GetSupportReportsQueryHandler reports;

  public AdvisorReportsController(GetSupportReportsQueryHandler reports) {
    this.reports = reports;
  }

  @GetMapping
  public List<SupportReportView> list() {
    return reports.handle(new GetSupportReportsQuery(AdvisorStudentsController.advisorReference()));
  }
}
