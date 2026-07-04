package com.gchoi.loyalty.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaWebSocketBridge {

  private static final Logger log = LoggerFactory.getLogger(KafkaWebSocketBridge.class);

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @KafkaListener(
      topics = "chat-messages",
      groupId = "${spring.kafka.consumer.group-id}",
      containerFactory = "objectKafkaListenerContainerFactory")
  public void handleChatMessage(ChatMessage message) {
    log.info("consumed from Kafka: {}", message);
    messagingTemplate.convertAndSend("/topic/messages", message);
    log.info("pushed to WebSocket /topic/messages");
  }
}
