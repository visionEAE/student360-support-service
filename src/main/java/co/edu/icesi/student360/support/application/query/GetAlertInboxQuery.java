package co.edu.icesi.student360.support.application.query;

public record GetAlertInboxQuery(String advisorReference) {

  @Override
  public String toString() {
    return advisorReference;
  }
}
