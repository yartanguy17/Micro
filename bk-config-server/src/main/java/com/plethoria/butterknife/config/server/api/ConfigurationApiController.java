package com.plethoria.butterknife.config.server.api;


import com.plethoria.butterknife.config.server.exception.ConfigurationException;
import com.plethoria.butterknife.config.server.service.ConfigurationService;
import com.plethoria.butterknife.config.server.service.FileConfigurationService;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Configuration api controller.
 */
@RestController

@RequestMapping("/config")
@Slf4j
public class ConfigurationApiController {

  private final List<String> langs;

  private final ConfigurationService configServerService;
  private final FileConfigurationService fileConfigurationService;

  /**
   * Instantiates a new Configuration api controller.
   *
   * @param configServerService      the config server service
   * @param langs                    the langs
   * @param fileConfigurationService the file configuration service
   */
  public ConfigurationApiController(
      @Qualifier(value = "fileSystemConfigurationService") ConfigurationService configServerService,
      @Value("${plethoria.languages}") String langs,
      FileConfigurationService fileConfigurationService) {
    this.configServerService = configServerService;
    this.langs = Arrays.asList(langs.split(","));
    this.fileConfigurationService = fileConfigurationService;
  }

  /**
   * Update properties response entity.
   *
   * @param applicationName the application name
   * @param properties      the properties
   * @return the response entity
   */
  @PostMapping("/{applicationName}")
  public ResponseEntity updateProperties(@PathVariable String applicationName,
      @RequestBody Map<String, Object> properties) {
    log.info("Update properties {}", properties.keySet());
    try {
      if (langs.stream().anyMatch(applicationName::contains)) {
        applicationName = "labels/" + applicationName;
      } else if (applicationName.equals("ldap")) {
        applicationName = "ldap/" + applicationName;
      } else if (applicationName.equals("lookups")) {
        applicationName = "lookups/" + applicationName;
      }
      configServerService.updateProperties(properties, applicationName);
      log.debug("Properties successfully updated");
      return ResponseEntity.ok().build();
    } catch (ConfigurationException e) {
      log.debug("Failed to update properties. {}", e.getMessage());
      log.trace("Cause: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Remove properties response entity.
   *
   * @param applicationName the application name
   * @param properties      the properties
   * @return the response entity
   */
  @PostMapping("/remove/{applicationName}")
  public ResponseEntity removeProperties(@PathVariable String applicationName,
      @RequestBody List<String> properties) {
    log.info("Remove properties {}", properties);
    try {
      configServerService.removeProperties(properties, applicationName);
      log.debug("Properties successfully removed");
      return ResponseEntity.ok().build();
    } catch (ConfigurationException e) {
      log.debug("Failed to remove properties. {}", e.getMessage());
      log.trace("Cause: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }


  /**
   * Reset properties to default response entity.
   *
   * @return the response entity
   */
  @DeleteMapping("/reset")
  public ResponseEntity resetPropertiesToDefault() {
    log.info("Resetting all properties");
    try {
      configServerService.resetConfigurationBrandingFilesToDefault();
      configServerService.resetPropertiesToDefault();
      return ResponseEntity.ok().build();
    } catch (ConfigurationException e) {
      log.debug("Failed to reset properties. {}", e.getMessage());
      log.trace("Cause: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Reset file properties to default response entity.
   *
   * @param applicationName the application name
   * @return the response entity
   */
  @DeleteMapping("/reset/{applicationName}")
  public ResponseEntity resetFilePropertiesToDefault(@PathVariable String applicationName) {
    log.info("Resetting properties for: {}", applicationName);
    if (langs.parallelStream().anyMatch(applicationName::contains)) {
      applicationName = "labels/" + applicationName;
    }
    try {
      configServerService.resetFilePropertiesToDefault(applicationName);

      return ResponseEntity.ok().build();
    } catch (NoSuchFileException e) {
      return ResponseEntity.ok().build();
    } catch (ConfigurationException e) {
      log.debug("Failed to reset properties. {}", e.getMessage());
      log.trace("Cause: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}