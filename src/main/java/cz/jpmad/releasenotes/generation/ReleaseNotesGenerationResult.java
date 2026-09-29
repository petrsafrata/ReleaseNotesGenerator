package cz.jpmad.releasenotes.generation;

import java.util.Optional;

public record ReleaseNotesGenerationResult(
        String markdown,
        Optional<String> githubRepositoryFullName,
        int githubPullRequestCount,
        boolean aiImproved
) {

    public ReleaseNotesGenerationResult {
        githubRepositoryFullName = githubRepositoryFullName == null
                ? Optional.empty()
                : githubRepositoryFullName;
    }
}
