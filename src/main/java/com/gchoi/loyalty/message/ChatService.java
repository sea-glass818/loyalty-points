package com.gchoi.loyalty.message;

import java.time.Instant;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

  private final KafkaTemplate<String, Object> objectKafkaTemplate;


  public ChatService(KafkaTemplate<String, Object> objectKafkaTemplate) {
    this.objectKafkaTemplate = objectKafkaTemplate;
  }

  public void handleMessage(ChatMessage message) {
    message.setTimestamp(Instant.now().toString());
    this.objectKafkaTemplate.send("chat-messages", message.getSender(), message);
  }

  public void handleJoin(ChatMessage message) {
    message.setType("JOIN");
    message.setContent(message.getSender() + " joined the chat");
    message.setTimestamp(Instant.now().toString());
    this.objectKafkaTemplate.send("chat-messages", message.getSender(), message);
  }

  public void handleLeave(ChatMessage message) {
    message.setType("LEAVE");
    message.setContent(message.getSender() + " left the chat");
    message.setTimestamp(Instant.now().toString());
    this.objectKafkaTemplate.send("chat-messages", message.getSender(), message);
  }
}
