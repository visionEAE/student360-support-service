package co.edu.icesi.student360.support.domain.service;

import co.edu.icesi.student360.common.api.exception.AccessDeniedForSubjectException;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.common.identity.Identity;
import co.edu.icesi.student360.common.identity.IdentityContext;

/**
 * Who may read a student's support information: the student themself (SELF), an advisor with an
 * active assignment (ASSIGNMENT) or an admin (ADMIN_ROLE). Who may write a wellbeing entry: only
 * the student. Advisors' own writes (plans, requests, alerts, reports) go through {@link
 * AssignmentAccessPolicy}.
 */
public class StudentCaseAccessPolicy {

  static final String STUDENT = "STUDENT";
  static final String ADVISOR = "ADVISOR";
  static final String ADMIN = "ADMIN";
  static final String SUBJECT_TYPE = "STUDENT";

  private final AssignmentAccessPolicy assignments;

  public StudentCaseAccessPolicy(AssignmentAccessPolicy assignments) {
    this.assignments = assignments;
  }

  public void assertCanRead(String studentReference) {
    Identity caller = IdentityContext.require();
    if (caller.hasRole(ADMIN)) {
      AuthorizationBasisHolder.grant(AuthorizationBasis.ADMIN_ROLE);
      return;
    }
    if (caller.hasRole(STUDENT) && studentReference.equals(caller.externalReference())) {
      AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
      return;
    }
    if (caller.hasRole(ADVISOR)) {
      assignments.assertAssigned(studentReference, SUBJECT_TYPE, studentReference);
      return;
    }
    throw new AccessDeniedForSubjectException(SUBJECT_TYPE, studentReference);
  }

  /** Wellbeing is self-reported: nobody records it on a student's behalf. */
  public void assertIsSelf(String studentReference) {
    Identity caller = IdentityContext.require();
    if (caller.hasRole(STUDENT) && studentReference.equals(caller.externalReference())) {
      AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
      return;
    }
    throw new AccessDeniedForSubjectException(SUBJECT_TYPE, studentReference);
  }
}
