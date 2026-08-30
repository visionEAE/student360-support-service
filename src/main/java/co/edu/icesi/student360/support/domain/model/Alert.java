package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** An early alert. {@code triggeringSignals} makes it explainable rather than a black box. */
@Entity
@Table(name = "alert", schema = "support")
public class Alert {

  @Id private UUID id;

  @Column(name = "student_reference", nullable = false)
  private String studentReference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Severity severity;

  @Column(nullable = false)
  private String source;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "triggering_signals", nullable = false, columnDefinition = "jsonb")
  private TriggeringSignals triggeringSignals;

  @Column(name = "generated_at", nullable = false)
  private Instant generatedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlertStatus status;

  protected Alert() {}

  public static Alert generate(
      String studentReference,
      Severity severity,
      String source,
      TriggeringSignals signals,
      Instant now) {
    Alert alert = new Alert();
    alert.id = UUID.randomUUID();
    alert.studentReference = studentReference;
    alert.severity = severity;
    alert.source = source;
    alert.triggeringSignals = signals;
    alert.generatedAt = now;
    alert.status = AlertStatus.OPEN;
    return alert;
  }

  public void acknowledge() {
    if (status == AlertStatus.OPEN) {
      status = AlertStatus.ACKNOWLEDGED;
    }
  }

  public UUID getId() {
    return id;
  }

  public String getStudentReference() {
    return studentReference;
  }

  public Severity getSeverity() {
    return severity;
  }

  public String getSource() {
    return source;
  }

  public TriggeringSignals getTriggeringSignals() {
    return triggeringSignals;
  }

  public Instant getGeneratedAt() {
    return generatedAt;
  }

  public AlertStatus getStatus() {
    return status;
  }
}
