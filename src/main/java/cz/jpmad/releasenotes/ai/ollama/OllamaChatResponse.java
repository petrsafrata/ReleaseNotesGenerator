package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaChatResponse(
        String model,
        OllamaMessage message,
        boolean done,

        @JsonProperty("done_reason")
        String doneReason
) {
}
