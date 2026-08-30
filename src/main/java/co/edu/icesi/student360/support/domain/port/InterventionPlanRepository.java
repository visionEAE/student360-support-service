package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import java.util.Optional;
import java.util.UUID;

public interface InterventionPlanRepository {

  InterventionPlan save(InterventionPlan plan);

  Optional<InterventionPlan> findByAlertId(UUID alertId);
}
