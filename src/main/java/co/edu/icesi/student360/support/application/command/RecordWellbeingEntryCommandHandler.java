package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.common.api.exception.NotFoundException;
import co.edu.icesi.student360.common.audit.Audited;
import co.edu.icesi.student360.common.audit.RecordType;
import co.edu.icesi.student360.common.outbox.DomainEvent;
import co.edu.icesi.student360.common.outbox.EventPublisher;
import co.edu.icesi.student360.support.application.SourceFetcher;
import co.edu.icesi.student360.support.application.SupportEvents;
import co.edu.icesi.student360.support.domain.model.Alert;
import co.edu.icesi.student360.support.domain.model.DimensionEntry;
import co.edu.icesi.student360.support.domain.model.EngagementSnapshot;
import co.edu.icesi.student360.support.domain.model.EntryStatus;
import co.edu.icesi.student360.support.domain.model.FinancialSnapshot;
import co.edu.icesi.student360.support.domain.model.InterventionPlan;
import co.edu.icesi.student360.support.domain.model.RiskEvaluation;
import co.edu.icesi.student360.support.domain.model.WellbeingDimension;
import co.edu.icesi.student360.support.domain.model.WellbeingEntry;
import co.edu.icesi.student360.support.domain.port.AlertRepository;
import co.edu.icesi.student360.support.domain.port.CoreServiceClient;
import co.edu.icesi.student360.support.domain.port.DimensionEntryRepository;
import co.edu.icesi.student360.support.domain.port.InterventionPlanRepository;
import co.edu.icesi.student360.support.domain.port.LmsServiceClient;
import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import co.edu.icesi.student360.support.domain.port.WellbeingEntryRepository;
import co.edu.icesi.student360.support.domain.service.ConvergentRiskRule;
import co.edu.icesi.student360.support.domain.service.StudentCaseAccessPolicy;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * The write side of "Mi espacio seguro". A draft is stored and nothing else happens. Sending an
 * entry persists it under the student's pseudonym, emits the event and orchestrates the risk
 * evaluation with signals fetched synchronously from core-service and lms-service. Entry, alert,
 * plan and their outbox events commit together; a source that is down degrades the evaluation.
 */
public class RecordWellbeingEntryCommandHandler {

  private static final Logger log =
      LoggerFactory.getLogger(RecordWellbeingEntryCommandHandler.class);
  private static final Set<WellbeingDimension> ALL_DIMENSIONS =
      EnumSet.allOf(WellbeingDimension.class);

  private final WellbeingEntryRepository entries;
  private final DimensionEntryRepository dimensions;
  private final AlertRepository alerts;
  private final InterventionPlanRepository plans;
  private final CoreServiceClient core;
  private final LmsServiceClient lms;
  private final Pseudonymizer pseudonymizer;
  private final ConvergentRiskRule rule;
  private final StudentCaseAccessPolicy access;
  private final EventPublisher events;
  private final Clock clock;

  public RecordWellbeingEntryCommandHandler(
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
    this.entries = entries;
    this.dimensions = dimensions;
    this.alerts = alerts;
    this.plans = plans;
    this.core = core;
    this.lms = lms;
    this.pseudonymizer = pseudonymizer;
    this.rule = rule;
    this.access = access;
    this.events = events;
    this.clock = clock;
  }

