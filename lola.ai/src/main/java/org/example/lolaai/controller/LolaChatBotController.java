package org.example.lolaai.controller;

import org.example.lolaai.service.AIMLAPIService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LolaChatBotController {

    private final AIMLAPIService aimlAPIService;
    private final List<Message> conversation = new ArrayList<>();

    public LolaChatBotController(AIMLAPIService aimlAPIService) {
        this.aimlAPIService = aimlAPIService;
    }

    // Serve the chat page
    @GetMapping("/")
    public String showChatPage(Model model) {
        model.addAttribute("conversation", conversation);
        return "chat";
    }

    // Handle form submissions from the chat page
    @PostMapping("/chat")
    public String chatWithLola(@RequestParam("message") String message, Model model) {
        // Add user's message to the conversation
        conversation.add(new Message("user", message));

        // Get Lola's response
        String response = aimlAPIService.getResponseFromAIMLAPI(message);

        // Add Lola's response to the conversation
        conversation.add(new Message("lola", response));

        // Update the conversation in the model
        model.addAttribute("conversation", conversation);

        return "chat";
    }

    private static class Message {
        private final String sender;
        private final String text;

        public Message(String sender, String text) {
            this.sender = sender;
            this.text = text;
        }

        public String getSender() {
            return sender;
        }

        public String getText() {
            return text;
        }
    }
}
