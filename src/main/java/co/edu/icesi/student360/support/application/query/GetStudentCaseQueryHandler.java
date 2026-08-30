package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.support.application.SourceFetcher;
import co.edu.icesi.student360.support.application.query.model.StudentCaseView;
import co.edu.icesi.student360.support.domain.model.AdvisorAssignment;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.AlertStatus;
import co.edu.icesi.student360.support.domain.model.source.AcademicStatus;
import co.edu.icesi.student360.support.domain.model.source.EngagementSignals;
import co.edu.icesi.student360.support.domain.model.source.FinancialStatus;
import co.edu.icesi.student360.support.domain.model.source.StudentProfile;
import co.edu.icesi.student360.support.domain.port.AdvisorAssignmentRepository;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.port.LmsServiceClient;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import co.edu.icesi.student360.support.domain.port.SupportRequestRepository;
import co.edu.icesi.student360.support.domain.service.StudentCaseAccessPolicy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Vista 360°" / "Ver perfil completo": everything about one student, composed synchronously from
 * core-service, lms-service and this service's own tables. Every section degrades independently.
 */
public class GetStudentCaseQueryHandler {

  private final StudentCaseAccessPolicy access;
  private final CoreServiceClient core;
  private final LmsServiceClient lms;
  private final AdvisorAssignmentRepository assignmentRepository;
  private final AlertRepository alerts;
  private final InterventionPlanRepository plans;
  private final SupportReportRepository reports;
  private final SupportRequestRepository requests;
  private final GetAlertDetailQueryHandler alertDetail;
  private final GetWellbeingSummaryQueryHandler wellbeingSummary;

  public GetStudentCaseQueryHandler(
      StudentCaseAccessPolicy access,
      CoreServiceClient core,
      LmsServiceClient lms,
      AdvisorAssignmentRepository assignmentRepository,
      AlertRepository alerts,
      InterventionPlanRepository plans,
      SupportReportRepository reports,
      SupportRequestRepository requests,
      GetAlertDetailQueryHandler alertDetail,
      GetWellbeingSummaryQueryHandler wellbeingSummary) {
    this.access = access;
    this.core = core;
    this.lms = lms;
    this.assignmentRepository = assignmentRepository;
    this.alerts = alerts;
    this.plans = plans;
    this.reports = reports;
    this.requests = requests;
    this.alertDetail = alertDetail;
    this.wellbeingSummary = wellbeingSummary;
  }

  @Audited(action = "READ_STUDENT_CASE", subjectType = "STUDENT")
  @Transactional(readOnly = true)
  public StudentCaseView handle(GetStudentCaseQuery query) {
    access.assertCanRead(query.studentReference());
    String ref = query.studentReference();
    List<String> unavailable = new ArrayList<>();

    StudentProfile profile =
        SourceFetcher.fetch("core-service", () -> core.fetchStudentProfile(ref), unavailable)
            .orElse(null);
    AcademicStatus academic =
        SourceFetcher.fetch("core-service", () -> core.fetchAcademicStatus(ref), unavailable)
            .orElse(null);
    FinancialStatus financial =
        SourceFetcher.fetch("core-service", () -> core.fetchFinancialStatus(ref), unavailable)
            .orElse(null);
    EngagementSignals engagement =
        SourceFetcher.fetch("lms-service", () -> lms.fetchEngagementSignals(ref), unavailable)
            .orElse(null);

    StudentCaseView.AssignmentView assignmentView = latestAssignment(ref);
    var active =
        alerts.findByStudentReferenceInAndStatusInOrderByGeneratedAtDesc(
            List.of(ref), List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED));
    var activeAlertView = active.isEmpty() ? null : toAlertDetailView(active.get(0));

    return new StudentCaseView(
        profile,
        assignmentView,
        academic,
        financial,
        engagement,
        activeAlertView,
        wellbeingSummary.buildWithoutAuthorization(ref),
        requests.findByStudentReferenceInOrderByCreatedAtDesc(List.of(ref)).stream()
            .map(GetSupportRequestsQueryHandler::toView)
            .toList(),
        active.isEmpty()
            ? List.of()
            : reports.findByAlertIdOrderByCreatedAtDesc(active.get(0).getId()).stream()
                .map(r -> GetAlertDetailQueryHandler.toReportView(r, ref))
                .toList(),
        unavailable);
  }

  private co.edu.icesi.student360.support.application.query.model.AlertDetailView toAlertDetailView(
      Alert alert) {
    var plan =
        plans
            .findFirstByAlertIdOrderByCreatedAtDesc(alert.getId())
            .map(alertDetail::toPlanView)
            .orElse(null);
    var reportViews =
        reports.findByAlertIdOrderByCreatedAtDesc(alert.getId()).stream()
            .map(r -> GetAlertDetailQueryHandler.toReportView(r, alert.getStudentReference()))
            .toList();
    return new co.edu.icesi.student360.support.application.query.model.AlertDetailView(
        alert.getId().toString(),
        alert.getStudentReference(),
        alert.getSeverity().name(),
        alert.getStatus().name(),
        alert.getSource(),
        alert.getGeneratedAt(),
        alert.getTriggeringSignals(),
        plan,
        reportViews);
  }

  private StudentCaseView.AssignmentView latestAssignment(String studentReference) {
    LocalDate today = LocalDate.now();
    Optional<AdvisorAssignment> current =
        assignmentRepository
            .findByAdvisorReferenceAndStudentReference("", studentReference)
            .stream()
            .filter(a -> a.isActiveOn(today))
            .findFirst();
    return current
        .map(a -> new StudentCaseView.AssignmentView(a.getAdvisorReference(), null))
        .orElse(null);
  }
}
