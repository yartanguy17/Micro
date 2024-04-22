package com.plethoria.butterknife.config.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * The type Refresh application context.
 */
@Component
@Slf4j
public class RefreshApplicationContext implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  /**
   * Sets application context.
   *
   * @param applicationContext the application context
   */
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }


  /**
   * Refreshctx.
   */
  public void refreshctx() {
    ((AnnotationConfigWebApplicationContext) (applicationContext)).refresh();
  }
}