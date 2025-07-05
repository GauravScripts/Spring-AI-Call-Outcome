package org.spring.call_outcome.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.call_outcome.DTO.CallNotes;
import org.spring.call_outcome.DTO.CallOutcomeResponse;
import org.spring.call_outcome.DTO.DateCallingTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CallOutcomeService {

    private static final Logger logger = LoggerFactory.getLogger(CallOutcomeService.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // Memory: callId -> List of Messages
    private final ConcurrentHashMap<String, List<Message>> memory = new ConcurrentHashMap<>();

    public CallOutcomeService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public CallOutcomeResponse analyzeCallOutcome(CallNotes callNotes) {
        String callId = callNotes.getCallId();
        String prompt = """
                Analyze the following call notes and determine the outcome of the call (Positive, Negative, or Neutral).
                Provide a brief explanation for your classification.
                Call Notes: %s
                Return the response as a valid JSON object with fields: callId, outcome, and explanation.
                Ensure the response contains only the JSON object, without any markdown, backticks, or additional text.
                Example: {"callId":"%s","outcome":"Positive","explanation":"The customer was satisfied."}
                """.formatted(callNotes.getNotes(), callId);

        // Get or create message history for this callId
        List<Message> history = memory.computeIfAbsent(callId, k -> new ArrayList<>());
        history.add(new UserMessage(prompt));

        String rawResponse;
        try {
            rawResponse = chatClient.prompt()
                    .messages(history)
                    .tools(new DateCallingTool())
                    .call()
                    .content();

            logger.info("Raw AI response: {}", rawResponse);

            String cleanedResponse = rawResponse
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .replaceAll("```", "")
                    .trim();

            logger.info("Cleaned AI response: {}", cleanedResponse);

            history.add(new AssistantMessage(cleanedResponse));

            CallOutcomeResponse outcomeResponse = objectMapper.readValue(cleanedResponse, CallOutcomeResponse.class);
            outcomeResponse.setCallId(callId);
            return outcomeResponse;
        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", e.getMessage());
            CallOutcomeResponse errorResponse = new CallOutcomeResponse();
            errorResponse.setCallId(callId);
            errorResponse.setOutcome("Error");
            errorResponse.setExplanation("Failed to parse AI response: " + e.getMessage());
            return errorResponse;
        }
    }

    public List<Message> getMemoryForCallId(String callId) {
        return memory.getOrDefault(callId, Collections.emptyList());
    }
}