package co.edu.icesi.student360.support.domain.model.source;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** core-service {@code GET /students/{id}/academic-status}, relayed as-is. */
public record AcademicStatus(
    String studentId,
    String currentTerm,
    Integer currentSemester,
    Integer totalSemesters,
    String academicStanding,
    String enrollmentStatus,
    BigDecimal cumulativeGpa,
    Integer creditsEnrolled,
    List<GpaPoint> gpaHistory,
    List<Course> currentCourses,
    Instant sourceUpdatedAt) {

  public record GpaPoint(Integer semester, String term, BigDecimal termGpa) {}

  public record Course(String code, String name, Integer credits, BigDecimal currentGrade) {}
}
