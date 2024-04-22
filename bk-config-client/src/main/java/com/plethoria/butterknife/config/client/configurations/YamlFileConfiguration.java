package com.plethoria.butterknife.config.client.configurations;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.yaml.snakeyaml.Yaml;

/**
 * Base class representing yaml configuration source.
 */
public class YamlFileConfiguration extends AbstractFileConfiguration {

  /**
   * Instantiates a new Yaml file configuration.
   */
  public YamlFileConfiguration() {
  }

  /**
   * Instantiates a new Yaml file configuration.
   *
   * @param file the file
   * @throws ConfigurationException the configuration exception
   */
  public YamlFileConfiguration(File file) throws ConfigurationException {
    super(file);
  }

  /**
   * Instantiates a new Yaml file configuration.
   *
   * @param url the url
   * @throws ConfigurationException the configuration exception
   */
  public YamlFileConfiguration(String url) throws ConfigurationException {
    super(url);
  }

  /**
   * Instantiates a new Yaml file configuration.
   *
   * @param url the url
   * @throws ConfigurationException the configuration exception
   */
  public YamlFileConfiguration(URL url) throws ConfigurationException {
    super(url);
  }

  /**
   * Load.
   *
   * @param in the in
   */
  @Override
  public void load(Reader in) {
    Yaml yaml = new Yaml();
    Iterable<Object> it_conf = yaml.loadAll(in);

    for (Object obj : it_conf) {
      if (obj instanceof Map) {
        Map<String, Map<String, Object>> configuration = (Map<String, Map<String, Object>>) obj;
        getKeyValue(configuration, "");
      }
    }
  }

  /**
   * Save.
   *
   * @param out the out
   */
  @Override
  public void save(Writer out) {
  }

  private void getKeyValue(Map<String, Map<String, Object>> map, String key) {
    String localKey = key;
    for (String configKey : map.keySet()) {
      Object configValue = map.get(configKey);

      if (configValue instanceof Map) {
        key += configKey;
        key += ".";

        getKeyValue((Map<String, Map<String, Object>>) configValue, key);
      } else {
        key += configKey;
        addProperty(key, configValue.toString());
      }
      key = localKey;
    }
  }
}