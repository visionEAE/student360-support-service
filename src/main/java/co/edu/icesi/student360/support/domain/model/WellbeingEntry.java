package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A self-report stored under a pseudonym; the real student id is never written next to it. The
 * {@code level} is the minimum mood across dimensions: the rule reads that single number. Drafts
 * are invisible to advisors and never evaluated.
 */
@Entity
@Table(name = "wellbeing_entry", schema = "support")
public class WellbeingEntry {

  @Id private UUID id;

  @Column(name = "student_pseudonym", nullable = false)
  private String studentPseudonym;

  @Column(nullable = false)
  private short level;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EntryStatus status;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected WellbeingEntry() {}

  public static WellbeingEntry start(String pseudonym, int level, EntryStatus status, Instant now) {
    WellbeingEntry entry = new WellbeingEntry();
    entry.id = UUID.randomUUID();
    entry.studentPseudonym = pseudonym;
    entry.level = (short) level;
    entry.status = status;
    entry.recordedAt = now;
    entry.updatedAt = now;
    entry.sentAt = status == EntryStatus.SENT ? now : null;
    return entry;
  }

  public void update(int level, EntryStatus status, Instant now) {
    this.level = (short) level;
    this.updatedAt = now;
    if (status == EntryStatus.SENT && this.status != EntryStatus.SENT) {
      this.status = EntryStatus.SENT;
      this.sentAt = now;
      this.recordedAt = now;
    }
  }

  public boolean isSent() {
    return status == EntryStatus.SENT;
  }

  public UUID getId() {
    return id;
  }

  public String getStudentPseudonym() {
    return studentPseudonym;
  }

  public int getLevel() {
    return level;
  }

  public EntryStatus getStatus() {
    return status;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }

  public Instant getSentAt() {
    return sentAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
