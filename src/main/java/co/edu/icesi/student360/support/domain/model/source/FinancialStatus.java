package co.edu.icesi.student360.support.domain.model.source;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** core-service {@code GET /students/{id}/financial-status}, relayed as-is. */
public record FinancialStatus(
    String studentId,
    BigDecimal tuitionAmount,
    BigDecimal paidAmount,
    BigDecimal outstandingBalance,
    BigDecimal overdueBalance,
    Integer daysOverdue,
    Boolean overdue,
    LocalDate dueDate,
    String paymentPlan,
    String scholarship,
    Boolean financialHold,
    List<Payment> payments,
    Instant updatedAt) {

  public record Payment(LocalDate date, String description, BigDecimal amount, String status) {}

  public boolean isOverdue() {
    return overdueBalance != null && overdueBalance.signum() > 0;
  }
}
