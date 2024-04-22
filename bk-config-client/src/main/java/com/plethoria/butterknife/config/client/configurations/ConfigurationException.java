package com.plethoria.butterknife.config.client.configurations;

/**
 * The type Configuration exception.
 */
public class ConfigurationException extends RuntimeException {

  /**
   * Instantiates a new Configuration exception.
   *
   * @param message the message
   */
  public ConfigurationException(String message) {
    super(message);
  }

  /**
   * Instantiates a new Configuration exception.
   *
   * @param message the message
   * @param cause   the cause
   */
  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}