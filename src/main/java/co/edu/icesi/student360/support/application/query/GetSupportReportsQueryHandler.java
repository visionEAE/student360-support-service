package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.support.application.query.model.SupportReportView;
import co.edu.icesi.student360.support.domain.model.SupportReport;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import java.util.Map;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;

/** "Reportes": every report this advisor has written, most recent first. */
public class GetSupportReportsQueryHandler {

  private final SupportReportRepository reports;
  private final AlertRepository alerts;

  public GetSupportReportsQueryHandler(SupportReportRepository reports, AlertRepository alerts) {
    this.reports = reports;
    this.alerts = alerts;
  }

  @Audited(action = "LIST_SUPPORT_REPORTS", subjectType = "ADVISOR")
  @Transactional(readOnly = true)
  public java.util.List<SupportReportView> handle(GetSupportReportsQuery query) {
    AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
    var own = reports.findByAdvisorReferenceOrderByCreatedAtDesc(query.advisorReference());
    Map<java.util.UUID, String> studentByAlert =
        own.stream()
            .map(SupportReport::getAlertId)
            .distinct()
            .collect(
                java.util.stream.Collectors.toMap(
                    Function.identity(),
                    id -> alerts.findById(id).map(a -> a.getStudentReference()).orElse(null)));
    return own.stream()
        .map(
            report ->
                new SupportReportView(
                    report.getId().toString(),
                    report.getAlertId().toString(),
                    studentByAlert.get(report.getAlertId()),
                    report.getAdvisorReference(),
                    report.getContent(),
                    report.getCreatedAt()))
        .toList();
  }
}
