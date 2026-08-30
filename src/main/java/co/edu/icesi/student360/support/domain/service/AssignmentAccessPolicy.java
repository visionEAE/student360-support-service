package co.edu.icesi.student360.support.domain.service;

import co.edu.icesi.student360.common.api.exception.AccessDeniedForSubjectException;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.common.identity.Identity;
import co.edu.icesi.student360.common.identity.IdentityContext;
import co.edu.icesi.student360.support.domain.model.AdvisorAssignment;
import co.edu.icesi.student360.support.domain.port.AdvisorAssignmentRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Fine-grained authorization for advisors: an advisor may see a student only while holding an
 * <em>active</em> assignment to them. The basis recorded is ASSIGNMENT — the fact that answers an
 * improper-access complaint months later. Admins pass by role.
 */
public class AssignmentAccessPolicy {

  static final String ADMIN = "ADMIN";

  private final AdvisorAssignmentRepository assignments;
  private final Clock clock;

  public AssignmentAccessPolicy(AdvisorAssignmentRepository assignments, Clock clock) {
    this.assignments = assignments;
    this.clock = clock;
  }

  public void assertAssigned(String studentReference, String subjectType, String subjectId) {
    Identity caller = IdentityContext.require();
    if (caller.hasRole(ADMIN)) {
      AuthorizationBasisHolder.grant(AuthorizationBasis.ADMIN_ROLE);
      return;
    }
    if (caller.externalReference() != null
        && hasActiveAssignment(caller.externalReference(), studentReference)) {
      AuthorizationBasisHolder.grant(AuthorizationBasis.ASSIGNMENT);
      return;
    }
    throw new AccessDeniedForSubjectException(subjectType, subjectId);
  }

  public List<String> activelyAssignedStudents(String advisorReference) {
    LocalDate today = LocalDate.now(clock);
    return assignments.findByAdvisorReference(advisorReference).stream()
        .filter(assignment -> assignment.isActiveOn(today))
        .map(AdvisorAssignment::getStudentReference)
        .distinct()
        .toList();
  }

  private boolean hasActiveAssignment(String advisorReference, String studentReference) {
    LocalDate today = LocalDate.now(clock);
    return assignments
        .findByAdvisorReferenceAndStudentReference(advisorReference, studentReference)
        .stream()
        .anyMatch(assignment -> assignment.isActiveOn(today));
  }
}
