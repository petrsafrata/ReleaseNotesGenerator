package cz.jpmad.releasenotes.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class AiPromptLoader {

    private static final String SYSTEM_PROMPT = "prompts/release-notes-system.md";
    private static final String RESPONSE_SCHEMA = "prompts/release-notes-schema.json";

    @NonNull
    private final ObjectMapper objectMapper;

    public String loadSystemPrompt() {
        return loadResource(SYSTEM_PROMPT);
    }

    public JsonNode loadResponseSchema() {
        String schema = loadResource(RESPONSE_SCHEMA);

        try {
            return objectMapper.readTree(schema);
        } catch (JsonProcessingException exception) {
            throw new AiException("Unable to parse AI response schema.", exception);
        }
    }

    private String loadResource(String resourceName) {
        ClassLoader classLoader = AiPromptLoader.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new AiException("AI resource was not found: " + resourceName);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException exception) {
            throw new AiException("Unable to read AI resource: " + resourceName, exception);
        }
    }
}