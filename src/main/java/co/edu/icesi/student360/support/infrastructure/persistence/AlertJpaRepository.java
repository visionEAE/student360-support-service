package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.AlertStatus;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertJpaRepository extends JpaRepository<Alert, UUID>, AlertRepository {

  @Override
  List<Alert> findByStudentReferenceInOrderByGeneratedAtDesc(Collection<String> studentReferences);

  @Override
  List<Alert> findByStudentReferenceInAndStatusInOrderByGeneratedAtDesc(
      Collection<String> studentReferences, Collection<AlertStatus> statuses);
}
