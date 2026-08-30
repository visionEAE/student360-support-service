package co.edu.icesi.student360.support.infrastructure.client;

import co.edu.icesi.student360.support.domain.model.source.AcademicStatus;
import co.edu.icesi.student360.support.domain.model.source.FinancialStatus;
import co.edu.icesi.student360.support.domain.model.source.StudentProfile;
import co.edu.icesi.student360.support.domain.model.source.StudentSummary;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "core-service",
    url = "${student360.clients.core-service.url}",
    configuration = CoreServiceFeignClient.Configuration.class)
public interface CoreServiceFeignClient {

  @GetMapping("/api/core/students/{id}")
  StudentProfile profile(@PathVariable("id") String studentReference);

  @GetMapping("/api/core/students/{id}/academic-status")
  AcademicStatus academicStatus(@PathVariable("id") String studentReference);

  @GetMapping("/api/core/students/{id}/financial-status")
  FinancialStatus financialStatus(@PathVariable("id") String studentReference);

  @GetMapping("/api/core/students/summaries")
  List<StudentSummary> summaries(@RequestParam("ids") String commaSeparatedIds);

  class Configuration {
    @org.springframework.context.annotation.Bean
    DownstreamRequestInterceptor coreServiceRequestInterceptor(
        co.edu.icesi.student360.common.security.ServiceTokenProvider serviceTokens) {
      return new DownstreamRequestInterceptor(serviceTokens, "core-service");
    }
  }
}
