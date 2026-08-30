package co.edu.icesi.student360.support.infrastructure.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "lms-service",
    url = "${student360.clients.lms-service.url}",
    configuration = LmsServiceFeignClient.Configuration.class)
public interface LmsServiceFeignClient {

  @GetMapping("/api/lms/students/{id}/signals")
  SignalsResponse signals(@PathVariable("id") String studentReference);

  record SignalsResponse(
      Integer daysSinceLastAccess,
      BigDecimal onTimeSubmissionRate,
      Integer coursesWithoutActivity) {}

  class Configuration {
    @org.springframework.context.annotation.Bean
    DownstreamRequestInterceptor lmsServiceRequestInterceptor(
        co.edu.icesi.student360.common.security.ServiceTokenProvider serviceTokens) {
      return new DownstreamRequestInterceptor(serviceTokens, "lms-service");
    }
  }
}
