package co.edu.icesi.student360.support.domain.service;

import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.authorization.StudentRecordAccessPolicy;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.EngagementSnapshot;
import co.edu.icesi.student360.support.domain.model.FinancialSnapshot;
import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import co.edu.icesi.student360.support.domain.model.RiskEvaluation;
import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.port.LmsServiceClient;
import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import co.edu.icesi.student360.support.domain.port.SourceUnavailableException;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records a wellbeing entry and, when it is low, orchestrates the risk evaluation: the entry is
 * combined with signals fetched synchronously from core-service and lms-service, and the rule
 * decides whether an alert with a suggested intervention plan is generated. Entry, alert, plan and
 * their outbox events commit together.
 */
public class WellbeingService {

  public static final String WELLBEING_ENTRY_RECORDED = "WELLBEING_ENTRY_RECORDED";
  public static final String ALERT_GENERATED = "ALERT_GENERATED";
  public static final String INTERVENTION_PLAN_CREATED = "INTERVENTION_PLAN_CREATED";
  static final String AGGREGATE_STUDENT = "STUDENT";
  static final String AGGREGATE_ALERT = "ALERT";

  private static final Logger log = LoggerFactory.getLogger(WellbeingService.class);

  private final WellbeingEntryRepository entries;
  private final AlertRepository alerts;
  private final InterventionPlanRepository plans;
  private final CoreServiceClient core;
  private final LmsServiceClient lms;
  private final Pseudonymizer pseudonymizer;
  private final ConvergentRiskRule rule;
  private final StudentRecordAccessPolicy accessPolicy;
  private final EventPublisher events;
  private final Clock clock;

  public WellbeingService(
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
    this.entries = entries;
    this.alerts = alerts;
    this.plans = plans;
    this.core = core;
    this.lms = lms;
    this.pseudonymizer = pseudonymizer;
    this.rule = rule;
    this.accessPolicy = accessPolicy;
    this.events = events;
    this.clock = clock;
  }

  /** Returns the generated alert, if the rule fired. */
  @Audited(
      action = "RECORD_WELLBEING_ENTRY",
      subjectType = "STUDENT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public Optional<Alert> recordEntry(String studentReference, int level, String comment) {
    accessPolicy.assertCanRead(studentReference);
    Instant now = clock.instant();
    WellbeingEntry entry =
        entries.save(
            WellbeingEntry.record(
                pseudonymizer.pseudonymOf(studentReference), level, comment, now));
    events.publish(
        new DomainEvent(
            WELLBEING_ENTRY_RECORDED,
            AGGREGATE_STUDENT,
            studentReference,
            now,
            Map.of("entryId", entry.getId().toString(), "level", level)));
    // The free-text comment is deliberately absent from the event and from every log line.
    log.info("Wellbeing entry {} recorded (level {})", entry.getId(), level);

    RiskEvaluation evaluation = evaluate(studentReference, level);
    if (!evaluation.firesAlert()) {
      return Optional.empty();
    }
    Alert alert =
        alerts.save(
            Alert.generate(
                studentReference,
                evaluation.severity().orElseThrow(),
                ConvergentRiskRule.SOURCE,
                evaluation.signals(),
                now));
    events.publish(
        new DomainEvent(
            ALERT_GENERATED,
            AGGREGATE_ALERT,
            alert.getId().toString(),
            now,
            Map.of(
                "studentReference", studentReference,
                "severity", alert.getSeverity().name(),
                "source", alert.getSource(),
                "firedConditions", evaluation.signals().firedConditions())));
    InterventionPlan plan =
        plans.save(
            InterventionPlan.propose(
                alert.getId(), evaluation.suggestedPlan().orElseThrow(), describe(evaluation)));
    events.publish(
        new DomainEvent(
            INTERVENTION_PLAN_CREATED,
            AGGREGATE_ALERT,
            alert.getId().toString(),
            now,
            Map.of("planId", plan.getId().toString(), "type", plan.getType().name())));
    log.warn(
        "Alert {} ({}) generated for student {} by {}",
        alert.getId(),
        alert.getSeverity(),
        studentReference,
        evaluation.signals().firedConditions());
    return Optional.of(alert);
  }

  private RiskEvaluation evaluate(String studentReference, int level) {
    List<String> unavailable = new ArrayList<>();
    Optional<EngagementSnapshot> engagement =
        fetch("lms-service", () -> lms.fetchEngagementSignals(studentReference), unavailable);
    Optional<FinancialSnapshot> financial =
        fetch("core-service", () -> core.fetchFinancialStatus(studentReference), unavailable);
    return rule.evaluate(level, engagement, financial, unavailable);
  }

  /** A source that is down degrades the evaluation; it never fails the entry. */
  private static <T> Optional<T> fetch(String source, Supplier<T> call, List<String> unavailable) {
    try {
      return Optional.ofNullable(call.get());
    } catch (SourceUnavailableException exception) {
      log.warn("{} unavailable during risk evaluation: {}", source, exception.getMessage());
      unavailable.add(source);
      return Optional.empty();
    }
  }

  private static String describe(RiskEvaluation evaluation) {
    return switch (evaluation.suggestedPlan().orElseThrow()) {
      case INTEGRAL_SUPPORT ->
          "Joint session with the advisor, the financial aid office and the wellbeing team within"
              + " 5 working days; reconnect with course activity and agree on a payment plan.";
      case ACADEMIC_FOLLOW_UP ->
          "Advisor check-in within 10 working days; review course workload and recent"
              + " submissions together.";
    };
  }
}
