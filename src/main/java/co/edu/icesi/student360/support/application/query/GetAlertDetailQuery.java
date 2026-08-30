package co.edu.icesi.student360.support.application.query;

import java.util.UUID;

public record GetAlertDetailQuery(UUID alertId) {

  @Override
  public String toString() {
    return alertId.toString();
  }
}
