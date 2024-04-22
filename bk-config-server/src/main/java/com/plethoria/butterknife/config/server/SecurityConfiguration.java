package com.plethoria.butterknife.config.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * The type Security configuration.
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  /**
   * Configure.
   *
   * @param http the http
   * @throws Exception the exception
   */
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.csrf();
    super.configure(http);
  }
}
