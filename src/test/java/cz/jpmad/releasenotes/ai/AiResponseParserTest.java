package cz.jpmad.releasenotes.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiResponseParserTest {

    private final AiResponseParser parser = new AiResponseParser(new ObjectMapper());

    @Test
    void parsesReleaseNotesResponse() {
        AiReleaseNotesResponse response = parser.parse("""
                {
                  "summary": "This release improves GitHub integration.",
                  "changes": [
                    {
                      "id": "pr-2",
                      "description": "Added categorization based on pull request labels."
                    }
                  ]
                }
                """);

        assertEquals("This release improves GitHub integration.", response.summary());
        assertEquals(1, response.changes().size());
        assertEquals("pr-2", response.changes().getFirst().id());
        assertEquals(
                "Added categorization based on pull request labels.",
                response.changes().getFirst().description()
        );
    }

    @Test
    void rejectsInvalidJson() {
        AiException exception = assertThrows(
                AiException.class,
                () -> parser.parse("invalid JSON")
        );

        assertEquals("Unable to parse AI release notes response.", exception.getMessage());
    }

    @Test
    void rejectsEmptyResponse() {
        AiException exception = assertThrows(
                AiException.class,
                () -> parser.parse("")
        );

        assertEquals("AI release notes response must not be empty.", exception.getMessage());
    }

    @Test
    void rejectsNullResponse() {
        AiException exception = assertThrows(
                AiException.class,
                () -> parser.parse(null)
        );

        assertEquals("AI release notes response must not be empty.", exception.getMessage());
    }
}