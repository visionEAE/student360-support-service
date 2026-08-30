package co.edu.icesi.student360.support.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupportReportRequest(@NotBlank @Size(max = 4000) String content) {}
