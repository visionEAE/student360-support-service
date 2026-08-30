package co.edu.icesi.student360.support.application.query;

public record GetAdvisorStudentsOverviewQuery(String advisorReference) {

  @Override
  public String toString() {
    return advisorReference;
  }
}
