package cz.jpmad.releasenotes.ai.ollama;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RequiredArgsConstructor
public class JavaOllamaHttpClient implements OllamaHttpClient {

    @NonNull
    private final HttpClient httpClient;

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
        return post(uri, requestBody, Duration.ofMinutes(10));
    }

    @Override
    public OllamaHttpResponse post(URI uri, String requestBody, Duration timeout) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        return send(request);
    }

    private OllamaHttpResponse send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return new OllamaHttpResponse(response.statusCode(), response.body());
        } catch (HttpTimeoutException exception) {
            throw new OllamaException("Ollama did not respond within the configured timeout.", exception);
        } catch (IOException exception) {
            if (isConnectionFailure(exception)) {
                throw new OllamaException("Unable to connect to Ollama at " + getBaseUrl(request.uri())
                        + ". Start it using: release-notes ai start", exception);
            }

            throw new OllamaException("Unable to communicate with Ollama API.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OllamaException("Ollama API request was interrupted.", exception);
        }
    }

    private boolean isConnectionFailure(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConnectException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private String getBaseUrl(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority();
    }
}