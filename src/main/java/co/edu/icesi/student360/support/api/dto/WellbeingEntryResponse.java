package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.Alert;
import java.util.Optional;

/** The student learns whether their entry triggered an alert, never the signals behind it. */
public record WellbeingEntryResponse(
    String studentId, int level, boolean alertGenerated, String alertId) {

  public static WellbeingEntryResponse of(String studentId, int level, Optional<Alert> alert) {
    return new WellbeingEntryResponse(
        studentId, level, alert.isPresent(), alert.map(a -> a.getId().toString()).orElse(null));
  }
}
