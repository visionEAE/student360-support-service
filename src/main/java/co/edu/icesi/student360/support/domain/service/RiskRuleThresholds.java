package co.edu.icesi.student360.support.domain.service;

import java.math.BigDecimal;

/** The three numbers the rule depends on; configuration, never literals in the rule. */
public record RiskRuleThresholds(
    int lowWellbeingLevel, int maxDaysSinceAccess, BigDecimal minOnTimeRate) {}
