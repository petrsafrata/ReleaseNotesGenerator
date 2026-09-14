package cz.jpmad.releasenotes.ai.ollama;

import java.net.URI;

public interface OllamaHttpClient {

    OllamaHttpResponse get(URI uri);

    OllamaHttpResponse post(URI uri, String requestBody);
}
