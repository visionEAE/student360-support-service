package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** The relationship that authorizes an advisor to see a student. */
@Entity
@Table(name = "advisor_assignment", schema = "support")
public class AdvisorAssignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "advisor_reference", nullable = false)
  private String advisorReference;

  @Column(name = "student_reference", nullable = false)
  private String studentReference;

  @Column(name = "valid_from", nullable = false)
  private LocalDate validFrom;

  @Column(name = "valid_to")
  private LocalDate validTo;

  protected AdvisorAssignment() {}

  public boolean isActiveOn(LocalDate day) {
    return !day.isBefore(validFrom) && (validTo == null || !day.isAfter(validTo));
  }

  public String getAdvisorReference() {
    return advisorReference;
  }

  public String getStudentReference() {
    return studentReference;
  }
}
