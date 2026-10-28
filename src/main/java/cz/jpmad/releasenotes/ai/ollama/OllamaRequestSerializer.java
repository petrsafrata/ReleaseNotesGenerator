package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OllamaRequestSerializer {

    @NonNull
    private final ObjectMapper objectMapper;

    public String serializePullRequest(OllamaPullRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new OllamaException("Unable to create Ollama pull request.", exception);
        }
    }

    public String serializeChatRequest(OllamaChatRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new OllamaException("Unable to create Ollama chat request.", exception);
        }
    }
}
