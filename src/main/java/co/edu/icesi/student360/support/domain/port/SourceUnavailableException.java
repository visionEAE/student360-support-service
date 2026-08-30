package co.edu.icesi.student360.support.domain.port;

/** A source service could not provide its signal. Not a domain error: the rule degrades. */
public class SourceUnavailableException extends RuntimeException {

  private final String source;

  public SourceUnavailableException(String source, Throwable cause) {
    super(source + " unavailable", cause);
    this.source = source;
  }

  public String source() {
    return source;
  }
}
