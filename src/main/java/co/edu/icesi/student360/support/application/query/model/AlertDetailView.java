package co.edu.icesi.student360.support.application.query.model;

import co.edu.icesi.student360.support.domain.model.TriggeringSignals;
import java.time.Instant;
import java.util.List;

public record AlertDetailView(
    String id,
    String studentId,
    String severity,
    String status,
    String source,
    Instant generatedAt,
    TriggeringSignals triggeringSignals,
    InterventionPlanView interventionPlan,
    List<SupportReportView> reports) {}
