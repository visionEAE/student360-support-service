package co.edu.icesi.student360.support.api;

import co.edu.icesi.student360.support.api.dto.WellbeingEntryRequest;
import co.edu.icesi.student360.support.api.dto.WellbeingEntryResponse;
import co.edu.icesi.student360.support.application.command.DimensionInput;
import co.edu.icesi.student360.support.application.command.RecordWellbeingEntryCommand;
import co.edu.icesi.student360.support.application.command.RecordWellbeingEntryCommandHandler;
import co.edu.icesi.student360.support.application.query.GetStudentCaseQuery;
import co.edu.icesi.student360.support.application.query.GetStudentCaseQueryHandler;
import co.edu.icesi.student360.support.application.query.GetWellbeingDraftQuery;
import co.edu.icesi.student360.support.application.query.GetWellbeingDraftQueryHandler;
import co.edu.icesi.student360.support.application.query.GetWellbeingSummaryQuery;
import co.edu.icesi.student360.support.application.query.GetWellbeingSummaryQueryHandler;
import co.edu.icesi.student360.support.application.query.model.StudentCaseView;
import co.edu.icesi.student360.support.application.query.model.WellbeingSummaryView;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The student's own view: the 360° case, the safe-space form and its draft. */
@RestController
@RequestMapping("/api/support/students/{id}")
public class StudentSupportController {

  private final RecordWellbeingEntryCommandHandler recordEntry;
  private final GetWellbeingDraftQueryHandler getDraft;
  private final GetWellbeingSummaryQueryHandler getSummary;
  private final GetStudentCaseQueryHandler getCase;

  public StudentSupportController(
      RecordWellbeingEntryCommandHandler recordEntry,
      GetWellbeingDraftQueryHandler getDraft,
      GetWellbeingSummaryQueryHandler getSummary,
      GetStudentCaseQueryHandler getCase) {
    this.recordEntry = recordEntry;
    this.getDraft = getDraft;
    this.getSummary = getSummary;
    this.getCase = getCase;
  }

  @PostMapping("/wellbeing-entries")
  @ResponseStatus(HttpStatus.CREATED)
  public WellbeingEntryResponse create(
      @PathVariable String id, @Valid @RequestBody WellbeingEntryRequest body) {
    return WellbeingEntryResponse.from(recordEntry.handle(toCommand(id, null, body)));
  }

  @PutMapping("/wellbeing-entries/{entryId}")
  public WellbeingEntryResponse update(
      @PathVariable String id,
      @PathVariable UUID entryId,
      @Valid @RequestBody WellbeingEntryRequest body) {
    return WellbeingEntryResponse.from(recordEntry.handle(toCommand(id, entryId, body)));
  }

  @GetMapping("/wellbeing-entries/draft")
  public ResponseEntity<WellbeingEntryRequest> draft(@PathVariable String id) {
    return getDraft
        .handle(new GetWellbeingDraftQuery(id))
        .map(StudentSupportController::toResponse)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/wellbeing-summary")
  public WellbeingSummaryView wellbeingSummary(@PathVariable String id) {
    return getSummary.handle(new GetWellbeingSummaryQuery(id));
  }

  /** "Ver perfil completo" / the student's own "Mi vista 360°". */
  @GetMapping("/case")
  public StudentCaseView studentCase(@PathVariable String id) {
    return getCase.handle(new GetStudentCaseQuery(id));
  }

  private static RecordWellbeingEntryCommand toCommand(
      String studentId, UUID entryId, WellbeingEntryRequest body) {
    List<DimensionInput> dimensions =
        body.dimensions().stream()
            .map(d -> new DimensionInput(d.dimension(), d.mood(), d.needs(), d.note()))
            .toList();
    return new RecordWellbeingEntryCommand(studentId, entryId, body.status(), dimensions);
  }

  private static WellbeingEntryRequest toResponse(RecordWellbeingEntryCommand command) {
    List<co.edu.icesi.student360.support.api.dto.DimensionInputRequest> dimensions =
        command.dimensions().stream()
            .map(
                d ->
                    new co.edu.icesi.student360.support.api.dto.DimensionInputRequest(
                        d.dimension(), d.mood(), d.needs(), d.note()))
            .toList();
    return new WellbeingEntryRequest(command.status(), dimensions);
  }
}
