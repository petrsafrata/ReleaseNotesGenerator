package cz.jpmad.releasenotes.ai;

public interface AiReleaseNotesClient {

    AiReleaseNotesResponse generate(String model, AiPrompt prompt);
}
