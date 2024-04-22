package com.plethoria.butterknife.application.initializers;

import java.util.Collections;
import org.apache.catalina.Context;
import org.apache.tomcat.websocket.server.WsSci;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Tomcat configuration.
 */
@Configuration
public class TomcatConfiguration {

  /**
   * Tomcat container factory tomcat servlet web server factory.
   *
   * @return the tomcat servlet web server factory
   */
  @Bean
  public TomcatServletWebServerFactory tomcatContainerFactory() {
    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
    ;
    factory.setTomcatContextCustomizers(Collections.singletonList(tomcatContextCustomizer()));
    return factory;
  }

  /**
   * Tomcat context customizer tomcat context customizer.
   *
   * @return the tomcat context customizer
   */
  @Bean
  public TomcatContextCustomizer tomcatContextCustomizer() {
    return new TomcatContextCustomizer() {
      @Override
      public void customize(Context context) {
        context.addServletContainerInitializer(new WsSci(), null);
      }
    };
  }
}
