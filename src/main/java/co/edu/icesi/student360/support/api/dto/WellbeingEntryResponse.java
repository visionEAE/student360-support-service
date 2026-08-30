package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.application.command.RecordWellbeingEntryResult;

public record WellbeingEntryResponse(
    String entryId, String status, int level, boolean alertGenerated, String alertId) {

  public static WellbeingEntryResponse from(RecordWellbeingEntryResult result) {
    return new WellbeingEntryResponse(
        result.entryId().toString(),
        result.status().name(),
        result.level(),
        result.alertGenerated(),
        result.alertId() == null ? null : result.alertId().toString());
  }
}
