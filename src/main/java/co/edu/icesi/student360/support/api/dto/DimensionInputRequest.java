package co.edu.icesi.student360.support.api.dto;

import co.edu.icesi.student360.support.domain.model.Mood;
import co.edu.icesi.student360.support.domain.model.WellbeingDimension;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DimensionInputRequest(
    @NotNull WellbeingDimension dimension,
    @NotNull Mood mood,
    List<String> needs,
    @Size(max = 2000) String note) {

  public DimensionInputRequest {
    needs = needs == null ? List.of() : List.copyOf(needs);
  }
}
