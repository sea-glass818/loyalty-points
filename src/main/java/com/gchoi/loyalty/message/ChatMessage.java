package com.gchoi.loyalty.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
  private String sender;
  private String recipient;
  private String content;
  private String type;
  private String timestamp;
}
