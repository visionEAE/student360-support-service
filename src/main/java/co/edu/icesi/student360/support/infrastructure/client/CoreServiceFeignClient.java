package co.edu.icesi.student360.support.infrastructure.client;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "core-service",
    url = "${student360.clients.core-service.url}",
    configuration = CoreServiceFeignClient.Configuration.class)
public interface CoreServiceFeignClient {

  @GetMapping("/api/core/students/{id}/financial-status")
  FinancialStatusResponse financialStatus(@PathVariable("id") String studentReference);

  /** Only the fields this service reads; the rest of core's contract is ignored. */
  record FinancialStatusResponse(
      BigDecimal overdueBalance, int daysOverdue, boolean financialHold) {}

  class Configuration {
    @org.springframework.context.annotation.Bean
    DownstreamRequestInterceptor coreServiceRequestInterceptor(
        co.edu.icesi.student360.common.security.ServiceTokenProvider serviceTokens) {
      return new DownstreamRequestInterceptor(serviceTokens, "core-service");
    }
  }
}
