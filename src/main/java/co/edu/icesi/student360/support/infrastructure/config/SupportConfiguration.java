package co.edu.icesi.student360.support.infrastructure.config;

import co.edu.icesi.student360.common.authorization.StudentRecordAccessPolicy;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.domain.port.AdvisorAssignmentRepository;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.port.LmsServiceClient;
import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import co.edu.icesi.student360.support.domain.port.SupportReportRepository;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import co.edu.icesi.student360.support.domain.service.AlertService;
import co.edu.icesi.student360.support.domain.service.AssignmentAccessPolicy;
import co.edu.icesi.student360.support.domain.service.ConvergentRiskRule;
import co.edu.icesi.student360.support.domain.service.RiskRuleThresholds;
import co.edu.icesi.student360.support.domain.service.WellbeingService;
import co.edu.icesi.student360.support.infrastructure.security.HmacPseudonymizer;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Binds each port to its stage 1 adapter and assembles the domain services. */
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
  public AssignmentAccessPolicy assignmentAccessPolicy(
      AdvisorAssignmentRepository assignments, Clock clock) {
    return new AssignmentAccessPolicy(assignments, clock);
  }

  @Bean
  public WellbeingService wellbeingService(
      WellbeingEntryRepository entries,
      AlertRepository alerts,
      InterventionPlanRepository plans,
      CoreServiceClient core,
      LmsServiceClient lms,
      Pseudonymizer pseudonymizer,
      ConvergentRiskRule rule,
      StudentRecordAccessPolicy accessPolicy,
      EventPublisher events,
      Clock clock) {
    return new WellbeingService(
        entries, alerts, plans, core, lms, pseudonymizer, rule, accessPolicy, events, clock);
  }

  @Bean
  public AlertService alertService(
      AlertRepository alerts,
      InterventionPlanRepository plans,
      SupportReportRepository reports,
      AssignmentAccessPolicy assignmentPolicy,
      Clock clock) {
    return new AlertService(alerts, plans, reports, assignmentPolicy, clock);
  }
}
