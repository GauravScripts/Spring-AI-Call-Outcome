package org.spring.call_outcome.DTO;

import lombok.Data;

@Data
public class CallOutcomeResponse {
    private String callId;
    private String outcome;
    private String explanation;
}