package co.edu.icesi.student360.support.application.query.model;

import co.edu.icesi.student360.support.domain.model.source.AcademicStatus;
import co.edu.icesi.student360.support.domain.model.source.EngagementSignals;
import co.edu.icesi.student360.support.domain.model.source.FinancialStatus;
import co.edu.icesi.student360.support.domain.model.source.StudentProfile;
import java.util.List;

public record StudentCaseView(
    StudentProfile student,
    AssignmentView assignment,
    AcademicStatus academic,
    FinancialStatus financial,
    EngagementSignals engagement,
    AlertDetailView activeAlert,
    WellbeingSummaryView wellbeing,
    List<SupportRequestView> requests,
    List<SupportReportView> reports,
    List<String> unavailableSources) {

  public record AssignmentView(String advisorReference, String validFrom) {}
}
