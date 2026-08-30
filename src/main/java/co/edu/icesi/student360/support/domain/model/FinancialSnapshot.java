package co.edu.icesi.student360.support.domain.model;

import java.math.BigDecimal;

/** What this service needs from core-service, and nothing more. */
public record FinancialSnapshot(BigDecimal overdueBalance, int daysOverdue, boolean financialHold) {

  public boolean isOverdue() {
    return overdueBalance != null && overdueBalance.signum() > 0;
  }
}
