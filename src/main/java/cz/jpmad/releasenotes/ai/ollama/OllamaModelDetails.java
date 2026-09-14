package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaModelDetails(
        String format,
        String family,

        @JsonProperty("parameter_size")
        String parameterSize,

        @JsonProperty("quantization_level")
        String quantizationLevel
) {
}