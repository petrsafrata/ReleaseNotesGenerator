package cz.jpmad.releasenotes.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AiResponseParser {

    private final ObjectMapper objectMapper;

    public AiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AiReleaseNotesResponse parse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiException("AI release notes response must not be empty.");
        }

        try {
            return objectMapper.readValue(responseBody, AiReleaseNotesResponse.class);
        } catch (JsonProcessingException exception) {
            throw new AiException("Unable to parse AI release notes response.", exception);
        }
    }
}