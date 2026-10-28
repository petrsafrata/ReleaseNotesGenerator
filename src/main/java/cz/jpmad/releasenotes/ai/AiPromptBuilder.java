package cz.jpmad.releasenotes.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AiPromptBuilder {

    @NonNull
    private final ObjectMapper objectMapper;

    @NonNull
    private final AiPromptLoader promptLoader;

    public AiPrompt build(AiReleaseNotesRequest request) {
        validateRequest(request);

        try {
            String releaseData = objectMapper.writeValueAsString(request);
            String userMessage = "Rewrite the following release data:\n" + releaseData;

            return new AiPrompt(
                    promptLoader.loadSystemPrompt(),
                    userMessage,
                    promptLoader.loadResponseSchema()
            );
        } catch (JsonProcessingException exception) {
            throw new AiException("Unable to create AI release notes prompt.", exception);
        }
    }

    private void validateRequest(AiReleaseNotesRequest request) {
        if (request == null) {
            throw new AiException("AI release notes request must not be null.");
        }

        if (request.version() == null || request.version().isBlank()) {
            throw new AiException("Release version must not be empty.");
        }

        if (request.language() == null || request.language().isBlank()) {
            throw new AiException("AI output language must not be empty.");
        }

        if (request.changes().isEmpty()) {
            throw new AiException("AI release changes must not be empty.");
        }
    }
}