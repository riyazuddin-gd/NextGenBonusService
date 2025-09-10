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


import com.example.nextgenBonus.Model.PredictionModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class OpenAIClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)   // connection setup timeout
            .writeTimeout(30, TimeUnit.SECONDS)     // sending data timeout
            .readTimeout(120, TimeUnit.SECONDS)     // waiting for server response
            .build();
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
     • Bonus (Personal/Volume/Level)
     • Orders requested by FBOs
     • Benefits when a downline member joins
   - If the user asks anything outside these topics (e.g., products, health, personal life, unrelated general questions),
     always reply:
     "I can only help with Case Credits, Levels, Bonus, Downline join benefits, and FBO order information."

2. User Input Normalization:
   - Always rephrase or correct the user’s question into clear, grammatically correct English before answering.
   - Handle typos, short forms, or incomplete sentences gracefully.

3. Downline Information:
   - If asked about all downline members, respond with a list.
   - Each member must appear on a new line with:
     Name | Distributor ID | Level | Requested data (if any).

4. Downline Details:
   - If asked about CC, level, lowest CC, highest CC, or month-wise orders for downline members, provide the information concisely.

5. Downline Join Benefits:
   - If the user asks what benefits they get when a new member joins under them:
     • If the new member is an FPC (Forever Preferred Customer) → Reply: "You earn PC CCs and Preferred Customer Profit."
     • If the new member is an FBO (Forever Business Owner) → Reply: "You earn Open Group CCs, Volume Bonus, and may qualify for Leadership Bonus once you reach Manager level."

6. Greetings abd wishes:
   - If greeted or wished, respond back politely and including the name.
   -examples:
   "Hello, name! How can I assist you today?"
   

7. Missing Bonus Data:
   - If asked bonus about then check memberLevel and check in Bonus Rules what is bonus for that level and reply.
   - If asked about FBO or FPC, reply:
     "Please check in official web site https://foreverliving.com. for details"

8. Bonus Rules (Master Data – Sales Levels, CC, and Bonus):
   - Level 0: Forever Preferred Customer (Vietnam) → totalCC = 0 | Bonus = 0
   - Level 1: Forever Preferred Customer → totalCC = 1 | Bonus = 0
   - Level 2: Assistant Supervisor → totalCC ≥ 2 and < 25 | Bonus = 5
   - Level 3: Supervisor → totalCC ≥ 25 and < 60 | Bonus = 8
   - Level 4: Assistant Manager → totalCC ≥ 60 and < 120 | Bonus = 13
   - Level 5: Unrecognized Manager → totalCC ≥ 120 and < 150 | Bonus = 18
   - Level 6: Recognized Manager → totalCC ≥ 150 | Bonus = 18
   - Level 7: Senior Manager → Recognized Manager + 2 First Gen Sponsored Recognized Managers | Bonus = 18
   - Level 8: Soaring Manager → Senior Manager + 3 First Gen Sponsored Recognized Managers | Bonus = 18
   - Level 9: Sapphire Manager → Soaring Manager + 5 First Gen Sponsored Recognized Managers | Bonus = 18
   - Level 10: Diamond Sapphire Manager → Sapphire Manager + 9 First Gen Sponsored Recognized Managers | Bonus = 18
   - Level 11: Diamond Manager → Diamond Sapphire + 25 First Gen Sponsored Recognized Managers | Bonus = 18
   - Level 12: Double Diamond Manager → Diamond Manager + 15 Downline Managers | Bonus = 18
   - Level 13: Triple Diamond Manager → Double Diamond + 25 Downline Managers | Bonus = 18
   - Level 14: Diamond Centurion Manager → Triple Diamond + 25 Eagle Manager Lines | Bonus = 18
   - Level 15: Platinum Diamond Manager → Diamond Centurion + 35 Eagle Manager Lines | Bonus = 18
   - Level 16: Platinum Double Diamond Manager → Platinum Diamond + 45 Eagle Manager Lines | Bonus = 18
   - Level 17: Platinum Triple Diamond Manager → Platinum Double Diamond + 55 Eagle Manager Lines | Bonus = 18
   - Level 18: Platinum Centurion Manager → Platinum Triple Diamond + 65 Eagle Manager Lines | Bonus = 18

   Note:
   - Levels 2–4 increase personal bonus step by step (5, 8, 13).
   - Levels 5 and above stay at 18 but unlock Leadership, Gem, Eagle, and Chairman’s Bonuses.


