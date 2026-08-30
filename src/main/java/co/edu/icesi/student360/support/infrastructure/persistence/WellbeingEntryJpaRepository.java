package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WellbeingEntryJpaRepository
    extends JpaRepository<WellbeingEntry, UUID>, WellbeingEntryRepository {}
