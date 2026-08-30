package co.edu.icesi.student360.support.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.icesi.student360.support.domain.model.EngagementSnapshot;
import co.edu.icesi.student360.support.domain.model.FinancialSnapshot;
import co.edu.icesi.student360.support.domain.model.InterventionType;
import co.edu.icesi.student360.support.domain.model.RiskEvaluation;
import co.edu.icesi.student360.support.domain.model.Severity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Pure domain: no Spring, no database, no network. */
class ConvergentRiskRuleTest {

  private final ConvergentRiskRule rule =
      new ConvergentRiskRule(new RiskRuleThresholds(2, 14, new BigDecimal("0.6")));

  private static final EngagementSnapshot DISENGAGED =
      new EngagementSnapshot(21, new BigDecimal("0.40"), 2);
  private static final EngagementSnapshot ENGAGED =
      new EngagementSnapshot(1, new BigDecimal("1.00"), 0);
  private static final FinancialSnapshot OVERDUE =
      new FinancialSnapshot(new BigDecimal("4100000.00"), 62, true);
  private static final FinancialSnapshot CURRENT = new FinancialSnapshot(BigDecimal.ZERO, 0, false);

  @Test
  void shouldFireHighWhenLowWellbeingConvergesWithDisengagementAndOverdueBalance() {
    RiskEvaluation evaluation =
        rule.evaluate(1, Optional.of(DISENGAGED), Optional.of(OVERDUE), List.of());

    assertThat(evaluation.severity()).contains(Severity.HIGH);
    assertThat(evaluation.suggestedPlan()).contains(InterventionType.INTEGRAL_SUPPORT);
    assertThat(evaluation.signals().firedConditions())
        .containsExactly(
            "LOW_WELLBEING",
            "NO_RECENT_LMS_ACCESS",
            "LOW_ON_TIME_SUBMISSION_RATE",
            "OVERDUE_BALANCE");
    assertThat(evaluation.signals().daysSinceLastAccess()).isEqualTo(21);
    assertThat(evaluation.signals().overdueBalance()).isEqualByComparingTo("4100000.00");
  }

  @Test
  void shouldFireMediumWhenOnlyOneOfTheTwoConditionsConverges() {
    assertThat(
            rule.evaluate(2, Optional.of(DISENGAGED), Optional.of(CURRENT), List.of()).severity())
        .contains(Severity.MEDIUM);
    assertThat(rule.evaluate(2, Optional.of(ENGAGED), Optional.of(OVERDUE), List.of()).severity())
        .contains(Severity.MEDIUM);
  }

  @Test
  void shouldNotFireOnLowWellbeingAlone() {
    RiskEvaluation evaluation =
        rule.evaluate(1, Optional.of(ENGAGED), Optional.of(CURRENT), List.of());

    assertThat(evaluation.firesAlert()).isFalse();
    assertThat(evaluation.signals().firedConditions()).containsExactly("LOW_WELLBEING");
  }

  @Test
  void shouldNeverFireWhenWellbeingIsNotLow() {
    assertThat(
            rule.evaluate(4, Optional.of(DISENGAGED), Optional.of(OVERDUE), List.of()).firesAlert())
        .isFalse();
  }

  @Test
  void shouldDegradeWhenASourceIsUnavailableAndSaySo() {
    RiskEvaluation evaluation =
        rule.evaluate(1, Optional.empty(), Optional.of(OVERDUE), List.of("lms-service"));

    assertThat(evaluation.severity()).contains(Severity.MEDIUM);
    assertThat(evaluation.signals().unavailableSources()).containsExactly("lms-service");
    assertThat(evaluation.signals().daysSinceLastAccess()).isNull();
  }
}
