package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSupportRequestRequest(
    @NotNull RequestType type, @NotBlank @Size(max = 2000) String description, String alertId) {}
