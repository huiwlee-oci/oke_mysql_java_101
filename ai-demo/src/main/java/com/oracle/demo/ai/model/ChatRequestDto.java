package com.oracle.demo.ai.model;

import java.util.ArrayList;
import java.util.List;

public class ChatRequestDto {
  private List<ChatMessage> messages = new ArrayList<>();

  public List<ChatMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<ChatMessage> messages) {
    this.messages = messages == null ? new ArrayList<>() : messages;
  }

  public String latestUserMessage() {
    for (int i = messages.size() - 1; i >= 0; i--) {
      ChatMessage message = messages.get(i);
      if (message != null
          && "user".equalsIgnoreCase(message.getRole())
          && hasText(message.getContent())) {
        return message.getContent().trim();
      }
    }
    return "";
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
