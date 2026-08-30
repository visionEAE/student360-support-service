package co.edu.icesi.student360.support.infrastructure.persistence;

import co.edu.icesi.student360.support.domain.model.DimensionEntry;
import co.edu.icesi.student360.support.domain.port.DimensionEntryRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DimensionEntryJpaRepository
    extends JpaRepository<DimensionEntry, Long>, DimensionEntryRepository {

  @Override
  default List<DimensionEntry> saveDimensions(Iterable<DimensionEntry> entries) {
    return saveAll(entries);
  }

  @Override
  List<DimensionEntry> findByEntryIdIn(Collection<UUID> entryIds);

  @Override
  @Modifying
  @Query("delete from DimensionEntry d where d.entryId = :entryId")
  void deleteByEntryId(@Param("entryId") UUID entryId);
}
