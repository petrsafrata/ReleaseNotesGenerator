package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
public class OllamaApiClient {

    @NonNull
    private final URI baseUri;

    @NonNull
    private final OllamaHttpClient httpClient;

    @NonNull
    private final OllamaRequestSerializer requestSerializer;

    @NonNull
    private final OllamaResponseParser responseParser;

    public List<OllamaModel> getModels() {
        URI uri = baseUri.resolve("/api/tags");
        OllamaHttpResponse response = httpClient.get(uri);

        validateResponse(response);

        return responseParser.parseModels(response.body()).models();
    }

    public void pullModel(String model) {
        if (model == null || model.isBlank()) {
            throw new OllamaException("Ollama model name must not be empty.");
        }

        URI uri = baseUri.resolve("/api/pull");
        String requestBody = requestSerializer.serializePullRequest(new OllamaPullRequest(model));
        OllamaHttpResponse response = httpClient.post(uri, requestBody, Duration.ofHours(1));

        validateResponse(response);

        OllamaPullResponse pullResponse = responseParser.parsePullResponse(response.body());

        if (!pullResponse.successful()) {
            throw new OllamaException("Ollama did not confirm that the model was downloaded.");
        }
    }

    public String chat(String model, List<OllamaMessage> messages, JsonNode responseFormat) {
        if (model == null || model.isBlank()) {
            throw new OllamaException("Ollama model name must not be empty.");
        }

        if (messages == null || messages.isEmpty()) {
            throw new OllamaException("Ollama chat messages must not be empty.");
        }

        if (responseFormat == null) {
            throw new OllamaException("Ollama response format must not be null.");
        }

        URI uri = baseUri.resolve("/api/chat");
        OllamaChatRequest chatRequest = new OllamaChatRequest(model, messages, responseFormat);
        String requestBody = requestSerializer.serializeChatRequest(chatRequest);
        OllamaHttpResponse response = httpClient.post(uri, requestBody, Duration.ofMinutes(10));

        validateChatResponse(response, model);

        OllamaChatResponse chatResponse = responseParser.parseChatResponse(response.body());

        if (chatResponse.message() == null || chatResponse.message().content() == null
                || chatResponse.message().content().isBlank()) {
            throw new OllamaException("Ollama returned an empty chat response.");
        }

        return chatResponse.message().content();
    }

    private void validateChatResponse(OllamaHttpResponse response, String model) {
        if (response.successful()) {
            return;
        }

        String errorMessage = responseParser.parseErrorMessage(response.body());

        if (response.statusCode() == 404 && errorMessage != null
                && errorMessage.toLowerCase().contains("model")) {
            throw new OllamaException("Ollama model '" + model + "' is not installed. "
                    + "Install it using: release-notes ai pull " + model);
        }

        throw createApiException(response, errorMessage);
    }

    private void validateResponse(OllamaHttpResponse response) {
        if (response.successful()) {
            return;
        }

        String errorMessage = responseParser.parseErrorMessage(response.body());

        throw createApiException(response, errorMessage);
    }

    private OllamaException createApiException(OllamaHttpResponse response, String errorMessage) {
        if (errorMessage != null && !errorMessage.isBlank()) {
            return new OllamaException("Ollama API request failed: " + errorMessage);
        }

        return new OllamaException("Ollama API request failed with status code " + response.statusCode() + ".");
    }
}