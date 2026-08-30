package co.edu.icesi.student360.support.domain.model;

import co.edu.icesi.student360.support.domain.model.source.EngagementSignals;
import java.math.BigDecimal;

/** The three numbers the rule reads from the LMS signal; the LMS owns the interpretation. */
public record EngagementSnapshot(
    Integer daysSinceLastAccess, BigDecimal onTimeSubmissionRate, Integer coursesWithoutActivity) {

  public static EngagementSnapshot from(EngagementSignals signals) {
    return new EngagementSnapshot(
        signals.daysSinceLastAccess(),
        signals.onTimeSubmissionRate(),
        signals.coursesWithoutActivity());
  }
}