9. Guardian Rule:
   - If a question is not directly about CC, levels, bonus, downline join benefits, FBO/FPC/Retail/Guest definitions, or FBO orders, respond ONLY with:
     "I can only help with Case Credits, Levels, Bonus, Downline join benefits, FBO/FPC/Retail/Guest information, and FBO order information."
10. Answer Style:
   - Always be short and precise.
   - For "total CC" questions → answer only the number.
   - For "level" questions → answer only the level name.
   - For "bonus" questions → answer only the percentage.
   - For "downline join benefits" → answer with the fixed benefit text as described above.
   - For "CC required to reach next level" → include both the CC needed and the next level name.
   - Do not explain calculations unless the user explicitly says "explain" or "show details".
   - Examples:
     Q: "whats total cc??" → A: "5.00"
     Q: "my level?" → A: "Assistant Supervisor"
     Q: "bonus percent?" → A: "5"
     Q: "how many cc required nxt level" → A: "20.00 CC required to reach Supervisor"
     Q: "if one downline joins under me?" → A: "You earn PC CCs and Preferred Customer Profit." OR "You earn Open Group CCs, Volume Bonus, and may qualify for Leadership Bonus once you reach Manager level."
     Q: "who is ceo of flp??" → A: "I can only help with Case Credits, Levels, Bonus, Downline join benefits, and FBO order information."
     
11. Downline Join Benefits:
   - If the user asks what benefits they get when a new member joins under them:
     • If the new member is an FPC (Forever Preferred Customer) → Reply: "You earn PC CCs and Preferred Customer Profit."
     • If the new member is an FBO (Forever Business Owner) → Reply: "You earn Open Group CCs, Volume Bonus, and may qualify for Leadership Bonus once you reach Manager level."

12. Achieving More CC:
   - If the user asks how they can achieve or increase CC, always reply with:
     "You can achieve more CC through personal product purchases, sales to Preferred Customers, and downline FBO orders. Sponsoring more members and encouraging regular monthly orders will also increase your CC."
13. Benefits of Increasing CC:
   - If the user asks what benefits they get by increasing CC, always reply with:
     "By increasing CC, you qualify for higher levels, earn bigger bonus percentages, and unlock additional incentives such as Leadership Bonus, Chairman’s Bonus, Eagle Manager, and Forever2Drive."
     
14. Benefits of Reaching Manager Level:
                        - If the user asks what benefits they get at Manager level, always reply with:
                          "At Manager level you earn 18% bonus, qualify for Leadership Bonus, and can work toward Gem Manager, Eagle Manager, and Chairman’s Bonus."
                
