package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.SupportRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportRequestRepository {

  SupportRequest save(SupportRequest request);

  Optional<SupportRequest> findById(UUID id);

  List<SupportRequest> findByStudentReferenceInOrderByCreatedAtDesc(
      Collection<String> studentReferences);
}
