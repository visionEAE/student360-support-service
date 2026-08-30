package co.edu.icesi.student360.support.infrastructure.security;

import co.edu.icesi.student360.support.domain.port.Pseudonymizer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC-SHA256 of the student reference under a secret only this service holds. Deterministic, so a
 * student's entries can be found again; one-way, so the table reveals nobody without the key. In
 * stage 2 the key lives in Secret Manager; the function does not change.
 */
public class HmacPseudonymizer implements Pseudonymizer {

  private static final String ALGORITHM = "HmacSHA256";

  private final SecretKeySpec key;

  public HmacPseudonymizer(String secret) {
    this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
  }

  @Override
  public String pseudonymOf(String studentReference) {
    try {
      Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(key);
      return HexFormat.of()
          .formatHex(mac.doFinal(studentReference.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
      throw new IllegalStateException("Cannot compute pseudonym", exception);
    }
  }
}
