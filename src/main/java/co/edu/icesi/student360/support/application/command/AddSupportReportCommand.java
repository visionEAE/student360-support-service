package co.edu.icesi.student360.support.application.command;

import java.util.UUID;

public record AddSupportReportCommand(UUID alertId, String content) {

  @Override
  public String toString() {
    return alertId.toString();
  }
}
