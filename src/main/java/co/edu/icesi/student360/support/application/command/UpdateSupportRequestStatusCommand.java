package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.RequestStatus;
import java.util.UUID;

public record UpdateSupportRequestStatusCommand(
    UUID requestId, RequestStatus status, String resolution) {

  @Override
  public String toString() {
    return requestId.toString();
  }
}
