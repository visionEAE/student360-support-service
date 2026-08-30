package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.InterventionType;
import java.util.UUID;

public record CreateInterventionPlanCommand(
    String studentReference, UUID alertId, InterventionType type, String description) {

  @Override
  public String toString() {
    return studentReference;
  }
}
