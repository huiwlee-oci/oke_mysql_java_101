package com.oracle.demo.ai.service;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.AssistantMessage;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.DedicatedServingMode;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import com.oracle.bmc.generativeaiinference.model.SystemMessage;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import com.oracle.demo.ai.config.OciGenAiProperties;
import com.oracle.demo.ai.model.ChatMessage;
import com.oracle.demo.ai.model.ChatRequestDto;
import com.oracle.demo.ai.model.ChatResponseDto;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class OciGenerativeAiService {
  private final OciGenAiProperties properties;
  private final AtomicReference<GenerativeAiInferenceClient> client = new AtomicReference<>();

  public OciGenerativeAiService(OciGenAiProperties properties) {
    this.properties = properties;
  }

  public ChatResponseDto chat(ChatRequestDto request) {
    validateConfiguration();
    String message = request.latestUserMessage();
    if (!hasText(message)) {
      throw new IllegalArgumentException("Message is required.");
    }

    ChatDetails chatDetails =
        ChatDetails.builder()
            .compartmentId(properties.getCompartmentId())
            .servingMode(buildServingMode())
            .chatRequest(buildChatRequest(request, message))
            .build();

    ChatResponse response =
        getClient().chat(ChatRequest.builder().chatDetails(chatDetails).build());

    return ChatResponseDto.ok(
        extractReply(response),
        hasText(properties.getEndpointId()) ? properties.getEndpointId() : properties.getModelId(),
        response.getOpcRequestId());
  }

  private GenerativeAiInferenceClient getClient() {
    GenerativeAiInferenceClient existing = client.get();
    if (existing != null) {
      return existing;
    }

    GenerativeAiInferenceClient created =
        GenerativeAiInferenceClient.builder().build(createAuthenticationProvider());
    created.setRegion(Region.fromRegionId(properties.getRegion()));

    if (client.compareAndSet(null, created)) {
      return created;
    }
    created.close();
    return client.get();
  }

  private BaseChatRequest buildChatRequest(ChatRequestDto request, String latestUserMessage) {
    if ("generic".equals(normalize(properties.getChatFormat()))) {
      return buildGenericChatRequest(request);
    }
    return buildCohereChatRequest(request.getMessages(), latestUserMessage);
  }

  private BaseChatRequest buildCohereChatRequest(List<ChatMessage> messages, String latestUserMessage) {
    CohereChatRequest.Builder builder =
        CohereChatRequest.builder()
            .message(latestUserMessage)
            .preambleOverride(buildPreamble(messages))
            .maxTokens(properties.getMaxTokens())
            .temperature(properties.getTemperature())
            .topP(properties.getTopP());

    if (properties.getTopK() != null) {
      builder.topK(properties.getTopK());
    }
    return builder.build();
  }

  private BaseChatRequest buildGenericChatRequest(ChatRequestDto request) {
    GenericChatRequest.Builder builder =
        GenericChatRequest.builder()
            .messages(buildGenericMessages(request.getMessages()))
            .maxTokens(properties.getMaxTokens())
            .temperature(properties.getTemperature())
            .topP(properties.getTopP());

    if (properties.getTopK() != null) {
      builder.topK(properties.getTopK());
    }
    return builder.build();
  }

  private List<Message> buildGenericMessages(List<ChatMessage> messages) {
    List<Message> genericMessages = new ArrayList<>();
    if (hasText(properties.getSystemPrompt())) {
      genericMessages.add(
          SystemMessage.builder().content(textContent(properties.getSystemPrompt().trim())).build());
    }

    if (messages == null || messages.isEmpty()) {
      return genericMessages;
    }

    int limit = properties.getHistoryLimit() == null ? 8 : properties.getHistoryLimit();
    int start = Math.max(0, messages.size() - limit - 1);
    for (int i = start; i < messages.size(); i++) {
      ChatMessage message = messages.get(i);
      if (message == null || !hasText(message.getContent())) {
        continue;
      }
      String content = message.getContent().trim();
      if ("assistant".equalsIgnoreCase(message.getRole())) {
        genericMessages.add(AssistantMessage.builder().content(textContent(content)).build());
      } else {
        genericMessages.add(UserMessage.builder().content(textContent(content)).build());
      }
    }
    return genericMessages;
  }

  private List<ChatContent> textContent(String text) {
    return Arrays.asList(TextContent.builder().text(text).build());
  }

  private AbstractAuthenticationDetailsProvider createAuthenticationProvider() {
    String mode = normalize(properties.getAuthMode());
    switch (mode) {
      case "oke_workload_identity":
      case "oke":
      case "workload_identity":
        return OkeWorkloadIdentityAuthenticationDetailsProvider.builder().build();
      case "instance_principal":
      case "instance_principals":
        return InstancePrincipalsAuthenticationDetailsProvider.builder().build();
      case "resource_principal":
      case "resource_principals":
        return ResourcePrincipalAuthenticationDetailsProvider.builder().build();
      case "config_file":
      case "config":
      default:
        return createConfigFileProvider();
    }
  }

  private AbstractAuthenticationDetailsProvider createConfigFileProvider() {
    try {
      ConfigFileReader.ConfigFile config =
          hasText(properties.getConfigFile())
              ? ConfigFileReader.parse(properties.getConfigFile(), properties.getProfile())
              : ConfigFileReader.parseDefault(properties.getProfile());
      return new ConfigFileAuthenticationDetailsProvider(config);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to load OCI config file credentials.", e);
    }
  }

  private ServingMode buildServingMode() {
    if (hasText(properties.getEndpointId())) {
      return DedicatedServingMode.builder().endpointId(properties.getEndpointId()).build();
    }
    return OnDemandServingMode.builder().modelId(properties.getModelId()).build();
  }

  private String buildPreamble(List<ChatMessage> messages) {
    StringBuilder preamble = new StringBuilder();
    if (hasText(properties.getSystemPrompt())) {
      preamble.append(properties.getSystemPrompt().trim());
    }

    String history = buildHistory(messages);
    if (hasText(history)) {
      if (preamble.length() > 0) {
        preamble.append("\n\n");
      }
      preamble.append("Conversation so far:\n").append(history);
    }
    return preamble.toString();
  }

  private String buildHistory(List<ChatMessage> messages) {
    if (messages == null || messages.isEmpty()) {
      return "";
    }
    int limit = properties.getHistoryLimit() == null ? 8 : properties.getHistoryLimit();
    int start = Math.max(0, messages.size() - limit - 1);
    StringJoiner history = new StringJoiner("\n");

    for (int i = start; i < Math.max(0, messages.size() - 1); i++) {
      ChatMessage message = messages.get(i);
      if (message == null || !hasText(message.getContent())) {
        continue;
      }
      String role = "assistant".equalsIgnoreCase(message.getRole()) ? "Assistant" : "User";
      history.add(role + ": " + message.getContent().trim());
    }
    return history.toString();
  }

  private String extractReply(ChatResponse response) {
    Object chatResult = invoke(response, "getChatResult");
    Object chatResponse = invoke(chatResult, "getChatResponse");

    String directText = asText(invoke(chatResponse, "getText"));
    if (hasText(directText)) {
      return directText.trim();
    }

    String messageText = extractGenericMessageText(chatResponse);
    if (hasText(messageText)) {
      return messageText.trim();
    }

    return "OCI Generative AI returned an empty response.";
  }

  private String extractGenericMessageText(Object chatResponse) {
    Object choices = invoke(chatResponse, "getChoices");
    if (!(choices instanceof Collection) || ((Collection<?>) choices).isEmpty()) {
      return "";
    }

    Object firstChoice = ((Collection<?>) choices).iterator().next();
    Object message = invoke(firstChoice, "getMessage");
    Object content = invoke(message, "getContent");

    if (content instanceof Collection) {
      StringJoiner parts = new StringJoiner("\n");
      for (Object item : (Collection<?>) content) {
        String text = asText(invoke(item, "getText"));
        if (hasText(text)) {
          parts.add(text.trim());
        }
      }
      return parts.toString();
    }

    String text = asText(content);
    if (hasText(text)) {
      return text;
    }
    return asText(invoke(message, "getText"));
  }

  private Object invoke(Object target, String methodName) {
    if (target == null) {
      return null;
    }
    try {
      Method method = target.getClass().getMethod(methodName);
      return method.invoke(target);
    } catch (Exception ignored) {
      return null;
    }
  }

  private String asText(Object value) {
    return value instanceof String ? (String) value : "";
  }

  private void validateConfiguration() {
    if (!hasText(properties.getRegion())) {
      throw new IllegalStateException("OCI_GENAI_REGION is required.");
    }
    if (!hasText(properties.getCompartmentId())) {
      throw new IllegalStateException("OCI_GENAI_COMPARTMENT_ID is required.");
    }
    if (!hasText(properties.getEndpointId()) && !hasText(properties.getModelId())) {
      throw new IllegalStateException("Set OCI_GENAI_MODEL_ID or OCI_GENAI_ENDPOINT_ID.");
    }
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_');
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