  @Audited(
      action = "RECORD_WELLBEING_ENTRY",
      subjectType = "STUDENT",
      recordType = RecordType.STATE_CHANGE)
  @Transactional
  public RecordWellbeingEntryResult handle(RecordWellbeingEntryCommand command) {
    access.assertIsSelf(command.studentReference());
    validate(command);
    Instant now = clock.instant();
    String pseudonym = pseudonymizer.pseudonymOf(command.studentReference());
    int level = command.dimensions().stream().mapToInt(d -> d.mood().level()).min().orElse(4);

    WellbeingEntry entry;
    if (command.entryId() == null) {
      entry = entries.save(WellbeingEntry.start(pseudonym, level, command.status(), now));
    } else {
      entry =
          entries
              .findById(command.entryId())
              .filter(existing -> existing.getStudentPseudonym().equals(pseudonym))
              .orElseThrow(
                  () -> new NotFoundException("Wellbeing entry", command.entryId().toString()));
      if (entry.isSent()) {
        throw new EntryAlreadySentException(entry.getId().toString());
      }
      entry.update(level, command.status(), now);
      dimensions.deleteByEntryId(entry.getId());
    }
    dimensions.saveDimensions(
        command.dimensions().stream()
            .map(
                d -> DimensionEntry.of(entry.getId(), d.dimension(), d.mood(), d.needs(), d.note()))
            .toList());
    if (!entry.isSent()) {
      return new RecordWellbeingEntryResult(entry.getId(), entry.getStatus(), level, false, null);
    }

    events.publish(
        new DomainEvent(
            SupportEvents.WELLBEING_ENTRY_RECORDED,
            SupportEvents.AGGREGATE_STUDENT,
            command.studentReference(),
            now,
            Map.of(
                "entryId", entry.getId().toString(),
                "level", level,
                "moods", moods(command),
                "needs", needs(command))));
    // Free-text notes are deliberately absent from the event and from every log line.
    log.info("Wellbeing entry {} sent (level {})", entry.getId(), level);

    RiskEvaluation evaluation = evaluate(command.studentReference(), level);
    if (!evaluation.firesAlert()) {
      return new RecordWellbeingEntryResult(entry.getId(), entry.getStatus(), level, false, null);
    }
    Alert alert =
        alerts.save(
            Alert.generate(
                command.studentReference(),
                evaluation.severity().orElseThrow(),
                ConvergentRiskRule.SOURCE,
                evaluation.signals(),
                now));
    events.publish(
        new DomainEvent(
            SupportEvents.ALERT_GENERATED,
            SupportEvents.AGGREGATE_ALERT,
            alert.getId().toString(),
            now,
            Map.of(
                "studentReference", command.studentReference(),
                "severity", alert.getSeverity().name(),
                "source", alert.getSource(),
                "firedConditions", evaluation.signals().firedConditions())));
    InterventionPlan plan =
        plans.save(
            InterventionPlan.propose(
                alert.getId(),
                command.studentReference(),
                evaluation.suggestedPlan().orElseThrow(),
                PlanDescriptions.describe(evaluation.suggestedPlan().orElseThrow()),
                now));
    events.publish(
        new DomainEvent(
            SupportEvents.INTERVENTION_PLAN_CREATED,
            SupportEvents.AGGREGATE_PLAN,
            plan.getId().toString(),
            now,
            Map.of(
                "alertId", alert.getId().toString(),
                "studentReference", command.studentReference(),
                "type", plan.getType().name())));
    log.warn(
        "Alert {} ({}) generated for student {} by {}",
        alert.getId(),
        alert.getSeverity(),
        command.studentReference(),
        evaluation.signals().firedConditions());
    return new RecordWellbeingEntryResult(
        entry.getId(), entry.getStatus(), level, true, alert.getId());
  }

  private static void validate(RecordWellbeingEntryCommand command) {
    if (command.dimensions() == null || command.dimensions().isEmpty()) {
      throw new InvalidCommandException("At least one dimension is required");
    }
    Set<WellbeingDimension> given =
        command.dimensions().stream().map(DimensionInput::dimension).collect(Collectors.toSet());
    if (given.size() != command.dimensions().size()) {
      throw new InvalidCommandException("Each dimension may appear once");
    }
    if (command.status() == EntryStatus.SENT && !given.containsAll(ALL_DIMENSIONS)) {
      throw new InvalidCommandException("All three dimensions are required to send an entry");
    }
  }

  private static Map<String, String> moods(RecordWellbeingEntryCommand command) {
    return command.dimensions().stream()
        .collect(Collectors.toMap(d -> d.dimension().name(), d -> d.mood().name()));
  }

  private static Map<String, List<String>> needs(RecordWellbeingEntryCommand command) {
    return command.dimensions().stream()
        .collect(Collectors.toMap(d -> d.dimension().name(), DimensionInput::needs));
  }

  private RiskEvaluation evaluate(String studentReference, int level) {
    List<String> unavailable = new ArrayList<>();
    Optional<EngagementSnapshot> engagement =
        SourceFetcher.fetch(
                "lms-service", () -> lms.fetchEngagementSignals(studentReference), unavailable)
            .map(EngagementSnapshot::from);
    Optional<FinancialSnapshot> financial =
        SourceFetcher.fetch(
                "core-service", () -> core.fetchFinancialStatus(studentReference), unavailable)
            .map(FinancialSnapshot::from);
    return rule.evaluate(level, engagement, financial, unavailable);
  }
}
