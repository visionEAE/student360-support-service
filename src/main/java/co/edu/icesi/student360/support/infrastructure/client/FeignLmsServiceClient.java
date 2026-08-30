package co.edu.icesi.student360.support.infrastructure.client;

import co.edu.icesi.student360.support.domain.model.EngagementSnapshot;
import co.edu.icesi.student360.support.domain.port.LmsServiceClient;
import co.edu.icesi.student360.support.domain.port.SourceUnavailableException;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class FeignLmsServiceClient implements LmsServiceClient {

  private final LmsServiceFeignClient feign;

  public FeignLmsServiceClient(LmsServiceFeignClient feign) {
    this.feign = feign;
  }

  @Override
  public EngagementSnapshot fetchEngagementSignals(String studentReference) {
    try {
      LmsServiceFeignClient.SignalsResponse response = feign.signals(studentReference);
      return new EngagementSnapshot(
          response.daysSinceLastAccess(),
          response.onTimeSubmissionRate(),
          response.coursesWithoutActivity());
    } catch (FeignException exception) {
      throw new SourceUnavailableException("lms-service", exception);
    }
  }
}
