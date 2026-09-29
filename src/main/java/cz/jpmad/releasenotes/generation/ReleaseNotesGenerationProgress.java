package cz.jpmad.releasenotes.generation;

public record ReleaseNotesGenerationProgress(
        int currentStep,
        int totalSteps,
        String message
) {
}
