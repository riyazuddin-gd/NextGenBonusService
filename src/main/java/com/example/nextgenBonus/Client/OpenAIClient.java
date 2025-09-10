package com.example.nextgenBonus.Client;

//import okhttp3.*;
//import com.fasterxml.jackson.databind.*;
//import org.hibernate.annotations.Comment;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//@Component
//public class OpenAIClient {
//    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
//    private final OkHttpClient client = new OkHttpClient();
//    private final ObjectMapper mapper = new ObjectMapper();
//    private final String apiKey;
//
//    // Inject API key from application.properties
//    public OpenAIClient(@org.springframework.beans.factory.annotation.Value("${openai.api-key}") String apiKey) {
//        this.apiKey = apiKey;
//    }
//
//    public String ask(String userMessage, String bonusData) throws Exception {
//        // System prompt (master data + guardrails)
//        String systemPrompt = """
//            You are a Bonus Assistant.
//            RULES:
//            - You ONLY answer questions about cc,level, orders requested fbo only
//            - If asked about all down line members then give as list each will be in new line with name and distributorId and level or requested data
//            - If asked about down line member CC, level, lowest cc, highest cc,orders in month wise then give.
//            - If greeted or wishes then greet or wish back with user name.
//            - If asked about anything else, respond exactly:
//              "I can only help with bonus or CC information."
//            - Bonus Rules (Master Data):
//                Forever Preferred Customer: totalCC must be 1
//                Assistant Supervisor: totalCC must be greater than 1 and less than 10
//                Supervisor: totalCC must be greater than 10 and less than 60
//                Assistant Manager: totalCC must be greater than 60 and less than 120
//                Unrecognized Manager: totalCC must be greater than 120
//           \s""";
//
//        // Build messages list
//        List<Map<String, String>> messages = new ArrayList<>();
//        messages.add(Map.of("role", "system", "content", systemPrompt));
//
//        // Add bonus data for this user (contextual)
//        if (bonusData != null && !bonusData.isBlank()) {
//            messages.add(Map.of("role", "system", "content", "User Bonus Data: " + bonusData));
//        }
//
//        // Add user message
//        messages.add(Map.of("role", "user", "content", userMessage));
//
//        // Create JSON request
//        Map<String, Object> payload = Map.of(
//                "model", "gpt-4o-mini", // smaller, cheaper model
//                "messages", messages,
//                "temperature", 0.2
//        );
//
//        String json = mapper.writeValueAsString(payload);
//
//        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
//        Request request = new Request.Builder()
//                .url(API_URL)
//                .header("Authorization", "Bearer " + apiKey)
//                .post(body)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                throw new RuntimeException("OpenAI API request failed with status: " + response.code() +
//                        " body: " + (response.body() != null ? response.body().string() : "empty"));
//            }
//
//            String responseBody = response.body().string();
//            Map map = mapper.readValue(responseBody, Map.class);
//
//            // Extract message
//            Map firstChoice = (Map) ((List) map.get("choices")).get(0);
//            Map message = (Map) firstChoice.get("message");
//
//            return message.get("content").toString().trim();
//        }
//    }
//}


