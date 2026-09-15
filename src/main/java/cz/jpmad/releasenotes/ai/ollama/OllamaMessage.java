package cz.jpmad.releasenotes.ai.ollama;

public record OllamaMessage(
        String role,
        String content
) {
}
