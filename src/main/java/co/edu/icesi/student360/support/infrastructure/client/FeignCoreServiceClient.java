package co.edu.icesi.student360.support.infrastructure.client;

import co.edu.icesi.student360.support.domain.model.FinancialSnapshot;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.SourceUnavailableException;
import feign.FeignException;
import org.springframework.stereotype.Component;

/** Adapter: maps the Feign contract to the port and transport failures to "unavailable". */
@Component
public class FeignCoreServiceClient implements CoreServiceClient {

  private final CoreServiceFeignClient feign;

  public FeignCoreServiceClient(CoreServiceFeignClient feign) {
    this.feign = feign;
  }

  @Override
  public FinancialSnapshot fetchFinancialStatus(String studentReference) {
    try {
      CoreServiceFeignClient.FinancialStatusResponse response =
          feign.financialStatus(studentReference);
      return new FinancialSnapshot(
          response.overdueBalance(), response.daysOverdue(), response.financialHold());
    } catch (FeignException exception) {
      throw new SourceUnavailableException("core-service", exception);
    }
  }
}
