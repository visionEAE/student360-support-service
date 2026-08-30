package co.edu.icesi.student360.support.infrastructure.config;

import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.command.AddSupportReportCommandHandler;
import co.edu.icesi.student360.support.application.command.CreateInterventionPlanCommandHandler;
import co.edu.icesi.student360.support.application.command.CreateManualAlertCommandHandler;
import co.edu.icesi.student360.support.application.command.CreateSupportRequestCommandHandler;
import co.edu.icesi.student360.support.application.command.RecordWellbeingEntryCommandHandler;
import co.edu.icesi.student360.support.application.command.UpdateAlertStatusCommandHandler;
import co.edu.icesi.student360.support.application.command.UpdateInterventionPlanStatusCommandHandler;
import co.edu.icesi.student360.support.application.command.UpdateSupportRequestStatusCommandHandler;
import co.edu.icesi.student360.support.application.query.GetAdvisorStudentsOverviewQueryHandler;
import co.edu.icesi.student360.support.application.query.GetAlertDetailQueryHandler;
import co.edu.icesi.student360.support.application.query.GetAlertInboxQueryHandler;
import co.edu.icesi.student360.support.application.query.GetInterventionPlansQueryHandler;
import co.edu.icesi.student360.support.application.query.GetStudentCaseQueryHandler;
import co.edu.icesi.student360.support.application.query.GetSupportReportsQueryHandler;
import co.edu.icesi.student360.support.application.query.GetSupportRequestsQueryHandler;
import co.edu.icesi.student360.support.application.query.GetWellbeingDraftQueryHandler;
import co.edu.icesi.student360.support.application.query.GetWellbeingSummaryQueryHandler;
import co.edu.icesi.student360.support.domain.port.AdvisorAssignmentRepository;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.DimensionEntryRepository;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.port.LmsServiceClient;
import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import co.edu.icesi.student360.support.domain.port.SupportRequestRepository;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import co.edu.icesi.student360.support.domain.service.ConvergentRiskRule;
import co.edu.icesi.student360.support.domain.service.RiskRuleThresholds;
import co.edu.icesi.student360.support.domain.service.StudentCaseAccessPolicy;
import co.edu.icesi.student360.support.domain.service.WellbeingSummaryCalculator;
import co.edu.icesi.student360.support.infrastructure.security.HmacPseudonymizer;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Binds each port to its stage 1 adapter and wires every CQRS handler. */
@Configuration
public class SupportConfiguration {

  @Bean
  public Pseudonymizer pseudonymizer(SupportProperties properties) {
    return new HmacPseudonymizer(properties.pseudonymSecret());
  }

  @Bean
  public ConvergentRiskRule convergentRiskRule(SupportProperties properties) {
    SupportProperties.Rule rule = properties.rule();
    return new ConvergentRiskRule(
        new RiskRuleThresholds(
            rule.lowWellbeingLevel(), rule.maxDaysSinceAccess(), rule.minOnTimeRate()));
  }

  @Bean
  public WellbeingSummaryCalculator wellbeingSummaryCalculator(Clock clock) {
    return new WellbeingSummaryCalculator(clock);
  }

  @Bean
  public AssignmentAccessPolicy assignmentAccessPolicy(
      AdvisorAssignmentRepository assignments, Clock clock) {
    return new AssignmentAccessPolicy(assignments, clock);
  }

  @Bean
  public StudentCaseAccessPolicy studentCaseAccessPolicy(AssignmentAccessPolicy assignments) {
    return new StudentCaseAccessPolicy(assignments);
  }

  // ---- commands ----

  @Bean
  public RecordWellbeingEntryCommandHandler recordWellbeingEntryCommandHandler(
      WellbeingEntryRepository entries,
      DimensionEntryRepository dimensions,
      AlertRepository alerts,
      InterventionPlanRepository plans,
      CoreServiceClient core,
      LmsServiceClient lms,
      Pseudonymizer pseudonymizer,
      ConvergentRiskRule rule,
      StudentCaseAccessPolicy access,
      EventPublisher events,
      Clock clock) {
    return new RecordWellbeingEntryCommandHandler(
        entries, dimensions, alerts, plans, core, lms, pseudonymizer, rule, access, events, clock);
  }

  @Bean
  public CreateInterventionPlanCommandHandler createInterventionPlanCommandHandler(
      InterventionPlanRepository plans,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    return new CreateInterventionPlanCommandHandler(plans, assignments, events, clock);
  }