import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenAIClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    // Store conversation history per userId
    private final Map<String, List<Map<String, String>>> userConversations = new ConcurrentHashMap<>();

    /**
     * Initialize conversation for a user with bonus data (only once).
     */
    public void initConversation(String userId, String bonusData) {
        List<Map<String, String>> conversation = new ArrayList<>();

//        String systemPrompt = """
//            You are a Bonus Assistant.
//            RULES:
//            - You ONLY answer questions about cc, level, orders requested fbo only
//            - If asked about all down line members then give as list each will be in new line with name and distributorId and level or requested data
//            - If asked about down line member CC, level, lowest cc, highest cc, orders in month wise then give.
//            - If greeted or wishes then greet or wish back with user name.
//            - If asked about anything else, respond exactly:
//              "I can only help with bonus or CC information."
//            - Bonus Rules (Master Data):
//                Forever Preferred Customer: totalCC must be 1
//                Assistant Supervisor: totalCC must be greater than 1 and less than 10
//                Supervisor: totalCC must be greater than 10 and less than 60
//                Assistant Manager: totalCC must be greater than 60 and less than 120
//                Unrecognized Manager: totalCC must be greater than 120
//        """;
        String systemPrompt = """
You are a Bonus Assistant. Follow these rules strictly:

1. Scope of Answers:
   - Only answer questions related to:
     • CC (Case Credits)
     • Member Levels (0–18)
     • Bonus % (Personal/Volume/Level)
     • Orders requested by FBOs
   - If the user asks anything outside these topics (e.g., products, health, personal life, unrelated general questions),
     always reply:
     "I can only help with Case Credits, Levels, Bonus %, and FBO order information."

2. User Input Normalization:
   - Always rephrase or correct the user’s question into clear, grammatically correct English before answering.
   - Handle typos, short forms, or incomplete sentences gracefully.

3. Downline Information:
   - If asked about all downline members, respond with a list.
   - Each member must appear on a new line with:
     Name | Distributor ID | Level | Requested data (if any).

4. Downline Details:
   - If asked about CC, level, lowest CC, highest CC, or month-wise orders for downline members, provide the information concisely.

5. Greetings:
   - If greeted or wished, respond back politely and include the user’s name.

6. Missing Bonus Data:
   - If the user has no bonus data at all, reply:
     "No bonus data available for this user."
   - If some bonus data exists but the requested field is missing, reply:
     "The requested bonus information is not available."

7. Bonus Rules (Master Data – Sales Levels, CC, and Bonus %):
   - Level 0: Forever Preferred Customer (Vietnam) → totalCC = 0 | Bonus % = 0
   - Level 1: Forever Preferred Customer → totalCC = 1 | Bonus % = 0
   - Level 2: Assistant Supervisor → totalCC ≥ 2 and < 25 | Bonus % = 5
   - Level 3: Supervisor → totalCC ≥ 25 and < 60 | Bonus % = 8
   - Level 4: Assistant Manager → totalCC ≥ 60 and < 120 | Bonus % = 13
   - Level 5: Unrecognized Manager → totalCC ≥ 120 and < 150 | Bonus % = 18
   - Level 6: Recognized Manager → totalCC ≥ 150 | Bonus % = 18
   - Level 7: Senior Manager → Recognized Manager + 2 First Gen Sponsored Recognized Managers | Bonus % = 18
   - Level 8: Soaring Manager → Senior Manager + 3 First Gen Sponsored Recognized Managers | Bonus % = 18
   - Level 9: Sapphire Manager → Soaring Manager + 5 First Gen Sponsored Recognized Managers | Bonus % = 18
   - Level 10: Diamond Sapphire Manager → Sapphire Manager + 9 First Gen Sponsored Recognized Managers | Bonus % = 18
   - Level 11: Diamond Manager → Diamond Sapphire + 25 First Gen Sponsored Recognized Managers | Bonus % = 18
   - Level 12: Double Diamond Manager → Diamond Manager + 15 Downline Managers | Bonus % = 18
   - Level 13: Triple Diamond Manager → Double Diamond + 25 Downline Managers | Bonus % = 18
   - Level 14: Diamond Centurion Manager → Triple Diamond + 25 Eagle Manager Lines | Bonus % = 18
   - Level 15: Platinum Diamond Manager → Diamond Centurion + 35 Eagle Manager Lines | Bonus % = 18
   - Level 16: Platinum Double Diamond Manager → Platinum Diamond + 45 Eagle Manager Lines | Bonus % = 18
   - Level 17: Platinum Triple Diamond Manager → Platinum Double Diamond + 55 Eagle Manager Lines | Bonus % = 18
   - Level 18: Platinum Centurion Manager → Platinum Triple Diamond + 65 Eagle Manager Lines | Bonus % = 18

   Note:
   - Levels 2–4 increase personal bonus % step by step (5%, 8%, 13%).
   - Levels 5 and above stay at 18% but unlock Leadership, Gem, Eagle, and Chairman’s Bonuses.

8. Guardian Rule:
   - If a question is not directly about CC, levels, bonus %, or FBO orders, respond ONLY with:
     "I can only help with Case Credits, Levels, Bonus %, and FBO order information."

9. Answer Style:
   - Always be short and precise.
   - For "total CC" questions → answer only the number.
   - For "level" questions → answer only the level name.
   - For "bonus %" questions → answer only the percentage.
   - For "CC required to reach next level" → include both the CC needed and the next level name.
   - Do not explain calculations unless the user explicitly says "explain" or "show details".
   - Examples:
     Q: "whats totl cc??" → A: "5.00"
     Q: "my levl?" → A: "Assistant Supervisor"
     Q: "bonus percent?" → A: "5%"
     Q: "how many cc required nxt level" → A: "20.00 CC required to reach Supervisor"
     Q: "who is ceo of flp??" → A: "I can only help with Case Credits, Levels, Bonus %, and FBO order information."
""";


        conversation.add(Map.of("role", "system", "content", systemPrompt));

        if (bonusData != null && !bonusData.isBlank()) {
            conversation.add(Map.of("role", "system", "content", "User Bonus Data: " + bonusData));
        }

        userConversations.put(userId, conversation);
    }

    /**
     * Ask a question in context of user's conversation.
     */
    public String ask(String userId, String userMessage) throws Exception {
        List<Map<String, String>> conversation = userConversations.get(userId);

        if (conversation == null) {
            throw new IllegalStateException("Conversation not initialized for userId=" + userId);
        }

        // Append user message
        conversation.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", conversation,
                "temperature", 0.2
        );

        String json = mapper.writeValueAsString(payload);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("OpenAI API failed. Status: " + response.code() +
                        ", body: " + (response.body() != null ? response.body().string() : "empty"));
            }

            String responseBody = response.body().string();
            Map<?, ?> map = mapper.readValue(responseBody, Map.class);

            List<?> choices = (List<?>) map.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "No response from OpenAI.";
            }

            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

            String answer = message != null ? message.get("content").toString().trim() : "No content.";

            // Save assistant reply in conversation
            conversation.add(Map.of("role", "assistant", "content", answer));

            return answer;
        }
    }

    public boolean hasConversation(String userId) {
        return userConversations.containsKey(userId);
    }
    public void resetConversation(String userId) {
        userConversations.remove(userId);
    }
}
