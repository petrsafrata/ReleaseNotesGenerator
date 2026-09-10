package cz.jpmad.releasenotes.github;

import cz.jpmad.releasenotes.git.GitCommit;

import java.util.*;

public class GitHubPullRequestService {

    private final GitHubApiClient apiClient;

    public GitHubPullRequestService(GitHubApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<GitHubPullRequestAssociation> findPullRequests(GitHubRepository repository, List<GitCommit> commits) {
        Map<Integer, MutableAssociation> associations = new LinkedHashMap<>();

        for (GitCommit commit : commits) {
            List<GitHubPullRequest> pullRequests = apiClient.getPullRequestsForCommit(repository, commit.hash());

            for (GitHubPullRequest pullRequest : pullRequests) {
                MutableAssociation association = associations.computeIfAbsent(pullRequest.number(),
                        ignored -> new MutableAssociation(pullRequest)
                        );

                association.commitHashes().add(commit.hash());
            }
        }

        return associations.values()
                .stream()
                .map(association ->
                        new GitHubPullRequestAssociation(
                                association.pullRequest(),
                                Set.copyOf(association.commitHashes())
                        )
                )
                .toList();
    }

    private record MutableAssociation(GitHubPullRequest pullRequest, LinkedHashSet<String> commitHashes) {
        private MutableAssociation(GitHubPullRequest pullRequest) {
            this(pullRequest, new LinkedHashSet<>());
        }
    }
}