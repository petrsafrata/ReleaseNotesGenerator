package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jpmad.releasenotes.ai.AiPrompt;
import cz.jpmad.releasenotes.ai.AiReleaseNotesResponse;
import cz.jpmad.releasenotes.ai.AiResponseParser;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaReleaseNotesClientTest {

    @Test
    void generatesReleaseNotesResponse() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        OllamaHttpClient httpClient = new OllamaHttpClient() {
            @Override
            public OllamaHttpResponse get(URI uri) {
                throw new AssertionError("GET request was not expected.");
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody) {
                return new OllamaHttpResponse(
                        200,
                        """
                        {
                          "model": "gemma3:4b",
                          "message": {
                            "role": "assistant",
                            "content": "{\\"summary\\":\\"This release improves GitHub integration.\\",\\"changes\\":[{\\"id\\":\\"pr-42\\",\\"description\\":\\"Added GitHub integration.\\"}]}"
                          },
                          "done": true
                        }
                        """
                );
            }

            @Override
            public OllamaHttpResponse post(URI uri, String requestBody, java.time.Duration timeout) {
                return post(uri, requestBody);
            }
        };

        OllamaApiClient apiClient = new OllamaApiClient(
                URI.create("http://localhost:11434"),
                httpClient,
                new OllamaRequestSerializer(objectMapper),
                new OllamaResponseParser(objectMapper)
        );

        OllamaReleaseNotesClient client = new OllamaReleaseNotesClient(
                apiClient,
                new AiResponseParser(objectMapper)
        );

        AiPrompt prompt = new AiPrompt(
                "System instructions",
                "Release data",
                objectMapper.readTree("""
                        {
                          "type": "object"
                        }
                        """)
        );

        AiReleaseNotesResponse response = client.generate("gemma3:4b", prompt);

        assertEquals("This release improves GitHub integration.", response.summary());
        assertEquals("pr-42", response.changes().getFirst().id());
        assertEquals("Added GitHub integration.", response.changes().getFirst().description());
        assertTrue(response.changes().size() == 1);
    }
}