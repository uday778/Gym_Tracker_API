package com.gymtracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private Instant timestamp;
    private int status;
    private String errorCode;
    private String message;
    private Map<String, String> fieldErrors;  // populated only on validation failures
}
