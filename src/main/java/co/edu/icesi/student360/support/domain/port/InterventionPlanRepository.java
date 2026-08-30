package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterventionPlanRepository {

  InterventionPlan save(InterventionPlan plan);

  Optional<InterventionPlan> findById(UUID id);

  Optional<InterventionPlan> findFirstByAlertIdOrderByCreatedAtDesc(UUID alertId);

  List<InterventionPlan> findByStudentReferenceInOrderByCreatedAtDesc(
      Collection<String> studentReferences);
}
