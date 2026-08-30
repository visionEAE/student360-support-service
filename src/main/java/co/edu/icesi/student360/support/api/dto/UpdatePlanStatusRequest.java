package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.PlanStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePlanStatusRequest(@NotNull PlanStatus status) {}
