package com.plethoria.butterknife.config.server.exception;

/**
 * The type Configuration exception.
 */
public class ConfigurationException extends Exception {

  /**
   * Instantiates a new Configuration exception.
   *
   * @param cause the cause
   */
  public ConfigurationException(Throwable cause) {
    super(cause);
  }

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
