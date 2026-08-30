package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.api.exception.DomainException;
import org.springframework.http.HttpStatus;

/** A sent entry is part of the record; it is not edited. */
public class EntryAlreadySentException extends DomainException {

  public EntryAlreadySentException(String entryId) {
    super(
        HttpStatus.CONFLICT,
        "Entry already sent",
        "Wellbeing entry " + entryId + " was already sent");
  }
}
