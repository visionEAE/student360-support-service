package co.edu.icesi.student360.support.domain.port;

import co.edu.icesi.student360.support.domain.model.source.EngagementSignals;

/** Port: the interpreted engagement signals, fetched synchronously from lms-service. */
public interface LmsServiceClient {

  /**
   * @throws SourceUnavailableException when lms-service cannot be reached or answers with an error;
   *     the caller degrades
   */
  EngagementSignals fetchEngagementSignals(String studentReference);
}
