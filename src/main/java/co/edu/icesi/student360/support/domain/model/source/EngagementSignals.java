package co.edu.icesi.student360.support.domain.model.source;

import java.math.BigDecimal;
import java.time.Instant;

/** lms-service {@code GET /students/{id}/signals}, relayed as-is. */
public record EngagementSignals(
    String studentId,
    Instant computedAt,
    Integer daysSinceLastAccess,
    Instant lastAccessAt,
    BigDecimal onTimeSubmissionRate,
    Integer coursesWithoutActivity,
    Integer activeCourses,
    Integer accessCount30d,
    Integer lateSubmissions,
    Integer missingSubmissions) {}
