package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OllamaApiClientTest {

    @Test
    void loadsInstalledModels() {
        AtomicReference<URI> requestedUri = new AtomicReference<>();

        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                requestedUri.set(uri);

                return new OllamaHttpResponse(
                        200,
                        """
                        {
                          "models": [
                            {
                              "name": "gemma3:4b",
                              "model": "gemma3:4b",
                              "size": 3338801804,
                              "digest": "abc123",
                              "details": {
                                "format": "gguf",
                                "family": "gemma3",
                                "parameter_size": "4.3B",
                                "quantization_level": "Q4_K_M"
                              }
                            }
                          ]
                        }
                        """
                );
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody) {
                throw new AssertionError("POST request was not expected.");
            }
        };

        OllamaApiClient client = createClient(httpClient);

        List<OllamaModel> models = client.getModels();

        assertEquals(URI.create("http://localhost:11434/api/tags"), requestedUri.get());
        assertEquals(1, models.size());
        assertEquals("gemma3:4b", models.getFirst().name());
    }

    @Test
    void reportsApiError() {
        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                return new OllamaHttpResponse(500, "");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody) {
                throw new AssertionError("POST request was not expected.");
            }
        };

        OllamaApiClient client = createClient(httpClient);

        OllamaException exception = assertThrows(OllamaException.class, client::getModels);

        assertEquals("Ollama API request failed with status code 500.", exception.getMessage());
    }

    private OllamaApiClient createClient(OllamaHttpClient httpClient) {
        return new OllamaApiClient(
                URI.create("http://localhost:11434"),
                httpClient,
                new OllamaResponseParser(new ObjectMapper())
        );
    }
}