package co.edu.icesi.student360.support.application.query;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.AuthorizationBasis;
import co.edu.icesi.student360.common.audit.AuthorizationBasisHolder;
import co.edu.icesi.student360.support.application.SourceFetcher;
import co.edu.icesi.student360.support.application.query.model.AdvisorStudentRow;
import co.edu.icesi.student360.support.application.query.model.AdvisorStudentsOverviewView;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.AlertStatus;
import co.edu.icesi.student360.support.domain.model.EntryStatus;
import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import co.edu.icesi.student360.support.domain.model.source.StudentSummary;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import co.edu.icesi.student360.support.domain.service.RiskClassifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * "Mis estudiantes": one row per actively assigned student, composed from this service's alerts and
 * wellbeing entries plus a single batch call to core-service. A source that is down leaves that
 * column UNKNOWN for every row rather than failing the whole page.
 */
public class GetAdvisorStudentsOverviewQueryHandler {

  private final AssignmentAccessPolicy assignments;
  private final CoreServiceClient core;
  private final AlertRepository alerts;
  private final WellbeingEntryRepository wellbeingEntries;
  private final Pseudonymizer pseudonymizer;

  public GetAdvisorStudentsOverviewQueryHandler(
      AssignmentAccessPolicy assignments,
      CoreServiceClient core,
      AlertRepository alerts,
      WellbeingEntryRepository wellbeingEntries,
      Pseudonymizer pseudonymizer) {
    this.assignments = assignments;
    this.core = core;
    this.alerts = alerts;
    this.wellbeingEntries = wellbeingEntries;
    this.pseudonymizer = pseudonymizer;
  }

  @Audited(action = "LIST_ADVISOR_STUDENTS", subjectType = "ADVISOR")
  @Transactional(readOnly = true)
  public AdvisorStudentsOverviewView handle(GetAdvisorStudentsOverviewQuery query) {
    AuthorizationBasisHolder.grant(AuthorizationBasis.SELF);
    List<String> studentRefs = assignments.activelyAssignedStudents(query.advisorReference());
    if (studentRefs.isEmpty()) {
      return new AdvisorStudentsOverviewView(query.advisorReference(), List.of(), List.of());
    }

    List<String> unavailable = new ArrayList<>();
    Map<String, StudentSummary> summaries =
        SourceFetcher.fetch(
                "core-service", () -> core.fetchStudentSummaries(studentRefs), unavailable)
            .orElse(List.of())
            .stream()
            .collect(Collectors.toMap(StudentSummary::id, s -> s));

    List<Alert> activeAlerts =
        alerts.findByStudentReferenceInAndStatusInOrderByGeneratedAtDesc(
            studentRefs, List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED));
    Map<String, List<Alert>> alertsByStudent =
        activeAlerts.stream().collect(Collectors.groupingBy(Alert::getStudentReference));

    List<AdvisorStudentRow> rows =
        studentRefs.stream()
            .map(
                ref -> toRow(ref, summaries.get(ref), alertsByStudent.getOrDefault(ref, List.of())))
            .sorted(Comparator.comparing((AdvisorStudentRow row) -> riskRank(row.overallRisk())))
            .toList();
    return new AdvisorStudentsOverviewView(query.advisorReference(), rows, unavailable);
  }

  private AdvisorStudentRow toRow(
      String studentRef, StudentSummary summary, List<Alert> activeAlerts) {
    RiskClassifier.Status academic =
        summary == null
            ? RiskClassifier.Status.UNKNOWN
            : RiskClassifier.academic(summary.academicStanding());
    RiskClassifier.Status financial =
        summary == null
            ? RiskClassifier.Status.UNKNOWN
            : RiskClassifier.financial(summary.overdue(), summary.outstandingBalance());
    Optional<Integer> latestMood = latestSentLevel(studentRef);
    RiskClassifier.Status emotional = RiskClassifier.emotional(latestMood);
    RiskClassifier.Risk overall =
        RiskClassifier.overall(academic, financial, emotional, activeAlerts);
    Alert openAlert = activeAlerts.stream().findFirst().orElse(null);

    return new AdvisorStudentRow(
        studentRef,
        summary == null ? null : summary.code(),
        summary == null ? studentRef : summary.fullName(),
        initialsOf(summary == null ? studentRef : summary.fullName()),
        summary == null || summary.program() == null ? null : summary.program().name(),
        summary == null ? null : summary.currentSemester(),
        academic.name(),
        financial.name(),
        emotional.name(),
        overall.name(),
        openAlert == null ? null : openAlert.getId().toString(),
        summary == null ? null : summary.outstandingBalance(),
        summary == null ? null : summary.overdue(),
        summary == null ? null : summary.updatedAt());
  }

  private Optional<Integer> latestSentLevel(String studentReference) {
    String pseudonym = pseudonymizer.pseudonymOf(studentReference);
    return wellbeingEntries
        .findByStudentPseudonymAndStatusOrderByRecordedAtDesc(pseudonym, EntryStatus.SENT)
        .stream()
        .findFirst()
        .map(WellbeingEntry::getLevel);
  }

  private static int riskRank(String risk) {
    return switch (risk) {
      case "HIGH" -> 0;
      case "MEDIUM" -> 1;
      case "LOW" -> 2;
      default -> 3;
    };
  }

  private static String initialsOf(String fullName) {
    String[] parts = fullName.trim().split("\\s+");
    StringBuilder initials = new StringBuilder();
    for (String part : parts) {
      if (!part.isEmpty() && initials.length() < 2) {
        initials.append(Character.toUpperCase(part.charAt(0)));
      }
    }
    return initials.toString();
  }
}
