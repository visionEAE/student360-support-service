package co.edu.icesi.student360.support.application.query.model;

import java.util.List;

public record WellbeingSummaryView(
    String studentId,
    Integer currentLevel,
    String currentLevelLabel,
    int entriesThisMonth,
    String trend,
    List<WeekPointView> weekly,
    List<WellbeingEntrySummary> recent) {}
