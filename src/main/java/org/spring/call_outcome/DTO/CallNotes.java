package org.spring.call_outcome.DTO;

import lombok.Data;

@Data
public class CallNotes {
    private String callId;
    private String notes;
    private String timestamp;
}