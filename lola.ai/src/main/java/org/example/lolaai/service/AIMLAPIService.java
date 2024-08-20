package org.example.lolaai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class AIMLAPIService {

    private static final Logger logger = LoggerFactory.getLogger(AIMLAPIService.class);
    private static final String AIML_API_URL = "https://api.aimlapi.com/v1/messages"; // Correct endpoint

    @Value("${aimlapi.api.key}")
    private String apiKey;

    public String getResponseFromAIMLAPI(String prompt) {
        // Basic conversation handling
        String response = handleBasicConversation(prompt);
        if (response != null) {
            return response;
        }

        boolean useRealApi = true;  // Toggle this flag for testing

        // Construct the Lola-specific prompt
        String lolaPrompt = "You are a friendly and playful pug named Lola. Respond to this in a fun and pug-like way: " + prompt;

        if (useRealApi) {
            try {
                // Prepare the HTTP headers
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + apiKey);
                headers.set("Content-Type", "application/json");

                // Prepare the request body
                JSONObject requestBody = createRequestBody(lolaPrompt);

                // Create an HTTP entity with the headers and body
                HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

                // Send the POST request to the AIML API
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(AIML_API_URL, entity, String.class);

                // Log the entire response for debugging
                String responseBody = responseEntity.getBody();
                logger.debug("API Response: " + responseBody);

                // Parse the response body
                JSONObject jsonResponse = new JSONObject(responseBody);

                // Extract the generated text from the "content" array
                if (jsonResponse.has("content")) {
                    JSONArray contentArray = jsonResponse.getJSONArray("content");
                    StringBuilder generatedText = new StringBuilder();
                    for (int i = 0; i < contentArray.length(); i++) {
                        JSONObject contentObject = contentArray.getJSONObject(i);
                        if ("text".equals(contentObject.getString("type"))) {
                            generatedText.append(contentObject.getString("text")).append(" ");
                        }
                    }
                    return generatedText.toString().trim();
                } else {
                    logger.error("Unexpected response structure: " + responseBody);
                    return "Woof! I'm having some trouble thinking right now. Try again later!";
                }
            } catch (HttpClientErrorException.TooManyRequests e) {
                logger.error("AIML API quota exceeded", e);
                return "Sorry, I've hit my thinking limit for now. Please try again later!";
            } catch (Exception e) {
                logger.error("Error occurred while calling AIML API", e);
                return "Woof! I'm having some trouble thinking right now. Try again later!";
            }
        } else {
            // Mock response for testing without making actual API calls
            return "This is a mock response simulating the AIML API.";
        }
    }

    // Method to handle basic predefined conversations
    private String handleBasicConversation(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        switch (lowerPrompt) {
            case "hi":
            case "hello":
                return "Woof! Hi there! How are you?";
            case "how are you":
            case "how are you?":
                return "I'm a happy pug! How about you?";
            case "how old are you":
            case "how old are you?":
                return "I'm forever young, woof woof!";
            default:
                return null;  // If no match, return null to proceed with API call or fallback
        }
    }

    // Method to create the request body for the AIML API
    private JSONObject createRequestBody(String prompt) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "claude-3-5-sonnet-20240620"); // Example model, update as required

        // Create messages array
        JSONArray messagesArray = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("role", "user");
        messageObject.put("content", prompt);
        messagesArray.put(messageObject);

        requestBody.put("messages", messagesArray);

        return requestBody;
    }
}
