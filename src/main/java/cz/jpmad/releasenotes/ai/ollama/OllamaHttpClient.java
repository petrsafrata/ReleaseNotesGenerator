package cz.jpmad.releasenotes.ai.ollama;

import java.net.URI;
import java.time.Duration;

public interface OllamaHttpClient {

    OllamaHttpResponse get(URI uri);

    OllamaHttpResponse post(URI uri, String requestBody);

    default OllamaHttpResponse post(URI uri, String requestBody, Duration timeout) {
        return post(uri, requestBody);
    }
}
