package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void pullsModel() {
        AtomicReference<URI> requestedUri = new AtomicReference<>();
        AtomicReference<String> suppliedBody = new AtomicReference<>();

        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                throw new AssertionError("GET request was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody) {
                throw new AssertionError("POST request without timeout was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody, Duration timeout) {
                requestedUri.set(uri);
                suppliedBody.set(requestBody);

                return new OllamaHttpResponse(
                        200,
                        """
                        {
                          "status": "success"
                        }
                        """
                );
            }
        };

        OllamaApiClient client = createClient(httpClient);

        client.pullModel("gemma3:4b");

        assertEquals(URI.create("http://localhost:11434/api/pull"), requestedUri.get());
        assertTrue(suppliedBody.get().contains("\"model\":\"gemma3:4b\""));
        assertTrue(suppliedBody.get().contains("\"stream\":false"));
    }

    @Test
    void sendsStructuredChatRequest() throws Exception {
        AtomicReference<URI> requestedUri = new AtomicReference<>();
        AtomicReference<String> suppliedBody = new AtomicReference<>();

        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                throw new AssertionError("GET request was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody) {
                throw new AssertionError("POST request without timeout was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody, Duration timeout) {
                requestedUri.set(uri);
                suppliedBody.set(requestBody);

                return new OllamaHttpResponse(
                        200,
                        """
                        {
                          "model": "gemma3:4b",
                          "message": {
                            "role": "assistant",
                            "content": "{\\"summary\\":\\"Release summary\\",\\"changes\\":[]}"
                          },
                          "done": true,
                          "done_reason": "stop"
                        }
                        """
                );
            }
        };

        OllamaApiClient client = createClient(httpClient);
        JsonNode responseFormat = new ObjectMapper().readTree("""
            {
              "type": "object"
            }
            """);

        String response = client.chat(
                "gemma3:4b",
                List.of(
                        new OllamaMessage("system", "System instructions"),
                        new OllamaMessage("user", "Release data")
                ),
                responseFormat
        );

        assertEquals(URI.create("http://localhost:11434/api/chat"), requestedUri.get());
        assertEquals("{\"summary\":\"Release summary\",\"changes\":[]}", response);

        JsonNode requestBody = new ObjectMapper().readTree(suppliedBody.get());

        assertEquals("gemma3:4b", requestBody.get("model").asText());
        assertFalse(requestBody.get("stream").asBoolean());
        assertEquals(0, requestBody.get("options").get("temperature").asDouble());
        assertEquals("system", requestBody.get("messages").get(0).get("role").asText());
        assertEquals("user", requestBody.get("messages").get(1).get("role").asText());
        assertEquals("object", requestBody.get("format").get("type").asText());
    }

    @Test
    void rejectsEmptyChatResponse() throws Exception {
        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                throw new AssertionError("GET request was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody) {
                throw new AssertionError("POST request without timeout was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody, Duration timeout) {
                return new OllamaHttpResponse(
                        200,
                        """
                        {
                          "model": "gemma3:4b",
                          "message": {
                            "role": "assistant",
                            "content": ""
                          },
                          "done": true
                        }
                        """
                );
            }
        };

        OllamaApiClient client = createClient(httpClient);
        JsonNode responseFormat = new ObjectMapper().readTree("""
            {
              "type": "object"
            }
            """);

        OllamaException exception = assertThrows(
                OllamaException.class,
                () -> client.chat(
                        "gemma3:4b",
                        List.of(new OllamaMessage("user", "Release data")),
                        responseFormat
                )
        );

        assertEquals("Ollama returned an empty chat response.", exception.getMessage());
    }

    @Test
    void reportsMissingChatModel() throws Exception {
        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                throw new AssertionError("GET request was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody) {
                throw new AssertionError("POST request without timeout was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody, Duration timeout) {
                return new OllamaHttpResponse(
                        404,
                        """
                        {
                          "error": "model 'missing-model' not found"
                        }
                        """
                );
            }
        };

        OllamaApiClient client = createClient(httpClient);
        JsonNode responseFormat = new ObjectMapper().readTree("""
        {
          "type": "object"
        }
        """);

        OllamaException exception = assertThrows(
                OllamaException.class,
                () -> client.chat(
                        "missing-model",
                        List.of(new OllamaMessage("user", "Release data")),
                        responseFormat
                )
        );

        assertEquals("Ollama model 'missing-model' is not installed. "
                + "Install it using: release-notes ai pull missing-model", exception.getMessage());
    }

    @Test
    void reportsApiErrorMessage() {
        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                return new OllamaHttpResponse(
                        500,
                        """
                        {
                          "error": "unable to load model"
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

        OllamaException exception = assertThrows(OllamaException.class, client::getModels);

        assertEquals("Ollama API request failed: unable to load model", exception.getMessage());
    }

    private OllamaApiClient createClient(OllamaHttpClient httpClient) {
        return new OllamaApiClient(
                URI.create("http://localhost:11434"),
                httpClient,
                new OllamaRequestSerializer(new ObjectMapper()),
                new OllamaResponseParser(new ObjectMapper())
        );
    }
}