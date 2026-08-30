package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRequestStatusRequest(
    @NotNull RequestStatus status, @Size(max = 2000) String resolution) {}
