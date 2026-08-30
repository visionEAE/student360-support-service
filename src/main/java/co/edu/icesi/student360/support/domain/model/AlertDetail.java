package co.edu.icesi.student360.support.domain.model;

import java.util.List;
import java.util.Optional;

public record AlertDetail(
    Alert alert, Optional<InterventionPlan> plan, List<SupportReport> reports) {}
