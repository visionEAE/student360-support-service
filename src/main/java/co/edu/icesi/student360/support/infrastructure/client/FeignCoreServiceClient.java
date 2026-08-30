package co.edu.icesi.student360.support.infrastructure.client;

import co.edu.icesi.student360.support.domain.model.source.AcademicStatus;
import co.edu.icesi.student360.support.domain.model.source.FinancialStatus;
import co.edu.icesi.student360.support.domain.model.source.StudentProfile;
import co.edu.icesi.student360.support.domain.model.source.StudentSummary;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.SourceUnavailableException;
import feign.FeignException;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/** Adapter: maps the Feign contract to the port and transport failures to "unavailable". */
@Component
public class FeignCoreServiceClient implements CoreServiceClient {

  static final String SOURCE = "core-service";

  private final CoreServiceFeignClient feign;

  public FeignCoreServiceClient(CoreServiceFeignClient feign) {
    this.feign = feign;
  }

  @Override
  public StudentProfile fetchStudentProfile(String studentReference) {
    return guarded(() -> feign.profile(studentReference));
  }

  @Override
  public AcademicStatus fetchAcademicStatus(String studentReference) {
    return guarded(() -> feign.academicStatus(studentReference));
  }

  @Override
  public FinancialStatus fetchFinancialStatus(String studentReference) {
    return guarded(() -> feign.financialStatus(studentReference));
  }

  @Override
  public List<StudentSummary> fetchStudentSummaries(Collection<String> studentReferences) {
    if (studentReferences.isEmpty()) {
      return List.of();
    }
    return guarded(() -> feign.summaries(String.join(",", studentReferences)));
  }

  private static <T> T guarded(Supplier<T> call) {
    try {
      return call.get();
    } catch (FeignException exception) {
      throw new SourceUnavailableException(SOURCE, exception);
    }
  }
}