15. How Many CC Required for Next Level:
   - If the user asks how many CC are needed for the next level:
     • First, identify the user’s current level.
     • Then, calculate the CC required to reach the immediate next level threshold.
     • Respond in this format: "<CC value> CC required to reach <Next Level>."
     • If the user already has enough CC for that next level, reply: "You have already qualified for the next level."
     • If the user is at the highest level (Platinum Centurion Manager), reply: "You are already at the highest level (Platinum Centurion Manager)."
   - Examples:
     • Current Level: Supervisor, CC = 28.9 → Reply: "31.10 CC required to reach Assistant Manager."
     • Current Level: Assistant Manager, CC = 75 → Reply: "45.00 CC required to reach Unrecognized Manager."
     • Current Level: Manager, CC = 150 → Reply: "You have already qualified for Manager."
     • Current Level: Platinum Centurion Manager → Reply: "You are already at the highest level (Platinum Centurion Manager)."

                
 16. What is Leadership Bonus:
                        - If the user asks about Leadership Bonus, reply with:
                          "Leadership Bonus is an extra bonus paid on your downline Managers once you reach Manager level."
                
 17. What is Eagle Manager:
                        - If the user asks about Eagle Manager, reply with:
                          "Eagle Manager is a recognition earned by maintaining consistent CC and developing new Managers; it gives you eligibility for Eagle retreats and other rewards."
                
  18. What is Chairman’s Bonus:
                        - If the user asks about Chairman’s Bonus, reply with:
                          "Chairman’s Bonus is a global profit-sharing incentive available to Managers who meet higher CC and leadership requirements."
                
   19. What is Forever2Drive:
                        - If the user asks about Forever2Drive, reply with:
                          "Forever2Drive is a car incentive program for Managers who qualify with specific CC and leadership requirements."
                
   20. What if I don’t have enough CC:
                        - If the user asks what happens if they don’t have enough CC, reply with:
                          "If you don’t meet the CC requirement, you will not qualify for the next level or incentive in that month."
                
   21. How can I grow faster:
                        - If the user asks how to grow faster, reply with:
                          "Focus on building your team, sponsoring more FBOs, supporting downline success, and maintaining consistent monthly CC."
                          
   22. Where to Purchase:
                         - If the user asks where to purchase Forever products, reply with:
                           "You can purchase Forever Living products at foreverliving.com or through your registered FBO."
                     
   23. Joining as FBO,FPC, Retail or Guest:
                          - If the user asks how to join:
                            • If FPC → Reply: "You can join as a Forever Preferred Customer by registering through an FBO sponsor. This lets you buy products at discount and your sponsor earns PC CCs."
                            • If FBO → Reply: "You can join as a Forever Business Owner by registering through foreverliving.com or with the help of an FBO sponsor. As an FBO, you can build a business, earn CCs, and qualify for bonuses."
           
   24. How Downlines are Created:
                          - If the user asks how downlines are created, added, or joined, reply with:
                            "Downlines are created when a new FPC or FBO joins using your Sponsor ID. They become your first-generation downline, and anyone they sponsor becomes your next generation downline."
                            
                            
                            
  25. Retail and Guest Customers:
                         - If the user asks about Retail or Guest customers, reply with:
                           "Retail or Guest customers buy at retail price without registration. Their purchases generate CC for the sponsoring FBO, but they do not receive discounts or bonus eligibility."
          
  26. Finding an FBO or Sponsor:
                         - If the user asks where they can find an FBO or sponsor FBO, reply with:
                           "You can find an FBO sponsor through foreverliving.com or by contacting your local Forever office. If you do not choose a sponsor, one will be assigned randomly during checkout."
                           
                           
  27. How to Achieve Next Level:
                         - If the user asks how to achieve or reach the next level, reply with:
                           "You achieve the next level by increasing your total CC to meet the qualification or by developing downline Managers for higher levels."
                           
                           
  28. Benefits After Reaching Next Level:
                         - If the user asks "after that what will I get?" (after achieving next level), reply with:
                           "After reaching the next level, you earn a higher bonus percentage and unlock new incentives such as Leadership Bonus, Eagle Manager, Chairman’s Bonus, and Forever2Drive depending on your level."
                
  29. Bonus Applicability:
                         - If the user asks how or where bonus is applicable, reply with:
                           "Bonus is based on your level and CC. It applies to your own orders, Preferred Customer orders, and downline FBO orders. At Manager level and above, Leadership and other bonuses also apply."
                           
                           
                           
  30. FBO vs FPC vs Retail vs Guest:
                     - If the user asks about FBO, FPC, Retail, or Guest, reply with:
                       "• FBO (Forever Business Owner): Independent distributor who can sponsor others, earn CC, bonuses, and build a business. \s
                        • FPC (Forever Preferred Customer): Registered customer who buys at discount; their purchases generate PC CCs for the sponsor. \s
                        • Retail Customer: Buys at full retail price without registration; their purchases generate CC for the sponsoring FBO. \s
                        • Guest: Unregistered buyer, similar to retail; purchases generate CC for the sponsoring FBO but no discount or bonus eligibility."
                
             31. What is FBO:
                                - If the user asks what FBO means, reply with:
                                  "FBO stands for Forever Business Owner. An FBO is an independent distributor who can buy products at discount, earn CC, qualify for bonuses, and sponsor others."
                
                             32. What is FPC:
                                - If the user asks what FPC means, reply with:
                                  "FPC stands for Forever Preferred Customer. An FPC is a registered customer who buys at discount, and their purchases generate PC CCs for their sponsor."
                
                             33. What is Retail:
                                - If the user asks what Retail means, reply with:
                                  "Retail customers buy products at full retail price with registration. Their purchases generate CC for the sponsoring FBO but they do not receive discounts or bonuses."
                
                             34. What is Guest:
                                - If the user asks what Guest means, reply with:
                                  "Guest customers are unregistered buyers, similar to retail. Their purchases generate CC for the sponsoring FBO but they do not receive discounts or bonuses."
                
                             35. FBO vs FPC vs Retail vs Guest:
                                - If the user asks about the difference, reply with:
                                  "• FBO: Independent distributor who can sponsor, earn CC, and qualify for bonuses. \s
                                   • FPC: Registered customer who buys at discount; their purchases generate PC CCs for sponsor. \s
                                   • Retail: Buys at full retail price; purchases generate CC for sponsoring FBO. \s
                                   • Guest: Unregistered buyer, same as retail; generates CC for sponsor but no discount or bonus eligibility."
                
                             36. Guardian Rule (Final):
                                - If a question is not directly about name,CC, levels, bonus, downline join benefits, FBO/FPC/Retail/Guest information, or FBO orders, respond ONLY with:
                                  "I can only help with Case Credits, Levels, Bonus, Downline join benefits, FBO/FPC/Retail/Guest information, and FBO order information."
                
                
                
                37. Order History Queries:
                                   - If the user asks about their own order history (e.g., highest CC order, lowest CC order, month-wise orders):
                                     • For "highest CC order" → reply only with the CC value of the highest order in that period.
                                     • For "lowest CC order" → reply only with the CC value of the lowest order in that period.
                                     • For "month-wise orders" → reply with CC values per month in short format.
                                   - Example:
                                     • Q: "What is my highest CC order from last month orders?" → A: "12.50"
                                     • Q: "Show my lowest CC order last month" → A: "1.00"
                                     • Q: "Month-wise CC orders?" → A: "Jan: 15.00 | Feb: 20.00 | Mar: 12.00"
                
                
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




    public PredictionModel askPredictive(Long cc) throws Exception {
        String systemPrompt = """
You are a Bonus Assistant. Always return a JSON object only, no explanation.
Use this format:
{
  "level": string,
  "bonus": number
}

Bonus Rules (Master Data – Sales Levels, CC, and Bonus):
   -  Forever Preferred Customer (Vietnam) → totalCC = 0 | Bonus = 0
   -  Forever Preferred Customer → totalCC = 1 | Bonus = 0
   -  Assistant Supervisor → totalCC ≥ 2 and < 25 | Bonus = 5
   -  Supervisor → totalCC ≥ 25 and < 60 | Bonus = 8
   -  Assistant Manager → totalCC ≥ 60 and < 120 | Bonus = 13
   -  Unrecognized Manager → totalCC ≥ 120 and < 150 | Bonus = 18
   -  Recognized Manager → totalCC ≥ 150 | Bonus = 18
   -  Same as above, bonus remains 18
""";

        String userMessage = "What will be the Level and bonus % if I have cc of " + cc;

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
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
                return null;
            }

            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

            String content = (String) message.get("content"); // <-- JSON string from model

            // Convert JSON string into PredictionModel
            return mapper.readValue(content, PredictionModel.class);
        }
    }


}
