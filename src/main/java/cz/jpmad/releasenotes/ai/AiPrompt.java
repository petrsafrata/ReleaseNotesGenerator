package cz.jpmad.releasenotes.ai;

import com.fasterxml.jackson.databind.JsonNode;

public record AiPrompt(
        String systemMessage,
        String userMessage,
        JsonNode responseFormat
) {
}
