package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaErrorResponse(
        String error
) {
}