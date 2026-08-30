package co.edu.icesi.student360.support.domain.model;

import co.edu.icesi.student360.support.domain.model.source.FinancialStatus;
import java.math.BigDecimal;

/** The three numbers the rule reads from the official financial status. */
public record FinancialSnapshot(BigDecimal overdueBalance, int daysOverdue, boolean financialHold) {

  public static FinancialSnapshot from(FinancialStatus status) {
    return new FinancialSnapshot(
        status.overdueBalance(),
        status.daysOverdue() == null ? 0 : status.daysOverdue(),
        Boolean.TRUE.equals(status.financialHold()));
  }

  public boolean isOverdue() {
    return overdueBalance != null && overdueBalance.signum() > 0;
  }
}
