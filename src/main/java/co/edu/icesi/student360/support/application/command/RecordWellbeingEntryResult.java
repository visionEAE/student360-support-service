package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.EntryStatus;
import java.util.UUID;

public record RecordWellbeingEntryResult(
    UUID entryId, EntryStatus status, int level, boolean alertGenerated, UUID alertId) {}
