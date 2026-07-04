package com.gchoi.loyalty.message;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @MessageMapping("/chat")
  public void handleMessage(ChatMessage message) {
    this.chatService.handleMessage(message);
  }

  @MessageMapping("/join")
  public void handleJoin(ChatMessage message) {
    this.chatService.handleJoin(message);
  }

  @MessageMapping("/leave")
  public void handleLeave(ChatMessage message) {
    this.chatService.handleLeave(message);
  }
}
