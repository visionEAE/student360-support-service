package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateManualAlertRequest(
    @NotNull Severity severity, @NotBlank @Size(max = 2000) String reason) {}
