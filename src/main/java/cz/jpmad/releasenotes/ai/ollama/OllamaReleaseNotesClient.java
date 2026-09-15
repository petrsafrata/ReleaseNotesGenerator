package cz.jpmad.releasenotes.ai.ollama;

import cz.jpmad.releasenotes.ai.AiPrompt;
import cz.jpmad.releasenotes.ai.AiReleaseNotesClient;
import cz.jpmad.releasenotes.ai.AiReleaseNotesResponse;
import cz.jpmad.releasenotes.ai.AiResponseParser;

import java.util.List;

public class OllamaReleaseNotesClient implements AiReleaseNotesClient {

    private final OllamaApiClient apiClient;
    private final AiResponseParser responseParser;

    public OllamaReleaseNotesClient(OllamaApiClient apiClient, AiResponseParser responseParser) {
        this.apiClient = apiClient;
        this.responseParser = responseParser;
    }

    @Override
    public AiReleaseNotesResponse generate(String model, AiPrompt prompt) {
        String responseBody = apiClient.chat(
                model,
                List.of(
                        new OllamaMessage("system", prompt.systemMessage()),
                        new OllamaMessage("user", prompt.userMessage())
                ),
                prompt.responseFormat()
        );

        return responseParser.parse(responseBody);
    }
}