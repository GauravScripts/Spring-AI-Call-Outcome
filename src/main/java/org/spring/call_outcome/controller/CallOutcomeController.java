package org.spring.call_outcome.controller;


import org.spring.call_outcome.DTO.CallNotes;
import org.spring.call_outcome.DTO.CallOutcomeResponse;
import org.spring.call_outcome.service.CallOutcomeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/call-outcome")
public class CallOutcomeController {

    private final CallOutcomeService callOutcomeService;

    public CallOutcomeController(CallOutcomeService callOutcomeService) {
        this.callOutcomeService = callOutcomeService;
    }

    @PostMapping
    public ResponseEntity<CallOutcomeResponse> getCallOutcome(@RequestBody CallNotes callNotes) {
        if (callNotes.getNotes() == null || callNotes.getNotes().isEmpty()) {
            return ResponseEntity.badRequest().body(new CallOutcomeResponse());
        }
        CallOutcomeResponse response = callOutcomeService.analyzeCallOutcome(callNotes);
        return ResponseEntity.ok(response);
    }
}