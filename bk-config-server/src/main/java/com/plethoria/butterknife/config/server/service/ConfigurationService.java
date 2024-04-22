package com.plethoria.butterknife.config.server.service;


import com.plethoria.butterknife.config.server.exception.ConfigurationException;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Map;

/**
 * The interface Configuration service.
 */
public interface ConfigurationService {

  /**
   * Update properties in yaml file with name - applicationName, updated properties will be written
   * in the runtime file.
   *
   * @param properties      - properties for update
   * @param applicationName - name of the file whose properties will be updated
   * @throws ConfigurationException the configuration exception
   */
  void updateProperties(Map<String, Object> properties, String applicationName)
      throws ConfigurationException;

  /**
   * Remove properties.
   *
   * @param properties      the properties
   * @param applicationName the application name
   * @throws ConfigurationException the configuration exception
   */
  void removeProperties(List<String> properties, String applicationName)
      throws ConfigurationException;

  /**
   * Reset properties to default.
   *
   * @throws ConfigurationException the configuration exception
   */
  void resetPropertiesToDefault() throws ConfigurationException;

  /**
   * Reset file properties to default.
   *
   * @param applicationName the application name
   * @throws NoSuchFileException    the no such file exception
   * @throws ConfigurationException the configuration exception
   */
  void resetFilePropertiesToDefault(String applicationName)
      throws NoSuchFileException, ConfigurationException;

  /**
   * Reset configuration branding files to default.
   *
   * @throws ConfigurationException the configuration exception
   */
  void resetConfigurationBrandingFilesToDefault() throws ConfigurationException;
}