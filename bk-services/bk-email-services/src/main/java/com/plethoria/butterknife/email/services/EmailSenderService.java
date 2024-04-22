package com.plethoria.butterknife.email.services;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * The type Email sender service.
 */
@Service
@Setter
@Slf4j
public class EmailSenderService {

  @Autowired
  private JavaMailSender javaMailSender;

  /**
   * Send email.
   *
   * @param message the message
   */
  public void sendEmail(SimpleMailMessage message) {
    log.debug("Sending Email from {} to {}", message.getFrom(), message.getTo());
    javaMailSender.send(message);
  }
}
