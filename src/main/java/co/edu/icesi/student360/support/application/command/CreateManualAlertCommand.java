package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.Severity;

public record CreateManualAlertCommand(String studentReference, Severity severity, String reason) {

  @Override
  public String toString() {
    return studentReference;
  }
}
