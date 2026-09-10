package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.github.GitHubPullRequest;
import java.util.Optional;

public record ReleaseChange(
        ChangeCategory category,
        String description,
        GitCommit sourceCommit,
        Optional<GitHubPullRequest> pullRequest
) {

    public ReleaseChange(ChangeCategory category, String description, GitCommit sourceCommit) {
        this(category, description, sourceCommit, Optional.empty());
    }

    public ReleaseChange withPullRequest(GitHubPullRequest pullRequest) {
        return new ReleaseChange(category, pullRequest.title(), sourceCommit, Optional.of(pullRequest));
    }
}