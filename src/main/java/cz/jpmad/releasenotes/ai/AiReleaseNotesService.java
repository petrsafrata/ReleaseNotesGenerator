package cz.jpmad.releasenotes.ai;

import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseRange;

import java.util.List;

public class AiReleaseNotesService {

    private final AiReleaseNotesClient client;
    private final AiPromptBuilder promptBuilder;
    private final AiResponseValidator responseValidator;
    private final AiReleaseNotesMapper mapper;

    public AiReleaseNotesService(AiReleaseNotesClient client, AiPromptBuilder promptBuilder,
                                 AiResponseValidator responseValidator, AiReleaseNotesMapper mapper) {
        this.client = client;
        this.promptBuilder = promptBuilder;
        this.responseValidator = responseValidator;
        this.mapper = mapper;
    }

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