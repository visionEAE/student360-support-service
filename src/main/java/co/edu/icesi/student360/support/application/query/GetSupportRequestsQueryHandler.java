package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.support.application.query.model.SupportRequestView;
import co.edu.icesi.student360.support.domain.port.SupportRequestRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public class GetSupportRequestsQueryHandler {

  private final SupportRequestRepository requests;
  private final AssignmentAccessPolicy assignments;

  public GetSupportRequestsQueryHandler(
      SupportRequestRepository requests, AssignmentAccessPolicy assignments) {
    this.requests = requests;
    this.assignments = assignments;
  }

  @Audited(action = "LIST_SUPPORT_REQUESTS", subjectType = "ADVISOR")
  @Transactional(readOnly = true)
  public List<SupportRequestView> handle(GetSupportRequestsQuery query) {
    AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
    List<String> scope;
    if (query.studentReference() != null) {
      assignments.assertAssigned(query.studentReference(), "STUDENT", query.studentReference());
      scope = List.of(query.studentReference());
    } else {
      scope = assignments.activelyAssignedStudents(query.advisorReference());
    }
    if (scope.isEmpty()) {
      return List.of();
    }
    return requests.findByStudentReferenceInOrderByCreatedAtDesc(scope).stream()
        .map(GetSupportRequestsQueryHandler::toView)
        .toList();
  }

  static SupportRequestView toView(
      co.edu.icesi.student360.support.domain.model.SupportRequest request) {
    return new SupportRequestView(
        request.getId().toString(),
        request.getStudentReference(),
        request.getAlertId() == null ? null : request.getAlertId().toString(),
        request.getType().name(),
        request.getDescription(),
        request.getStatus().name(),
        request.getResolution(),
        request.getCreatedBy(),
        request.getCreatedAt(),
        request.getUpdatedAt());
  }
}
