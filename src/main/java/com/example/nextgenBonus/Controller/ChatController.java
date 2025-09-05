package com.example.nextgenBonus.Controller;

import com.example.nextgenBonus.Model.ChatRequest;
import com.example.nextgenBonus.Model.ChatResponse;
import com.example.nextgenBonus.Service.ChatService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin("*")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/user-request")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.handleUserMessage(request);
        return ResponseEntity.ok(response);
    }
}
