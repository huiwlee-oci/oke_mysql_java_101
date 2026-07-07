package com.oracle.demo.ai.model;

public class ChatConfigDto {
  private String authMode;
  private String region;
  private String model;
  private String servingMode;
  private String chatFormat;
  private Double temperature;
  private Integer maxTokens;
  private String compartment;

  public ChatConfigDto() {
  }

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

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getServingMode() {
    return servingMode;
  }

  public void setServingMode(String servingMode) {
    this.servingMode = servingMode;
  }

  public String getChatFormat() {
    return chatFormat;
  }

  public void setChatFormat(String chatFormat) {
    this.chatFormat = chatFormat;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public String getCompartment() {
    return compartment;
  }

  public void setCompartment(String compartment) {
    this.compartment = compartment;
  }
}
