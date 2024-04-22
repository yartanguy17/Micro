package com.plethoria.butterknife.application;

import com.plethoria.butterknife.config.client.ConfigAppContextInitializer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * The type Butter knife.
 */
@SpringBootApplication(scanBasePackages = "com.plethoria")
@Slf4j
@Setter
@ConfigurationPropertiesScan(basePackages = "com.plethoria")
public class ButterKnife implements CommandLineRunner {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    new SpringApplicationBuilder(ButterKnife.class)
        .initializers(new ConfigAppContextInitializer()).run(args);
  }

  /**
   * Run.
   *
   * @param args the args
   * @throws Exception the exception
   */
  @Override
  public void run(String... args) throws Exception {
    log.info("Application started successfully!");
  }
}
