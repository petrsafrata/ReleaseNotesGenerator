package cz.jpmad.releasenotes.ai;

import cz.jpmad.releasenotes.release.ReleaseChange;

import java.util.List;

public record AiGenerationResult(
        String summary,
        List<ReleaseChange> changes
) {
    public AiGenerationResult {
        changes = List.copyOf(changes);
    }
}
