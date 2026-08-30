package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.FinancialSnapshot;

/** Port: the official financial status, fetched synchronously from core-service. */
public interface CoreServiceClient {

  /**
   * @throws SourceUnavailableException when core-service cannot be reached or answers with an
   *     error; the rule then evaluates in degraded mode
   */
  FinancialSnapshot fetchFinancialStatus(String studentReference);
}
