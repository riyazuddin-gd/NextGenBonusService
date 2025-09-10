package com.example.nextgenBonus.Controller;

import com.example.nextgenBonus.Entities.Member;
import com.example.nextgenBonus.Model.ChatRequest;
import com.example.nextgenBonus.Model.ChatResponse;
import com.example.nextgenBonus.Model.Downline;
import com.example.nextgenBonus.Model.PredictionModel;
import com.example.nextgenBonus.Repository.MemberRepository;
import com.example.nextgenBonus.Service.ChatService;
//import com.example.nextgenBonus.Service.OpenAIService;
import com.example.nextgenBonus.Service.JsonUtil;
import com.example.nextgenBonus.Service.OpenAIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    private final OpenAIService openAIService;
    @Autowired
    private MemberRepository memberRepository;

    public ChatController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }
//    @PostMapping("/user-request")
//    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
//        System.out.println(request.toString());
//        ChatResponse response = chatService.handleUserMessage(request);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/user-request")
    public ResponseEntity<ChatResponse> getChat() {
        ChatResponse response = chatService.handleUserMessage();
        openAIService.resetConversation("001000000001");
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/user-request")
//    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) throws JsonProcessingException {
//        System.out.println("Incoming Request: " + request);
//
//        // TODO: Lookup bonus data from DB using request.getUserId()
//        // For now, hardcode or fetch from repository
//        Optional<Member> member = memberRepository.findByDistributorId(request.getMemberId().toString());
//        String bonusData = "no bonus information";
//        if (member.isPresent()) {
//            List<Member> downlines1 = member.get().getDownlines().stream()
//                    .map(member1 -> {
//                        return member1;
//                    })
//                    .collect(Collectors.toList());
//            member.get().setDownlines(downlines1);
//        }
//
//        bonusData = JsonUtil.toJson(member.get());
//        System.out.println(bonusData);
//        String answer = openAIService.ask(request.getMessage(), bonusData);
//
//        ChatResponse response = new ChatResponse();
//        response.setReply(answer);
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/user-request")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) throws JsonProcessingException {
        System.out.println("Incoming Request: " + request);

        String userId = request.getMemberId().toString();

        // Check if conversation already exists for this user
        if (!openAIService.hasConversation(userId)) {
            // First time → fetch bonus data from DB
            Optional<Member> member = memberRepository.findByDistributorId(userId);
            String bonusData = "no bonus information";
            if (member.isPresent()) {
                List<Member> downlines1 = member.get().getDownlines().stream() .map(member1 -> { return member1; }) .collect(Collectors.toList()); member.get().setDownlines(downlines1);
                bonusData = JsonUtil.toJson(member.get());
            }
            System.out.println(bonusData);
            // Initialize conversation once with bonusData
            openAIService.initConversation(userId, bonusData);
        }

        // Always ask with user message (no bonus data again)
        String answer = openAIService.ask(userId, request.getMessage());

        ChatResponse response = new ChatResponse();
        response.setReply(answer);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/predictive/{cc}")
    public ResponseEntity<PredictionModel> chat(@PathVariable Long cc) throws JsonProcessingException {
        // Check if conversation already exists for this user
            // Initialize conversation once with bonusData


        // Always ask with user message (no bonus data again)
        PredictionModel response = openAIService.askPredictive(cc);


        return ResponseEntity.ok(response);
    }


}
