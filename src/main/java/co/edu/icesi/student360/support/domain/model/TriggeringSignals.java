package co.edu.icesi.student360.support.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exactly what the rule saw when it fired, persisted as JSON with the alert. Null fields mean the
 * source was unavailable at evaluation time — a degraded evaluation is recorded as such.
 */
public record TriggeringSignals(
    int wellbeingLevel,
    Integer daysSinceLastAccess,
    BigDecimal onTimeSubmissionRate,
    Integer coursesWithoutActivity,
    BigDecimal overdueBalance,
    Integer daysOverdue,
    Boolean financialHold,
    List<String> firedConditions,
    List<String> unavailableSources,
    String reason) {

  public static final String ADVISOR_JUDGEMENT = "ADVISOR_JUDGEMENT";

  /** Signals of an alert an advisor raised by hand: only their reason. */
  public static TriggeringSignals advisorJudgement(String reason) {
    return new TriggeringSignals(
        0, null, null, null, null, null, null, List.of(ADVISOR_JUDGEMENT), List.of(), reason);
  }
}
