package cz.jpmad.releasenotes.ai;

import java.util.List;

public record AiReleaseNotesRequest(
        String version,
        String language,
        List<AiReleaseChange> changes
) {
    public AiReleaseNotesRequest {
        changes = changes == null ? List.of() : List.copyOf(changes);
    }
}
