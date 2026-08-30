package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** What the support team asks of another office on behalf of a student. */
@Entity
@Table(name = "support_request", schema = "support")
public class SupportRequest {

  @Id private UUID id;

  @Column(name = "student_reference", nullable = false)
  private String studentReference;

  @Column(name = "alert_id")
  private UUID alertId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RequestType type;

  @Column(nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RequestStatus status;

  @Column private String resolution;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected SupportRequest() {}

  public static SupportRequest open(
      String advisorReference,
      String studentReference,
      UUID alertId,
      RequestType type,
      String description,
      Instant now) {
    SupportRequest request = new SupportRequest();
    request.id = UUID.randomUUID();
    request.studentReference = studentReference;
    request.alertId = alertId;
    request.type = type;
    request.description = description;
    request.status = RequestStatus.OPEN;
    request.createdBy = advisorReference;
    request.createdAt = now;
    request.updatedAt = now;
    return request;
  }

  public void changeStatus(RequestStatus next, String resolution, Instant now) {
    this.status = next;
    if (resolution != null) {
      this.resolution = resolution;
    }
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public String getStudentReference() {
    return studentReference;
  }

  public UUID getAlertId() {
    return alertId;
  }

  public RequestType getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public RequestStatus getStatus() {
    return status;
  }

  public String getResolution() {
    return resolution;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
