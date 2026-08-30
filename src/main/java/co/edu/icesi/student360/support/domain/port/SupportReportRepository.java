package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.SupportReport;
import java.util.List;
import java.util.UUID;

public interface SupportReportRepository {

  SupportReport save(SupportReport report);

  List<SupportReport> findByAlertIdOrderByCreatedAtDesc(UUID alertId);

  List<SupportReport> findByAdvisorReferenceOrderByCreatedAtDesc(String advisorReference);
}
