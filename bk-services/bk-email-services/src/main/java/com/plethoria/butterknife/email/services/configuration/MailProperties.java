package com.plethoria.butterknife.email.services.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "butterknife.email")
@Getter
@Setter
public class MailProperties {

  private String host;
  private int port;
  private String username;
  private String password;
  private String authentication;
  private String debug;
  private String tls;
  private String protocol;
}
