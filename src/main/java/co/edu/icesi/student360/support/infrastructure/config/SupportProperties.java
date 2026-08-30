package co.edu.icesi.student360.support.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "student360.support")
public record SupportProperties(
    @NotBlank @Size(min = 32) String pseudonymSecret, @DefaultValue Rule rule) {

  public record Rule(
      @DefaultValue("2") int lowWellbeingLevel,
      @DefaultValue("14") int maxDaysSinceAccess,
      @DefaultValue("0.6") BigDecimal minOnTimeRate) {}
}
