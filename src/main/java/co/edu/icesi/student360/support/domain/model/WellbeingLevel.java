package co.edu.icesi.student360.support.domain.model;

/** How a numeric level (1–4) is labelled for people. */
public enum WellbeingLevel {
  LOW,
  MEDIUM,
  GOOD;

  public static WellbeingLevel of(int level) {
    if (level <= 1) {
      return LOW;
    }
    return level == 2 ? MEDIUM : GOOD;
  }
}
