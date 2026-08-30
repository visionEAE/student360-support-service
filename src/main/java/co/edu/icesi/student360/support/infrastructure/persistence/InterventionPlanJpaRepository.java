package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionPlanJpaRepository
    extends JpaRepository<InterventionPlan, UUID>, InterventionPlanRepository {

  @Override
  Optional<InterventionPlan> findFirstByAlertIdOrderByCreatedAtDesc(UUID alertId);

  @Override
  List<InterventionPlan> findByStudentReferenceInOrderByCreatedAtDesc(
      Collection<String> studentReferences);
}
