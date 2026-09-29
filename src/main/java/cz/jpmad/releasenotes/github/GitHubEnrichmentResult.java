package cz.jpmad.releasenotes.github;

import cz.jpmad.releasenotes.release.ReleaseChange;

import java.util.List;

public record GitHubEnrichmentResult(
        List<ReleaseChange> changes,
        String repositoryFullName,
        int pullRequestCount
) {

    public GitHubEnrichmentResult {
        changes = List.copyOf(changes);
    }
}