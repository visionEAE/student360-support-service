package co.edu.icesi.student360.support.domain.service;

import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Pure computation over a student's sent entries: current level, trend, weekly series. */
public class WellbeingSummaryCalculator {

  public static final int WEEKS = 6;

  public enum Trend {
    UP,
    DOWN,
    STABLE
  }

  public record WeekPoint(LocalDate weekStart, String label, Integer level) {}

  public record Summary(
      Integer currentLevel, int entriesThisMonth, Trend trend, List<WeekPoint> weekly) {}

  private final Clock clock;

  public WellbeingSummaryCalculator(Clock clock) {
    this.clock = clock;
  }

  /**
   * @param sentNewestFirst sent entries, newest first
   */
  public Summary summarise(List<WellbeingEntry> sentNewestFirst) {
    Instant now = clock.instant();
    LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
    Integer current = sentNewestFirst.isEmpty() ? null : sentNewestFirst.get(0).getLevel();
    Trend trend = Trend.STABLE;
    if (sentNewestFirst.size() >= 2) {
      int latest = sentNewestFirst.get(0).getLevel();
      int previous = sentNewestFirst.get(1).getLevel();
      trend = latest > previous ? Trend.UP : latest < previous ? Trend.DOWN : Trend.STABLE;
    }
    LocalDate monthStart = today.withDayOfMonth(1);
    int thisMonth =
        (int)
            sentNewestFirst.stream()
                .filter(
                    e ->
                        !LocalDate.ofInstant(e.getRecordedAt(), ZoneOffset.UTC)
                            .isBefore(monthStart))
                .count();
    LocalDate currentWeekStart = today.with(DayOfWeek.MONDAY);
    List<WeekPoint> weekly = new ArrayList<>();
    for (int i = WEEKS - 1; i >= 0; i--) {
      LocalDate start = currentWeekStart.minusWeeks(i);
      LocalDate end = start.plusWeeks(1);
      Optional<Integer> level =
          sentNewestFirst.stream()
              .filter(
                  e -> {
                    LocalDate day = LocalDate.ofInstant(e.getRecordedAt(), ZoneOffset.UTC);
                    return !day.isBefore(start) && day.isBefore(end);
                  })
              .map(WellbeingEntry::getLevel)
              .min(Comparator.naturalOrder());
      weekly.add(new WeekPoint(start, "S" + (WEEKS - i), level.orElse(null)));
    }
    return new Summary(current, thisMonth, trend, weekly);
  }
}
