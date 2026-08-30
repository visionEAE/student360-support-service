package co.edu.icesi.student360.support.domain.model;

import java.util.Optional;

/** The outcome of running the rule: possibly an alert with a suggested plan. */
public record RiskEvaluation(
    TriggeringSignals signals,
    Optional<Severity> severity,
    Optional<InterventionType> suggestedPlan) {

  public boolean firesAlert() {
    return severity.isPresent();
  }
}
