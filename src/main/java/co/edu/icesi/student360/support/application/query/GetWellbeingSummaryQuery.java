package co.edu.icesi.student360.support.application.query;

public record GetWellbeingSummaryQuery(String studentReference) {

  @Override
  public String toString() {
    return studentReference;
  }
}
