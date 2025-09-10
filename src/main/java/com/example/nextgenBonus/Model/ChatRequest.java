package com.example.nextgenBonus.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatRequest {
    @JsonProperty("memberId")
    private String memberId;

    @JsonProperty("message")
    private String message;

    public ChatRequest() {
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "memberId=" + memberId +
                ", message='" + message + '\'' +
                '}';
    }
}