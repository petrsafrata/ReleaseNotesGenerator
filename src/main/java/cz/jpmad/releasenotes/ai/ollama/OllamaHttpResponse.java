package cz.jpmad.releasenotes.ai.ollama;

public record OllamaHttpResponse(
        int statusCode,
        String body
) {
    public boolean successful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
