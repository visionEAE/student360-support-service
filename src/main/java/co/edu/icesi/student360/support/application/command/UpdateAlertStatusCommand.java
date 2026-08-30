package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.AlertStatus;
import java.util.UUID;

public record UpdateAlertStatusCommand(UUID alertId, AlertStatus status) {

  @Override
  public String toString() {
    return alertId.toString();
  }
}
