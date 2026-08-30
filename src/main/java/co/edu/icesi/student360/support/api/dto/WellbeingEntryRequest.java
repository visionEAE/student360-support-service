package co.edu.icesi.student360.support.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WellbeingEntryRequest(
    @NotNull @Min(1) @Max(5) Integer level, @Size(max = 2000) String comment) {}
