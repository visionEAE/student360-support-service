package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.EntryStatus;
import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WellbeingEntryJpaRepository
    extends JpaRepository<WellbeingEntry, java.util.UUID>, WellbeingEntryRepository {

  @Override
  List<WellbeingEntry> findByStudentPseudonymAndStatusOrderByRecordedAtDesc(
      String pseudonym, EntryStatus status);

  @Override
  Optional<WellbeingEntry> findFirstByStudentPseudonymAndStatusOrderByUpdatedAtDesc(
      String pseudonym, EntryStatus status);
}
