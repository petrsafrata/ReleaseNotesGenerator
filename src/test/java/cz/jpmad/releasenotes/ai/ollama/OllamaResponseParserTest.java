package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaResponseParserTest {

    private final OllamaResponseParser parser = new OllamaResponseParser(new ObjectMapper());

    @Test
    void parsesModelList() {
        String responseBody = """
                {
                  "models": [
                    {
                      "name": "gemma3:4b",
                      "model": "gemma3:4b",
                      "modified_at": "2026-09-14T12:00:00Z",
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
                """;

        List<OllamaModel> models = parser.parseModels(responseBody).models();

        assertEquals(1, models.size());

        OllamaModel model = models.getFirst();

        assertEquals("gemma3:4b", model.name());
        assertEquals("gemma3:4b", model.model());
        assertEquals(3338801804L, model.size());
        assertEquals("abc123", model.digest());
        assertEquals("gguf", model.details().format());
        assertEquals("gemma3", model.details().family());
        assertEquals("4.3B", model.details().parameterSize());
        assertEquals("Q4_K_M", model.details().quantizationLevel());
    }

    @Test
    void parsesEmptyModelList() {
        OllamaModelList result = parser.parseModels("""
                {
                  "models": []
                }
                """);

        assertTrue(result.models().isEmpty());
    }

    @Test
    void handlesMissingModelsProperty() {
        OllamaModelList result = parser.parseModels("{}");

        assertTrue(result.models().isEmpty());
    }

    @Test
    void rejectsInvalidJson() {
        OllamaException exception = assertThrows(
                OllamaException.class,
                () -> parser.parseModels("invalid JSON")
        );

        assertEquals("Unable to parse Ollama model response.", exception.getMessage());
    }
}