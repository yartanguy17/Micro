package com.plethoria.butterknife.config.client.configurations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * The type Cloud environment.
 */
@Slf4j
public class CloudEnvironment extends StandardServletEnvironment {


  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static CloudEnvironment getInstance() {
    return CloudEnvironmentHolder.INSTANCE;
  }

  /**
   * Init property sources.
   *
   * @param servletContext the servlet context
   * @param servletConfig  the servlet config
   */
  @Override
  public void initPropertySources(ServletContext servletContext, ServletConfig servletConfig) {
    super.initPropertySources(servletContext, servletConfig);
    customizePropertySources(this.getPropertySources());
    log.info("Cloud Environment: {}", this);
  }

  /**
   * Customize property sources.
   *
   * @param propertySources the property sources
   */
  @Override
  protected void customizePropertySources(MutablePropertySources propertySources) {
    super.customizePropertySources(propertySources);
    try {
      PropertySource<?> source = initConfigServicePropertySourceLocator(this);
      if (source != null) {
        propertySources.addLast(source);
      }
    } catch (Exception ex) {
      log.error("Error Initializing the cloud configuration system. Cause: {}", ex.getMessage());
    }
  }

  private PropertySource<?> initConfigServicePropertySourceLocator(Environment environment)
      throws ConfigurationException {
    String configLocation = System.getProperty("config.server.properties.location");
    log.debug("Config Server properties location: {}", configLocation);
    if (configLocation != null) {
      try {
        log.info("Loading config server settings");
        YamlFileConfiguration yamlFileConfiguration = new YamlFileConfiguration(configLocation);
        Iterator<String> it = yamlFileConfiguration.getKeys();
        Map<String, Object> result = new HashMap<>();
        it.forEachRemaining(key -> result.put(key, yamlFileConfiguration.getProperty(key)));
        result.forEach((s, o) -> {
          log.info("{} {}", s, o);
        });
        log.info("Loading the Configuration server {}",
            result.get("configuration.server.url").toString());

        ConfigClientProperties configClientProperties = new ConfigClientProperties(environment) {
          @Override
          public ConfigClientProperties override(Environment environment) {
            return this;
          }
        };
        String[] uris = {result.get("configuration.server.url").toString()};
        configClientProperties.setUri(uris);
        configClientProperties.setProfile(
            result.get("application.profile").toString().replace("[", "").replace("]", ""));
        configClientProperties.setLabel("");
        configClientProperties.setName(result.get("application.name.active").toString());
        configClientProperties.setUsername(result.get("configuration.server.username").toString());
        configClientProperties.setPassword(result.get("configuration.server.password").toString());

        ConfigServicePropertySourceLocator configServicePropertySourceLocator = new ConfigServicePropertySourceLocator(
            configClientProperties);
        return configServicePropertySourceLocator.locate(environment);
      } catch (ConfigurationException e) {
        log.error("Configuration server client initialization failed", e);
      }
      return null;
    } else {
      log.error(
          "The property mhealth.config.server.properties.location could not be found in your VM arguments.");
      ConfigurationException ex = new ConfigurationException(
          "The property mhealth.config.server.properties.location could not be found in your VM arguments.");
      throw ex;
    }

  }

  private static class CloudEnvironmentHolder {

    private static final CloudEnvironment INSTANCE = new CloudEnvironment();
  }
}