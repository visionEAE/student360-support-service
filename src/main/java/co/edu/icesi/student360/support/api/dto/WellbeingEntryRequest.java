package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.EntryStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record WellbeingEntryRequest(
    @NotNull EntryStatus status, @NotEmpty @Valid List<DimensionInputRequest> dimensions) {}
