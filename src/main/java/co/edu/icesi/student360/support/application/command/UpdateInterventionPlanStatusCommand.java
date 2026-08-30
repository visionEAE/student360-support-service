package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.PlanStatus;
import java.util.UUID;

public record UpdateInterventionPlanStatusCommand(UUID planId, PlanStatus status) {

  @Override
  public String toString() {
    return planId.toString();
  }
}
