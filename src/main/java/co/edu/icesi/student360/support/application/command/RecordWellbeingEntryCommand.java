package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.EntryStatus;
import java.util.List;
import java.util.UUID;

/**
 * Save a draft or send an entry. {@code entryId} is null to create, or the id of an existing draft
 * to update/send it.
 */
public record RecordWellbeingEntryCommand(
    String studentReference, UUID entryId, EntryStatus status, List<DimensionInput> dimensions) {

  @Override
  public String toString() {
    return studentReference;
  }
}
