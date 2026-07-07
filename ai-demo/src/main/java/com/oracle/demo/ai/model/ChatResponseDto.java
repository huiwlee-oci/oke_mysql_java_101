package com.oracle.demo.ai.model;

public class ChatResponseDto {
  private String reply;
  private String error;
  private String model;
  private String opcRequestId;

  public ChatResponseDto() {
  }

  public static ChatResponseDto ok(String reply, String model, String opcRequestId) {
    ChatResponseDto response = new ChatResponseDto();
    response.reply = reply;
    response.model = model;
    response.opcRequestId = opcRequestId;
    return response;
  }

  public static ChatResponseDto error(String error) {
    ChatResponseDto response = new ChatResponseDto();
    response.error = error;
    return response;
  }

  public String getReply() {
    return reply;
  }

  public void setReply(String reply) {
    this.reply = reply;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getOpcRequestId() {
    return opcRequestId;
  }

  public void setOpcRequestId(String opcRequestId) {
    this.opcRequestId = opcRequestId;
  }
}
