package cz.jpmad.releasenotes.ai.ollama;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class JavaOllamaHttpClient implements OllamaHttpClient {

    private final HttpClient httpClient;

    public JavaOllamaHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public OllamaHttpResponse get(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        return send(request);
    }

    @Override
    public OllamaHttpResponse post(URI uri, String requestBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofMinutes(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        return send(request);
    }

    private OllamaHttpResponse send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return new OllamaHttpResponse(response.statusCode(), response.body());
        } catch (IOException exception) {
            throw new OllamaException("Unable to communicate with Ollama API.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OllamaException("Ollama API request was interrupted.", exception);
        }
    }
}
