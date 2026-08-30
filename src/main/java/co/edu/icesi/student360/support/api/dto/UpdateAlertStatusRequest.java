package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAlertStatusRequest(@NotNull AlertStatus status) {}
