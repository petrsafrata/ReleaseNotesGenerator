package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OllamaResponseParser {

    @NonNull
    private final ObjectMapper objectMapper;

    public OllamaModelList parseModels(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, OllamaModelList.class);
        } catch (JsonProcessingException exception) {
            throw new OllamaException("Unable to parse Ollama model response.", exception);
        }
    }

    public OllamaPullResponse parsePullResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, OllamaPullResponse.class);
        } catch (JsonProcessingException exception) {
            throw new OllamaException("Unable to parse Ollama pull response.", exception);
        }
    }

    public OllamaChatResponse parseChatResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, OllamaChatResponse.class);
        } catch (JsonProcessingException exception) {
            throw new OllamaException("Unable to parse Ollama chat response.", exception);
        }
    }

    public String parseErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        try {
            OllamaErrorResponse response = objectMapper.readValue(responseBody, OllamaErrorResponse.class);
            return response.error();
        } catch (JsonProcessingException exception) {
            return null;
        }
    }
}