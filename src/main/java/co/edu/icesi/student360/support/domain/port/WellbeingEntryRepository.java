package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.WellbeingEntry;

public interface WellbeingEntryRepository {

  WellbeingEntry save(WellbeingEntry entry);
}
