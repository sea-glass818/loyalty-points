package com.gchoi.loyalty.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import java.time.Instant;

@Controller
public class ChatController {

  @Autowired
  private KafkaTemplate<String,Object> kafkaTemplate;

  @MessageMapping("/chat")
  public void handleMessage(ChatMessage message) {
    message.setTimestamp(Instant.now().toString());
    kafkaTemplate.send("chat-messages", message.getSender(), message);
  }

  @MessageMapping("/join")
  public void handleJoin(ChatMessage message) {
    message.setType("JOIN");
    message.setContent(message.getSender() + " joined the chat");
    message.setTimestamp(Instant.now().toString());
    kafkaTemplate.send("chat-messages", message.getSender(), message);
  }

  @MessageMapping("/leave")
  public void handleLeave(ChatMessage message) {
    message.setType("LEAVE");
    message.setContent(message.getSender() + " left the chat");
    message.setTimestamp(Instant.now().toString());
    kafkaTemplate.send("chat-messages", message.getSender(), message);
  }
}