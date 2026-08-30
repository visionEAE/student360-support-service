package co.edu.icesi.student360.support.domain.model;

import java.math.BigDecimal;

/** The already-interpreted signal lms-service exposes; this service does not recompute it. */
public record EngagementSnapshot(
    Integer daysSinceLastAccess, BigDecimal onTimeSubmissionRate, Integer coursesWithoutActivity) {}
