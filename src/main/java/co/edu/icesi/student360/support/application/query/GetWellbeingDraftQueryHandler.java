package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.support.application.command.DimensionInput;
import co.edu.icesi.student360.support.application.command.RecordWellbeingEntryCommand;
import co.edu.icesi.student360.support.domain.model.DimensionEntry;
import co.edu.icesi.student360.support.domain.model.EntryStatus;
import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import co.edu.icesi.student360.support.domain.port.DimensionEntryRepository;
import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import co.edu.icesi.student360.support.domain.service.StudentCaseAccessPolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/** The latest unsent entry for the caller, so the safe-space form can be restored. */
public class GetWellbeingDraftQueryHandler {

  private final WellbeingEntryRepository entries;
  private final DimensionEntryRepository dimensions;
  private final Pseudonymizer pseudonymizer;
  private final StudentCaseAccessPolicy access;

  public GetWellbeingDraftQueryHandler(
      WellbeingEntryRepository entries,
      DimensionEntryRepository dimensions,
      Pseudonymizer pseudonymizer,
      StudentCaseAccessPolicy access) {
    this.entries = entries;
    this.dimensions = dimensions;
    this.pseudonymizer = pseudonymizer;
    this.access = access;
  }

  @Audited(action = "READ_WELLBEING_DRAFT", subjectType = "STUDENT")
  @Transactional(readOnly = true)
  public Optional<RecordWellbeingEntryCommand> handle(GetWellbeingDraftQuery query) {
    access.assertIsSelf(query.studentReference());
    String pseudonym = pseudonymizer.pseudonymOf(query.studentReference());
    Optional<WellbeingEntry> draft =
        entries.findFirstByStudentPseudonymAndStatusOrderByUpdatedAtDesc(
            pseudonym, EntryStatus.DRAFT);
    if (draft.isEmpty()) {
      return Optional.empty();
    }
    WellbeingEntry entry = draft.get();
    List<DimensionEntry> dims = dimensions.findByEntryIdIn(List.of(entry.getId()));
    List<DimensionInput> inputs =
        dims.stream()
            .map(d -> new DimensionInput(d.getDimension(), d.getMood(), d.getNeeds(), d.getNote()))
            .toList();
    return Optional.of(
        new RecordWellbeingEntryCommand(
            query.studentReference(), entry.getId(), entry.getStatus(), inputs));
  }
}
