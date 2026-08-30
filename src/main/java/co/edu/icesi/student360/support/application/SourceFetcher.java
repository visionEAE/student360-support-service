package co.edu.icesi.student360.support.application;

import co.edu.icesi.student360.support.domain.port.SourceUnavailableException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial degradation in one place: a source that is down yields an empty section and its name in
 * the list the caller reports, never a failed request.
 */
public final class SourceFetcher {

  private static final Logger log = LoggerFactory.getLogger(SourceFetcher.class);

  private SourceFetcher() {}

  public static <T> Optional<T> fetch(String source, Supplier<T> call, List<String> unavailable) {
    try {
      return Optional.ofNullable(call.get());
    } catch (SourceUnavailableException exception) {
      log.warn("{} unavailable: {}", source, exception.getMessage());
      if (!unavailable.contains(source)) {
        unavailable.add(source);
      }
      return Optional.empty();
    }
  }
}
