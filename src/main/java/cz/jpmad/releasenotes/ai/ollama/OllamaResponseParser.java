package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OllamaResponseParser {

    private final ObjectMapper objectMapper;

    public OllamaResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OllamaModelList parseModels(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, OllamaModelList.class);
        } catch (JsonProcessingException exception) {
            throw new OllamaException("Unable to parse Ollama model response.", exception);
        }
    }
}