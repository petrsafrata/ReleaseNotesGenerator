package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaModel(
        String name,
        String model,

        @JsonProperty("modified_at")
        String modifiedAt,

        long size,
        String digest,
        OllamaModelDetails details
) {
}