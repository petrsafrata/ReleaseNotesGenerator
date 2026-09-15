package cz.jpmad.releasenotes.ai.ollama;

public record OllamaPullRequest(
        String model,
        boolean stream
) {
    public OllamaPullRequest(String model) {
        this(model, false);
    }
}
