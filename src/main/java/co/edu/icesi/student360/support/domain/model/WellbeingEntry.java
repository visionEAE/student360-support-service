package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Stored under a pseudonym; the real student id is never written next to a wellbeing level. */
@Entity
@Table(name = "wellbeing_entry", schema = "support")
public class WellbeingEntry {

  @Id private UUID id;

  @Column(name = "student_pseudonym", nullable = false)
  private String studentPseudonym;

  @Column(nullable = false)
  private short level;

  @Column private String comment;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt;

  protected WellbeingEntry() {}

  public static WellbeingEntry record(String pseudonym, int level, String comment, Instant now) {
    WellbeingEntry entry = new WellbeingEntry();
    entry.id = UUID.randomUUID();
    entry.studentPseudonym = pseudonym;
    entry.level = (short) level;
    entry.comment = comment;
    entry.recordedAt = now;
    return entry;
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

  public String getComment() {
    return comment;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }
}
