package com.plethoria.butterknife.config.client.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * The type Web socket controller.
 */
@Controller
@Slf4j
public class WebSocketController {

  private final SimpMessagingTemplate template;

  /**
   * Instantiates a new Web socket controller.
   *
   * @param template the template
   */
  @Autowired
  WebSocketController(SimpMessagingTemplate template) {
    this.template = template;
  }

  /**
   * Send message.
   *
   * @param message the message
   */
  @MessageMapping("/send/message")
  public void sendMessage(String message) {
    log.info("STOMP message: {}", message);
    this.template.convertAndSend("/message", message);
  }
}