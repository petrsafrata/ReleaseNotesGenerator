package cz.jpmad.releasenotes.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPromptBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiPromptBuilder builder = new AiPromptBuilder(
            objectMapper,
            new AiPromptLoader(objectMapper)
    );

    @Test
    void buildsPromptFromReleaseData() throws Exception {
        AiReleaseNotesRequest request = new AiReleaseNotesRequest(
                "0.0.4",
                "en",
                List.of(
                        new AiReleaseChange(
                                "pr-2",
                                "FEATURES",
                                "Add pull request label categorization"
                        )
                )
        );

        AiPrompt prompt = builder.build(request);

        assertTrue(prompt.systemMessage().contains("You are a release notes editor."));
        assertTrue(prompt.systemMessage().contains("Preserve every change identifier exactly."));
        assertEquals("object", prompt.responseFormat().get("type").asText());

        String json = prompt.userMessage().substring(
                prompt.userMessage().indexOf('{')
        );
        JsonNode releaseData = objectMapper.readTree(json);

        assertEquals("0.0.4", releaseData.get("version").asText());
        assertEquals("en", releaseData.get("language").asText());
        assertEquals("pr-2", releaseData.get("changes").get(0).get("id").asText());
        assertEquals("FEATURES", releaseData.get("changes").get(0).get("category").asText());
    }

    @Test
    void rejectsMissingLanguage() {
        AiReleaseNotesRequest request = new AiReleaseNotesRequest(
                "0.0.4",
                "",
                List.of(
                        new AiReleaseChange(
                                "pr-2",
                                "FEATURES",
                                "Add pull request label categorization"
                        )
                )
        );

        AiException exception = assertThrows(
                AiException.class,
                () -> builder.build(request)
        );

        assertEquals("AI output language must not be empty.", exception.getMessage());
    }

    @Test
    void rejectsEmptyChanges() {
        AiReleaseNotesRequest request = new AiReleaseNotesRequest("0.0.4", "en", List.of());

        AiException exception = assertThrows(
                AiException.class,
                () -> builder.build(request)
        );

        assertEquals("AI release changes must not be empty.", exception.getMessage());
    }
}