package co.edu.icesi.student360.support.domain.model.source;

/** core-service {@code GET /students/{id}}, relayed as-is. */
public record StudentProfile(
    String id,
    String code,
    String fullName,
    String firstName,
    String lastName,
    String email,
    Program program,
    Integer currentSemester,
    String admissionTerm,
    String status,
    String enrollmentStatus) {

  public record Program(String code, String name, String faculty, Integer totalSemesters) {}
}
