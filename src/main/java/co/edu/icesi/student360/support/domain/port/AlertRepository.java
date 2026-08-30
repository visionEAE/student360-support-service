package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.AlertStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository {

  Alert save(Alert alert);

  Optional<Alert> findById(UUID id);

  List<Alert> findByStudentReferenceInOrderByGeneratedAtDesc(Collection<String> studentReferences);

  List<Alert> findByStudentReferenceInAndStatusInOrderByGeneratedAtDesc(
      Collection<String> studentReferences, Collection<AlertStatus> statuses);
}
