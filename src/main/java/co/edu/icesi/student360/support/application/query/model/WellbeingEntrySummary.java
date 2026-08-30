package co.edu.icesi.student360.support.application.query.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record WellbeingEntrySummary(
    String entryId, Instant recordedAt, int level, List<DimensionView> dimensions) {

  public record DimensionView(String dimension, String mood, List<String> needs, String note) {}

  static Map<String, String> moodLabels() {
    return Map.of(
        "DIFFICULT", "Difícil", "FAIR", "Regular", "GOOD", "Bien", "VERY_GOOD", "Muy bien");
  }
}
