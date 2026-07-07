package com.oracle.demo.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oci.genai")
public class OciGenAiProperties {
  private String authMode = "config_file";
  private String region;
  private String compartmentId;
  private String modelId;
  private String endpointId;
  private String chatFormat = "cohere";
  private String profile = "DEFAULT";
  private String configFile;
  private Double temperature = 0.3;
  private Double topP = 0.75;
  private Integer topK;
  private Integer maxTokens = 600;
  private Integer historyLimit = 8;
  private String systemPrompt =
      "You are a helpful assistant for Oracle Cloud users. Keep answers concise and practical.";

  public String getAuthMode() {
    return authMode;
  }

  public void setAuthMode(String authMode) {
    this.authMode = authMode;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getCompartmentId() {
    return compartmentId;
  }

  public void setCompartmentId(String compartmentId) {
    this.compartmentId = compartmentId;
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public String getEndpointId() {
    return endpointId;
  }

  public void setEndpointId(String endpointId) {
    this.endpointId = endpointId;
  }

  public String getChatFormat() {
    return chatFormat;
  }

  public void setChatFormat(String chatFormat) {
    this.chatFormat = chatFormat;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public String getConfigFile() {
    return configFile;
  }

  public void setConfigFile(String configFile) {
    this.configFile = configFile;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Double getTopP() {
    return topP;
  }

  public void setTopP(Double topP) {
    this.topP = topP;
  }

  public Integer getTopK() {
    return topK;
  }

  public void setTopK(Integer topK) {
    this.topK = topK;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public Integer getHistoryLimit() {
    return historyLimit;
  }

  public void setHistoryLimit(Integer historyLimit) {
    this.historyLimit = historyLimit;
  }

  public String getSystemPrompt() {
    return systemPrompt;
  }

  public void setSystemPrompt(String systemPrompt) {
    this.systemPrompt = systemPrompt;
  }
}
