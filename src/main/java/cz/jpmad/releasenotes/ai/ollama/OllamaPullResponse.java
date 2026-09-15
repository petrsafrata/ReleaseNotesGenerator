package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaPullResponse(
        String status
) {
    public boolean successful() {
        return "success".equalsIgnoreCase(status);
    }
}
