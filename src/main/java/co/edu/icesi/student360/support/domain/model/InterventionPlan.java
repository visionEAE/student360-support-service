package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "intervention_plan", schema = "support")
public class InterventionPlan {

  @Id private UUID id;

  @Column(name = "alert_id", nullable = false)
  private UUID alertId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InterventionType type;

  @Column(nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlanStatus status;

  protected InterventionPlan() {}

  public static InterventionPlan propose(UUID alertId, InterventionType type, String description) {
    InterventionPlan plan = new InterventionPlan();
    plan.id = UUID.randomUUID();
    plan.alertId = alertId;
    plan.type = type;
    plan.description = description;
    plan.status = PlanStatus.PROPOSED;
    return plan;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAlertId() {
    return alertId;
  }

  public InterventionType getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public PlanStatus getStatus() {
    return status;
  }
}
