package com.gchoi.loyalty.message;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  @Bean
  public NewTopic chatMessagesTopic() {
    return TopicBuilder
        .name("chat-messages")
        .partitions(3)
        .replicas(1)  // 1 replica for local dev
        .build();
  }
}