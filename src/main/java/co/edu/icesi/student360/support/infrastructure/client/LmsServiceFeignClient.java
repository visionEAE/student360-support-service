package co.edu.icesi.student360.support.infrastructure.client;

import co.edu.icesi.student360.support.domain.model.source.EngagementSignals;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "lms-service",
    url = "${student360.clients.lms-service.url}",
    configuration = LmsServiceFeignClient.Configuration.class)
public interface LmsServiceFeignClient {

  @GetMapping("/api/lms/students/{id}/signals")
  EngagementSignals signals(@PathVariable("id") String studentReference);

  class Configuration {
    @org.springframework.context.annotation.Bean
    DownstreamRequestInterceptor lmsServiceRequestInterceptor(
        co.edu.icesi.student360.common.security.ServiceTokenProvider serviceTokens) {
      return new DownstreamRequestInterceptor(serviceTokens, "lms-service");
    }
  }
}
