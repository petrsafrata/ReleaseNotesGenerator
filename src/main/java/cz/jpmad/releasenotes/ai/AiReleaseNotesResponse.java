package cz.jpmad.releasenotes.ai;

import java.util.List;

public record AiReleaseNotesResponse(
        String summary,
        List<AiImprovedChange> changes
) {
    public AiReleaseNotesResponse {
        changes = changes == null ? List.of() : List.copyOf(changes);
    }
}
