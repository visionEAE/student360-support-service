package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.support.application.query.model.WeekPointView;
import co.edu.icesi.student360.support.application.query.model.WellbeingEntrySummary;
import co.edu.icesi.student360.support.application.query.model.WellbeingSummaryView;
import co.edu.icesi.student360.support.domain.model.DimensionEntry;
import co.edu.icesi.student360.support.domain.model.EntryStatus;
import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import co.edu.icesi.student360.support.domain.model.WellbeingLevel;
import co.edu.icesi.student360.support.domain.port.DimensionEntryRepository;
import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import co.edu.icesi.student360.support.domain.service.StudentCaseAccessPolicy;
import co.edu.icesi.student360.support.domain.service.WellbeingSummaryCalculator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * The wellbeing panel: self, the assigned advisor, or an admin. Read-only, composed entirely from
 * this service's own tables — no source call.
 */
public class GetWellbeingSummaryQueryHandler {

  private static final int RECENT_LIMIT = 10;

  private final WellbeingEntryRepository entries;
  private final DimensionEntryRepository dimensions;
  private final Pseudonymizer pseudonymizer;
  private final StudentCaseAccessPolicy access;
  private final WellbeingSummaryCalculator calculator;

  public GetWellbeingSummaryQueryHandler(
      WellbeingEntryRepository entries,
      DimensionEntryRepository dimensions,
      Pseudonymizer pseudonymizer,
      StudentCaseAccessPolicy access,
      WellbeingSummaryCalculator calculator) {
    this.entries = entries;
    this.dimensions = dimensions;
    this.pseudonymizer = pseudonymizer;
    this.access = access;
    this.calculator = calculator;
  }

  @Audited(action = "READ_WELLBEING_SUMMARY", subjectType = "STUDENT")
  @Transactional(readOnly = true)
  public WellbeingSummaryView handle(GetWellbeingSummaryQuery query) {
    access.assertCanRead(query.studentReference());
    return build(query.studentReference());
  }

  /**
   * For composition inside {@link GetStudentCaseQueryHandler}, where access was already checked.
   */
  public WellbeingSummaryView buildWithoutAuthorization(String studentReference) {
    return build(studentReference);
  }

  private WellbeingSummaryView build(String studentReference) {
    String pseudonym = pseudonymizer.pseudonymOf(studentReference);
    List<WellbeingEntry> sent =
        entries.findByStudentPseudonymAndStatusOrderByRecordedAtDesc(pseudonym, EntryStatus.SENT);
    WellbeingSummaryCalculator.Summary summary = calculator.summarise(sent);

    List<WellbeingEntry> recentEntries = sent.stream().limit(RECENT_LIMIT).toList();
    Map<UUID, List<DimensionEntry>> byEntry =
        dimensions
            .findByEntryIdIn(recentEntries.stream().map(WellbeingEntry::getId).toList())
            .stream()
            .collect(Collectors.groupingBy(DimensionEntry::getEntryId));
    List<WellbeingEntrySummary> recent =
        recentEntries.stream()
            .map(
                entry ->
                    new WellbeingEntrySummary(
                        entry.getId().toString(),
                        entry.getRecordedAt(),
                        entry.getLevel(),
                        byEntry.getOrDefault(entry.getId(), List.of()).stream()
                            .sorted(Comparator.comparing(d -> d.getDimension().name()))
                            .map(
                                d ->
                                    new WellbeingEntrySummary.DimensionView(
                                        d.getDimension().name(),
                                        d.getMood().name(),
                                        d.getNeeds(),
                                        d.getNote()))
                            .toList()))
            .toList();

    return new WellbeingSummaryView(
        studentReference,
        summary.currentLevel(),
        summary.currentLevel() == null ? null : WellbeingLevel.of(summary.currentLevel()).name(),
        summary.entriesThisMonth(),
        summary.trend().name(),
        summary.weekly().stream()
            .map(w -> new WeekPointView(w.weekStart().toString(), w.label(), w.level()))
            .toList(),
        recent);
  }
}
