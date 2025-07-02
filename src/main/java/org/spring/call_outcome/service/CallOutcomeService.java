package org.spring.call_outcome.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.call_outcome.DTO.CallNotes;
import org.spring.call_outcome.DTO.CallOutcomeResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
@Service
public class CallOutcomeService {

    private static final Logger logger = LoggerFactory.getLogger(CallOutcomeService.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public CallOutcomeService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public CallOutcomeResponse analyzeCallOutcome(CallNotes callNotes) {
        String prompt = """
                Analyze the following call notes and determine the outcome of the call (Positive, Negative, or Neutral).
                Provide a brief explanation for your classification.
                Call Notes: %s
                Return the response as a valid JSON object with fields: callId, outcome, and explanation.
                Ensure the response contains only the JSON object, without any markdown, backticks, or additional text.
                Example: {"callId":"%s","outcome":"Positive","explanation":"The customer was satisfied."}
                """.formatted(callNotes.getNotes(), callNotes.getCallId());

        String rawResponse;
        try {
            rawResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            logger.info("Raw AI response: {}", rawResponse);

            // Clean the response to remove markdown or backticks
            String cleanedResponse = rawResponse
                    .replaceAll("```json\\n", "") // Remove ```json
                    .replaceAll("\\n```", "")     // Remove ```
                    .replaceAll("```", "")        // Remove any stray backticks
                    .trim();

            logger.info("Cleaned AI response: {}", cleanedResponse);

            // Parse the cleaned response
            CallOutcomeResponse outcomeResponse = objectMapper.readValue(cleanedResponse, CallOutcomeResponse.class);
            outcomeResponse.setCallId(callNotes.getCallId());
            return outcomeResponse;
        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", e.getMessage());
            CallOutcomeResponse errorResponse = new CallOutcomeResponse();
            errorResponse.setCallId(callNotes.getCallId());
            errorResponse.setOutcome("Error");
            errorResponse.setExplanation("Failed to parse AI response: " + e.getMessage());
            return errorResponse;
        }
    }
}