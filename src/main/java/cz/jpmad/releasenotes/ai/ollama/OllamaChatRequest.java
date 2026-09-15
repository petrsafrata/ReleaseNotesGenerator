package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record OllamaChatRequest(
        String model,
        List<OllamaMessage> messages,
        boolean stream,
        JsonNode format,
        OllamaOptions options
) {
    public OllamaChatRequest(String model, List<OllamaMessage> messages, JsonNode format) {
        this(model, List.copyOf(messages), false, format, new OllamaOptions(0));
    }
}
