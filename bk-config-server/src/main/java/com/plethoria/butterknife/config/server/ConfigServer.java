package com.plethoria.butterknife.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The type Config server.
 */
@SpringBootApplication(scanBasePackages = "com.plethoria")
@EnableConfigServer
@EnableAsync
public class ConfigServer {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(ConfigServer.class, args);
  }
}
