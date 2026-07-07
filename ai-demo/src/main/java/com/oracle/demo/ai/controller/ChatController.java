package com.oracle.demo.ai.controller;

import com.oracle.demo.ai.config.OciGenAiProperties;
import com.oracle.demo.ai.model.ChatConfigDto;
import com.oracle.demo.ai.model.ChatRequestDto;
import com.oracle.demo.ai.model.ChatResponseDto;
import com.oracle.demo.ai.service.OciGenerativeAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
  private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

  private final OciGenerativeAiService generativeAiService;
  private final OciGenAiProperties properties;

  public ChatController(
      OciGenerativeAiService generativeAiService, OciGenAiProperties properties) {
    this.generativeAiService = generativeAiService;
    this.properties = properties;
  }

  @GetMapping("/config")
  public ChatConfigDto config() {
    ChatConfigDto config = new ChatConfigDto();
    config.setAuthMode(properties.getAuthMode());
    config.setRegion(properties.getRegion());
    config.setModel(
        hasText(properties.getEndpointId()) ? properties.getEndpointId() : properties.getModelId());
    config.setServingMode(hasText(properties.getEndpointId()) ? "Dedicated endpoint" : "On-demand model");
    config.setChatFormat(properties.getChatFormat());
    config.setTemperature(properties.getTemperature());
    config.setMaxTokens(properties.getMaxTokens());
    config.setCompartment(maskOcid(properties.getCompartmentId()));
    return config;
  }

  @PostMapping
  public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto request) {
    try {
      return ResponseEntity.ok(generativeAiService.chat(request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(ChatResponseDto.error(e.getMessage()));
    } catch (IllegalStateException e) {
      LOG.warn("Chat configuration error: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .body(ChatResponseDto.error(e.getMessage()));
    } catch (Exception e) {
      LOG.error("OCI Generative AI chat request failed", e);
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body(ChatResponseDto.error("OCI Generative AI request failed."));
    }
  }

  private String maskOcid(String value) {
    if (!hasText(value)) {
      return "Not configured";
    }
    String trimmed = value.trim();
    int visible = Math.min(12, trimmed.length());
    return "..." + trimmed.substring(trimmed.length() - visible);
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
