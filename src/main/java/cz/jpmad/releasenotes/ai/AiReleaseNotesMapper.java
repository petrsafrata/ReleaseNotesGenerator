package cz.jpmad.releasenotes.ai;

import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseRange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiReleaseNotesMapper {

    public AiReleaseNotesRequest createRequest(ReleaseRange range, String language, List<ReleaseChange> changes) {
        List<AiReleaseChange> aiChanges = changes.stream()
                .map(change -> new AiReleaseChange(
                        resolveIdentifier(change),
                        change.category().name(),
                        change.description()
                ))
                .toList();

        return new AiReleaseNotesRequest(range.to(), language, aiChanges);
    }

    public List<ReleaseChange> applyResponse(List<ReleaseChange> changes, AiReleaseNotesResponse response) {
        Map<String, String> descriptions = new HashMap<>();

        for (AiImprovedChange change : response.changes()) {
            descriptions.put(change.id(), change.description());
        }

        return changes.stream()
                .map(change -> applyDescription(change, descriptions))
                .toList();
    }

    private ReleaseChange applyDescription(ReleaseChange change, Map<String, String> descriptions) {
        String identifier = resolveIdentifier(change);
        String description = descriptions.get(identifier);

        if (description == null) {
            throw new AiException("AI description was not found for change: " + identifier);
        }

        return new ReleaseChange(
                change.category(),
                description,
                change.sourceCommit(),
                change.pullRequest()
        );
    }

    private String resolveIdentifier(ReleaseChange change) {
        if (change.pullRequest().isPresent()) {
            return "pr-" + change.pullRequest().orElseThrow().number();
        }

        return "commit-" + change.sourceCommit().hash();
    }
}