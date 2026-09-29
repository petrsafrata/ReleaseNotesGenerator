package cz.jpmad.releasenotes.generation;

public record ReleaseNotesGenerationRequest(
        String from,
        String to,
        boolean githubEnabled,
        boolean aiEnabled,
        String model,
        String language
) {
}
