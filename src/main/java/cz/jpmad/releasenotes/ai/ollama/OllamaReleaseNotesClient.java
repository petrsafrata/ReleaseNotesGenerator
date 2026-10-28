package cz.jpmad.releasenotes.ai.ollama;

import cz.jpmad.releasenotes.ai.AiPrompt;
import cz.jpmad.releasenotes.ai.AiReleaseNotesClient;
import cz.jpmad.releasenotes.ai.AiReleaseNotesResponse;
import cz.jpmad.releasenotes.ai.AiResponseParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OllamaReleaseNotesClient implements AiReleaseNotesClient {

    @NonNull
    private final OllamaApiClient apiClient;

    @NonNull
    private final AiResponseParser responseParser;

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