package co.edu.icesi.student360.support.domain.service;

import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.Severity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * The three status columns and the overall risk of the advisor's overview, exactly as the contract
 * defines them. Pure and deterministic so the table can be reasoned about.
 */
public final class RiskClassifier {

  public enum Status {
    ON_TRACK,
    WATCH,
    AT_RISK,
    UNKNOWN
  }

  public enum Risk {
    LOW,
    MEDIUM,
    HIGH
  }

  private RiskClassifier() {}

  public static Status academic(String academicStanding) {
    if (academicStanding == null) {
      return Status.UNKNOWN;
    }
    return switch (academicStanding) {
      case "GOOD" -> Status.ON_TRACK;
      case "PROBATION" -> Status.WATCH;
      case "AT_RISK" -> Status.AT_RISK;
      default -> Status.UNKNOWN;
    };
  }

  public static Status financial(Boolean overdue, BigDecimal outstandingBalance) {
    if (overdue == null) {
      return Status.UNKNOWN;
    }
    if (overdue) {
      return Status.AT_RISK;
    }
    return outstandingBalance != null && outstandingBalance.signum() > 0
        ? Status.WATCH
        : Status.ON_TRACK;
  }

  public static Status emotional(Optional<Integer> latestLevel) {
    return latestLevel
        .map(level -> level <= 1 ? Status.AT_RISK : level == 2 ? Status.WATCH : Status.ON_TRACK)
        .orElse(Status.UNKNOWN);
  }

  public static Risk overall(
      Status academic, Status financial, Status emotional, List<Alert> activeAlerts) {
    boolean highAlert = activeAlerts.stream().anyMatch(a -> a.getSeverity() == Severity.HIGH);
    List<Status> statuses = List.of(academic, financial, emotional);
    long atRisk = statuses.stream().filter(s -> s == Status.AT_RISK).count();
    long watch = statuses.stream().filter(s -> s == Status.WATCH).count();
    if (highAlert || atRisk == 3) {
      return Risk.HIGH;
    }
    boolean mediumAlert = activeAlerts.stream().anyMatch(a -> a.getSeverity() == Severity.MEDIUM);
    if (mediumAlert || atRisk >= 1 || watch >= 2) {
      return Risk.MEDIUM;
    }
    return Risk.LOW;
  }
}
