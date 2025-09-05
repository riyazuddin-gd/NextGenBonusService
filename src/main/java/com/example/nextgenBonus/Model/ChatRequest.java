package com.example.nextgenBonus.Model;

public class ChatRequest {
    private Long memberId;
    private String message;

    public ChatRequest() {
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}