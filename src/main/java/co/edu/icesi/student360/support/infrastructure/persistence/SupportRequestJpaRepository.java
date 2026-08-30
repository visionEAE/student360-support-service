package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.SupportRequest;
import co.edu.icesi.student360.support.domain.port.SupportRequestRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestJpaRepository
    extends JpaRepository<SupportRequest, UUID>, SupportRequestRepository {

  @Override
  List<SupportRequest> findByStudentReferenceInOrderByCreatedAtDesc(
      Collection<String> studentReferences);
}
