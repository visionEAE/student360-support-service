package co.edu.icesi.student360.support.domain.model.source;

import java.math.BigDecimal;
import java.time.Instant;

/** core-service {@code GET /students/summaries?ids=}. */
public record StudentSummary(
    String id,
    String code,
    String fullName,
    Program program,
    Integer currentSemester,
    String academicStanding,
    Boolean overdue,
    Integer daysOverdue,
    BigDecimal outstandingBalance,
    Instant updatedAt) {

  public record Program(String code, String name) {}
}
