package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.EntryStatus;
import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WellbeingEntryRepository {

  WellbeingEntry save(WellbeingEntry entry);

  Optional<WellbeingEntry> findById(UUID id);

  /** Newest first. */
  List<WellbeingEntry> findByStudentPseudonymAndStatusOrderByRecordedAtDesc(
      String pseudonym, EntryStatus status);

  Optional<WellbeingEntry> findFirstByStudentPseudonymAndStatusOrderByUpdatedAtDesc(
      String pseudonym, EntryStatus status);
}
