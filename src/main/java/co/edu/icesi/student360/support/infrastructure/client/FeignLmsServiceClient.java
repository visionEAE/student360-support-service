package co.edu.icesi.student360.support.infrastructure.client;

import co.edu.icesi.student360.support.domain.model.source.EngagementSignals;
import co.edu.icesi.student360.support.domain.port.LmsServiceClient;
import co.edu.icesi.student360.support.domain.port.SourceUnavailableException;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class FeignLmsServiceClient implements LmsServiceClient {

  static final String SOURCE = "lms-service";

  private final LmsServiceFeignClient feign;

  public FeignLmsServiceClient(LmsServiceFeignClient feign) {
    this.feign = feign;
  }

  @Override
  public EngagementSignals fetchEngagementSignals(String studentReference) {
    try {
      return feign.signals(studentReference);
    } catch (FeignException exception) {
      throw new SourceUnavailableException(SOURCE, exception);
    }
  }
}
