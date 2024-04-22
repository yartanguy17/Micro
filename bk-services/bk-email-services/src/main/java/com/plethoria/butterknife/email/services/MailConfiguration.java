package com.plethoria.butterknife.email.services;

import com.plethoria.butterknife.email.services.configuration.MailProperties;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * The type Mail configuration.
 */
@Configuration
public class MailConfiguration {

  @Autowired
  private MailProperties properties;

  /**
   * Gets java mail sender.
   *
   * @return the java mail sender
   */
  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(properties.getHost());
    mailSender.setPort(properties.getPort());

    mailSender.setUsername(properties.getUsername());
    mailSender.setPassword(properties.getPassword());

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", properties.getProtocol());
    props.put("mail.smtp.auth", properties.getAuthentication());
    props.put("mail.smtp.starttls.enable", properties.getTls());
    props.put("mail.debug", properties.getDebug());

    return mailSender;
  }
}
