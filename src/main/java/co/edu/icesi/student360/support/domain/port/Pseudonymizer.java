package co.edu.icesi.student360.support.domain.port;

/**
 * Port: turns a student reference into the pseudonym wellbeing entries are stored under. The
 * function must be deterministic (so entries of one student can be found again) and one-way (so the
 * table alone reveals nobody).
 */
public interface Pseudonymizer {

  String pseudonymOf(String studentReference);
}
