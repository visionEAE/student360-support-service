package co.edu.icesi.student360.support.application.query;

public record GetSupportRequestsQuery(String advisorReference, String studentReference) {

  public static GetSupportRequestsQuery forAdvisor(String advisorReference) {
    return new GetSupportRequestsQuery(advisorReference, null);
  }

  public static GetSupportRequestsQuery forStudent(
      String advisorReference, String studentReference) {
    return new GetSupportRequestsQuery(advisorReference, studentReference);
  }

  @Override
  public String toString() {
    return advisorReference;
  }
}
