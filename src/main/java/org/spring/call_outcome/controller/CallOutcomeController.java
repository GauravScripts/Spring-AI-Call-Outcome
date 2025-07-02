package org.spring.call_outcome.controller;

    import org.spring.call_outcome.DTO.CallNotes;
    import org.spring.call_outcome.DTO.CallOutcomeResponse;
    import org.spring.call_outcome.service.CallOutcomeService;
    import org.springframework.ai.chat.messages.Message;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/call-outcome")
    public class CallOutcomeController {

        private final CallOutcomeService callOutcomeService;

        public CallOutcomeController(CallOutcomeService callOutcomeService) {
            this.callOutcomeService = callOutcomeService;
        }

        @PostMapping("/analyze")
        public CallOutcomeResponse analyzeCall(@RequestBody CallNotes callNotes) {
            return callOutcomeService.analyzeCallOutcome(callNotes);
        }

        @GetMapping("/memory/{callId}")
        public List<Message> getMemory(@PathVariable String callId) {
            return callOutcomeService.getMemoryForCallId(callId);
        }
    }