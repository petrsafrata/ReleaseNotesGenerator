package cz.jpmad.releasenotes.ai;

import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseRange;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class AiReleaseNotesService {

    @NonNull
    private final AiReleaseNotesClient client;

    @NonNull
    private final AiPromptBuilder promptBuilder;

    @NonNull
    private final AiResponseValidator responseValidator;

    @NonNull
    private final AiReleaseNotesMapper mapper;

    public AiGenerationResult improve(String model, String language, ReleaseRange range,
                                      List<ReleaseChange> changes) {
        if (changes.isEmpty()) {
            throw new AiException("AI release changes must not be empty.");
        }

        AiReleaseNotesRequest request = mapper.createRequest(range, language, changes);
        AiPrompt prompt = promptBuilder.build(request);
        AiReleaseNotesResponse response = client.generate(model, prompt);

        responseValidator.validate(request, response);

        List<ReleaseChange> improvedChanges = mapper.applyResponse(changes, response);

        return new AiGenerationResult(response.summary(), improvedChanges);
    }
}