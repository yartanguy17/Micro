package com.plethoria.butterknife.config.client;

import com.plethoria.butterknife.config.client.configurations.CloudEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * The type Config app context initializer.
 */
@Slf4j
@Configuration
public class ConfigAppContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {


  /**
   * Initialize.
   *
   * @param applicationContext the application context
   */
  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    CloudEnvironment environment = CloudEnvironment.getInstance();
    log.info("Initializing Context.");
    applicationContext.setEnvironment(environment);
  }

}