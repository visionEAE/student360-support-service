package co.edu.icesi.student360.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients
public class SupportServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(SupportServiceApplication.class, args);
  }
}
