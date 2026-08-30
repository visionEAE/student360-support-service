package co.edu.icesi.student360.support.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;

/** One dimension (economic, academic, emotional) of a wellbeing entry. */
@Entity
@Table(name = "wellbeing_entry_dimension", schema = "support")
public class DimensionEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "entry_id", nullable = false)
  private UUID entryId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WellbeingDimension dimension;

  @Column(nullable = false)
  private short mood;

  @Column(nullable = false, columnDefinition = "text[]")
  private List<String> needs;

  @Column private String note;

  protected DimensionEntry() {}

  public static DimensionEntry of(
      UUID entryId, WellbeingDimension dimension, Mood mood, List<String> needs, String note) {
    DimensionEntry entry = new DimensionEntry();
    entry.entryId = entryId;
    entry.dimension = dimension;
    entry.mood = (short) mood.level();
    entry.needs = List.copyOf(needs);
    entry.note = note;
    return entry;
  }

  public UUID getEntryId() {
    return entryId;
  }

  public WellbeingDimension getDimension() {
    return dimension;
  }

  public Mood getMood() {
    return Mood.ofLevel(mood);
  }

  public List<String> getNeeds() {
    return needs;
  }

  public String getNote() {
    return note;
  }
}
