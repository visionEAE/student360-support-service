package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "support_report", schema = "support")
public class SupportReport {

  @Id private UUID id;

  @Column(name = "alert_id", nullable = false)
  private UUID alertId;

  @Column(name = "advisor_reference", nullable = false)
  private String advisorReference;

  @Column(nullable = false)
  private String content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected SupportReport() {}

  public static SupportReport write(
      UUID alertId, String advisorReference, String content, Instant now) {
    SupportReport report = new SupportReport();
    report.id = UUID.randomUUID();
    report.alertId = alertId;
    report.advisorReference = advisorReference;
    report.content = content;
    report.createdAt = now;
    return report;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAlertId() {
    return alertId;
  }

  public String getAdvisorReference() {
    return advisorReference;
  }

  public String getContent() {
    return content;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
