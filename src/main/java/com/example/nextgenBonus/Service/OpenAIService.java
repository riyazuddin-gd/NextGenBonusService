package com.example.nextgenBonus.Service;

import com.example.nextgenBonus.Client.OpenAIClient;
import org.springframework.stereotype.Service;

//@Service
//public class OpenAIService {
//
//    private final OpenAIClient openAIClient;
//
//    public OpenAIService(OpenAIClient openAIClient) {
//        this.openAIClient = openAIClient;
//    }
//
//    /**
//     * Ask OpenAI a question about bonuses.
//     *
//     * @param prompt User's question
//     * @param bonusData Context data for this user (from DB)
//     */
//    public String ask(String prompt, String bonusData) {
//        try {
//            return openAIClient.ask(prompt, bonusData);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error communicating with OpenAI", e);
//        }
//    }
//}

@Service
public class OpenAIService {

    private final OpenAIClient openAIClient;

    public OpenAIService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    /**
     * Initialize a conversation for a user with bonus data (only once).
     */
    public void initConversation(String userId, String bonusData) {
        openAIClient.initConversation(userId, bonusData);
    }
    public void resetConversation(String userId) {
        openAIClient.resetConversation(userId);
    }
    /**
     * Check if a conversation already exists for a user.
     */
    public boolean hasConversation(String userId) {
        return openAIClient.hasConversation(userId);
    }

    /**
     * Ask OpenAI a question in the context of the user's conversation.
     */
    public String ask(String userId, String prompt) {
        try {
            return openAIClient.ask(userId, prompt);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error communicating with OpenAI", e);
        }
    }

}

