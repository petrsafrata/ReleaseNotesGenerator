package cz.jpmad.releasenotes.github;

import java.util.Set;

public record GitHubPullRequestAssociation(
        GitHubPullRequest pullRequest,
        Set<String> commitHashes
) {
}
