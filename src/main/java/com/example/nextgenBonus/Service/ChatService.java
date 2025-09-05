package com.example.nextgenBonus.Service;

import com.example.nextgenBonus.Model.ChatRequest;
import com.example.nextgenBonus.Model.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    public ChatResponse handleUserMessage(ChatRequest chatRequest){
        ChatResponse chatResponse = new ChatResponse("Hello") ;
        return chatResponse;
    }

    public ChatResponse handleUserMessage(){
        ChatResponse chatResponse = new ChatResponse("Hello World") ;
        return chatResponse;
    }
}
