package co.edu.icesi.student360.support.domain.model;

/** Self-reported feeling for one dimension; the numeric level is what the rule reads. */
public enum Mood {
  DIFFICULT(1),
  FAIR(2),
  GOOD(3),
  VERY_GOOD(4);

  private final int level;

  Mood(int level) {
    this.level = level;
  }

  public int level() {
    return level;
  }

  public static Mood ofLevel(int level) {
    for (Mood mood : values()) {
      if (mood.level == level) {
        return mood;
      }
    }
    throw new IllegalArgumentException("No mood for level " + level);
  }
}