  @Bean
  public UpdateInterventionPlanStatusCommandHandler updateInterventionPlanStatusCommandHandler(
      InterventionPlanRepository plans,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    return new UpdateInterventionPlanStatusCommandHandler(plans, assignments, events, clock);
  }

  @Bean
  public AddSupportReportCommandHandler addSupportReportCommandHandler(
      AlertRepository alerts,
      SupportReportRepository reports,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    return new AddSupportReportCommandHandler(alerts, reports, assignments, events, clock);
  }

  @Bean
  public CreateManualAlertCommandHandler createManualAlertCommandHandler(
      AlertRepository alerts,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    return new CreateManualAlertCommandHandler(alerts, assignments, events, clock);
  }

  @Bean
  public UpdateAlertStatusCommandHandler updateAlertStatusCommandHandler(
      AlertRepository alerts,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    return new UpdateAlertStatusCommandHandler(alerts, assignments, events, clock);
  }

  @Bean
  public CreateSupportRequestCommandHandler createSupportRequestCommandHandler(
      SupportRequestRepository requests,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    return new CreateSupportRequestCommandHandler(requests, assignments, events, clock);
  }

  @Bean
  public UpdateSupportRequestStatusCommandHandler updateSupportRequestStatusCommandHandler(
      SupportRequestRepository requests,
      AssignmentAccessPolicy assignments,
      EventPublisher events,
      Clock clock) {
    return new UpdateSupportRequestStatusCommandHandler(requests, assignments, events, clock);
  }

  // ---- queries ----

  @Bean
  public GetWellbeingDraftQueryHandler getWellbeingDraftQueryHandler(
      WellbeingEntryRepository entries,
      DimensionEntryRepository dimensions,
      Pseudonymizer pseudonymizer,
      StudentCaseAccessPolicy access) {
    return new GetWellbeingDraftQueryHandler(entries, dimensions, pseudonymizer, access);
  }

  @Bean
  public GetWellbeingSummaryQueryHandler getWellbeingSummaryQueryHandler(
      WellbeingEntryRepository entries,
      DimensionEntryRepository dimensions,
      Pseudonymizer pseudonymizer,
      StudentCaseAccessPolicy access,
      WellbeingSummaryCalculator calculator) {
    return new GetWellbeingSummaryQueryHandler(
        entries, dimensions, pseudonymizer, access, calculator);
  }

  @Bean
  public GetAlertInboxQueryHandler getAlertInboxQueryHandler(
      AlertRepository alerts, AssignmentAccessPolicy assignments) {
    return new GetAlertInboxQueryHandler(alerts, assignments);
  }

  @Bean
  public GetAlertDetailQueryHandler getAlertDetailQueryHandler(
      AlertRepository alerts,
      InterventionPlanRepository plans,
      SupportReportRepository reports,
      AssignmentAccessPolicy assignments) {
    return new GetAlertDetailQueryHandler(alerts, plans, reports, assignments);
  }

  @Bean
  public GetInterventionPlansQueryHandler getInterventionPlansQueryHandler(
      InterventionPlanRepository plans, AssignmentAccessPolicy assignments) {
    return new GetInterventionPlansQueryHandler(plans, assignments);
  }

  @Bean
  public GetSupportReportsQueryHandler getSupportReportsQueryHandler(
      SupportReportRepository reports, AlertRepository alerts) {
    return new GetSupportReportsQueryHandler(reports, alerts);
  }

  @Bean
  public GetSupportRequestsQueryHandler getSupportRequestsQueryHandler(
      SupportRequestRepository requests, AssignmentAccessPolicy assignments) {
    return new GetSupportRequestsQueryHandler(requests, assignments);
  }

  @Bean
  public GetAdvisorStudentsOverviewQueryHandler getAdvisorStudentsOverviewQueryHandler(
      AssignmentAccessPolicy assignments,
      CoreServiceClient core,
      AlertRepository alerts,
      WellbeingEntryRepository wellbeingEntries,
      Pseudonymizer pseudonymizer) {
    return new GetAdvisorStudentsOverviewQueryHandler(
        assignments, core, alerts, wellbeingEntries, pseudonymizer);
  }

  @Bean
  public GetStudentCaseQueryHandler getStudentCaseQueryHandler(
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
    return new GetStudentCaseQueryHandler(
        access,
        core,
        lms,
        assignmentRepository,
        alerts,
        plans,
        reports,
        requests,
        alertDetail,
        wellbeingSummary);
  }
}
