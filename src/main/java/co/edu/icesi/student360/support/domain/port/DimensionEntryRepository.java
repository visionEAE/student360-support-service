package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.DimensionEntry;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DimensionEntryRepository {

  List<DimensionEntry> saveDimensions(Iterable<DimensionEntry> entries);

  List<DimensionEntry> findByEntryIdIn(Collection<UUID> entryIds);

  void deleteByEntryId(UUID entryId);
}
