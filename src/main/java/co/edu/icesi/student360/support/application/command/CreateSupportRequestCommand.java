package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.RequestType;
import java.util.UUID;

public record CreateSupportRequestCommand(
    String studentReference, UUID alertId, RequestType type, String description) {

  @Override
  public String toString() {
    return studentReference;
  }
}
