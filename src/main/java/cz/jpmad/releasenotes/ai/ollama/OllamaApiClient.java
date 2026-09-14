package cz.jpmad.releasenotes.ai.ollama;

import java.net.URI;
import java.util.List;

public class OllamaApiClient {

    private final URI baseUri;
    private final OllamaHttpClient httpClient;
    private final OllamaResponseParser responseParser;

    public OllamaApiClient(URI baseUri, OllamaHttpClient httpClient, OllamaResponseParser responseParser) {
        this.baseUri = baseUri;
        this.httpClient = httpClient;
        this.responseParser = responseParser;
    }

    public List<OllamaModel> getModels() {
        URI uri = baseUri.resolve("/api/tags");
        OllamaHttpResponse response = httpClient.get(uri);

        validateResponse(response);

        return responseParser.parseModels(response.body()).models();
    }

    private void validateResponse(OllamaHttpResponse response) {
        if (response.successful()) {
            return;
        }

        throw new OllamaException("Ollama API request failed with status code " + response.statusCode() + ".");
    }
}