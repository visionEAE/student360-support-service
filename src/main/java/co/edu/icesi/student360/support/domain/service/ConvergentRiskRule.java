package co.edu.icesi.student360.support.domain.service;

import co.edu.icesi.student360.support.domain.model.EngagementSnapshot;
import co.edu.icesi.student360.support.domain.model.FinancialSnapshot;
import co.edu.icesi.student360.support.domain.model.InterventionType;
import co.edu.icesi.student360.support.domain.model.RiskEvaluation;
import co.edu.icesi.student360.support.domain.model.Severity;
import co.edu.icesi.student360.support.domain.model.TriggeringSignals;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * One explainable rule instead of five opaque ones. A low wellbeing entry alone is a feeling; a low
 * entry that converges with disengagement from the learning platform <em>and</em> an overdue
 * balance is a risk situation that deserves an integral intervention. Convergence with only one of
 * the two is a medium alert with an academic follow-up.
 *
 * <pre>
 *   LOW_WELLBEING            = level ≤ lowWellbeingLevel
 *   DISENGAGED      = daysSinceLastAccess &gt; maxDaysSinceAccess OR onTimeRate &lt; minOnTimeRate
 *   OVERDUE_BALANCE          = overdueBalance &gt; 0
 *   HIGH   = LOW_WELLBEING AND DISENGAGED AND OVERDUE_BALANCE
 *   MEDIUM = LOW_WELLBEING AND (DISENGAGED XOR OVERDUE_BALANCE)
 * </pre>
 *
 * Signals whose source was unavailable count as "not fired" and are listed so the alert says it was
 * evaluated in degraded mode.
 */
public class ConvergentRiskRule {

  public static final String SOURCE = "CONVERGENT_RISK_RULE_V1";
  static final String LOW_WELLBEING = "LOW_WELLBEING";
  static final String NO_RECENT_ACCESS = "NO_RECENT_LMS_ACCESS";
  static final String LOW_ON_TIME_RATE = "LOW_ON_TIME_SUBMISSION_RATE";
  static final String OVERDUE_BALANCE = "OVERDUE_BALANCE";

  private final RiskRuleThresholds thresholds;

  public ConvergentRiskRule(RiskRuleThresholds thresholds) {
    this.thresholds = thresholds;
  }

  public RiskEvaluation evaluate(
      int wellbeingLevel,
      Optional<EngagementSnapshot> engagement,
      Optional<FinancialSnapshot> financial,
      List<String> unavailableSources) {
    List<String> fired = new ArrayList<>();
    boolean lowWellbeing = wellbeingLevel <= thresholds.lowWellbeingLevel();
    if (lowWellbeing) {
      fired.add(LOW_WELLBEING);
    }
    boolean disengaged = false;
    if (engagement.isPresent()) {
      EngagementSnapshot e = engagement.get();
      if (e.daysSinceLastAccess() != null
          && e.daysSinceLastAccess() > thresholds.maxDaysSinceAccess()) {
        fired.add(NO_RECENT_ACCESS);
        disengaged = true;
      }
      if (e.onTimeSubmissionRate() != null
          && e.onTimeSubmissionRate().compareTo(thresholds.minOnTimeRate()) < 0) {
        fired.add(LOW_ON_TIME_RATE);
        disengaged = true;
      }
    }
    boolean overdue = financial.map(FinancialSnapshot::isOverdue).orElse(false);
    if (overdue) {
      fired.add(OVERDUE_BALANCE);
    }

    TriggeringSignals signals =
        new TriggeringSignals(
            wellbeingLevel,
            engagement.map(EngagementSnapshot::daysSinceLastAccess).orElse(null),
            engagement.map(EngagementSnapshot::onTimeSubmissionRate).orElse(null),
            engagement.map(EngagementSnapshot::coursesWithoutActivity).orElse(null),
            financial.map(FinancialSnapshot::overdueBalance).orElse(null),
            financial.map(FinancialSnapshot::daysOverdue).orElse(null),
            financial.map(FinancialSnapshot::financialHold).orElse(null),
            List.copyOf(fired),
            List.copyOf(unavailableSources),
            null);

    if (!lowWellbeing) {
      return new RiskEvaluation(signals, Optional.empty(), Optional.empty());
    }
    if (disengaged && overdue) {
      return new RiskEvaluation(
          signals, Optional.of(Severity.HIGH), Optional.of(InterventionType.INTEGRAL_SUPPORT));
    }
    if (disengaged || overdue) {
      return new RiskEvaluation(
          signals, Optional.of(Severity.MEDIUM), Optional.of(InterventionType.ACADEMIC_FOLLOW_UP));
    }
    return new RiskEvaluation(signals, Optional.empty(), Optional.empty());
  }
}
