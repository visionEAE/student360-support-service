package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.support.api.dto.WellbeingEntryRequest;
import co.edu.icesi.student360.support.api.dto.WellbeingEntryResponse;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.service.WellbeingService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support/students")
public class StudentWellbeingController {

  private final WellbeingService wellbeing;

  public StudentWellbeingController(WellbeingService wellbeing) {
    this.wellbeing = wellbeing;
  }

  @PostMapping("/{id}/wellbeing-entries")
  @ResponseStatus(HttpStatus.CREATED)
  public WellbeingEntryResponse record(
      @PathVariable String id, @Valid @RequestBody WellbeingEntryRequest body) {
    Optional<Alert> alert = wellbeing.recordEntry(id, body.level(), body.comment());
    return WellbeingEntryResponse.of(id, body.level(), alert);
  }
}
