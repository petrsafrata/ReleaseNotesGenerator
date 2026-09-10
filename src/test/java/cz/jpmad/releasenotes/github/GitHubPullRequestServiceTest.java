package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jpmad.releasenotes.git.GitCommit;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubPullRequestServiceTest {

    private static final GitHubRepository REPOSITORY = new GitHubRepository("jpmad",
            "ReleaseNotesGenerator");

    @Test
    void loadsPullRequestsForCommits() {
        GitHubHttpClient httpClient = (uri, token) -> {
            if (uri.toString().endsWith("/commit-one/pulls")) {
                return responseWithPullRequest(41, "Add configuration support");
            }

            if (uri.toString().endsWith("/commit-two/pulls")) {
                return responseWithPullRequest(42, "Improve error handling");
            }

            throw new AssertionError("Unexpected URI: " + uri);
        };

        GitHubPullRequestService service = createService(httpClient);

        List<GitHubPullRequestAssociation> associations = service.findPullRequests(REPOSITORY, List.of(
                commit("commit-one"), commit("commit-two"))
        );

        assertEquals(2, associations.size());
        assertEquals(41, associations.get(0).pullRequest().number());
        assertEquals("Add configuration support", associations.get(0).pullRequest().title());
        assertEquals(42, associations.get(1).pullRequest().number());
    }

    @Test
    void combinesCommitsBelongingToSamePullRequest() {
        GitHubHttpClient httpClient = (uri, token) -> responseWithPullRequest(42,
                "Add interactive generation mode");

        GitHubPullRequestService service = createService(httpClient);

        List<GitHubPullRequestAssociation> associations = service.findPullRequests(REPOSITORY, List.of(
                commit("commit-one"), commit("commit-two"))
        );

        assertEquals(1, associations.size());

        GitHubPullRequestAssociation association = associations.getFirst();

        assertEquals(42, association.pullRequest().number());
        assertEquals(2, association.commitHashes().size());
        assertTrue(association.commitHashes().contains("commit-one"));
        assertTrue(association.commitHashes().contains("commit-two"));
    }

    @Test
    void ignoresCommitsWithoutPullRequest() {
        GitHubHttpClient httpClient = (uri, token) -> new GitHubHttpResponse(200, "[]");

        GitHubPullRequestService service = createService(httpClient);

        List<GitHubPullRequestAssociation> associations = service.findPullRequests(REPOSITORY, List.of(
                commit("commit-without-pr"))
        );

        assertTrue(associations.isEmpty());
    }

    @Test
    void supportsMultiplePullRequestsForCommit() {
        GitHubHttpClient httpClient = (uri, token) ->
                new GitHubHttpResponse(
                        200,
                        """
                                [
                                  {
                                    "id": 1001,
                                    "number": 41,
                                    "title": "First pull request",
                                    "state": "closed",
                                    "html_url": "https://github.com/jpmad/ReleaseNotesGenerator/pull/41",
                                    "user": {
                                      "login": "jpmad",
                                      "html_url": "https://github.com/jpmad"
                                    }
                                  },
                                  {
                                    "id": 1002,
                                    "number": 42,
                                    "title": "Second pull request",
                                    "state": "open",
                                    "html_url": "https://github.com/jpmad/ReleaseNotesGenerator/pull/42",
                                    "user": {
                                      "login": "contributor",
                                      "html_url": "https://github.com/contributor"
                                    }
                                  }
                                ]
                                """
                );

        GitHubPullRequestService service = createService(httpClient);

        List<GitHubPullRequestAssociation> associations = service.findPullRequests(REPOSITORY, List.of(
                commit("commit-one"))
        );

        assertEquals(2, associations.size());
        assertEquals(41, associations.get(0).pullRequest().number());
        assertEquals(42, associations.get(1).pullRequest().number()
        );
    }

    private GitHubPullRequestService createService(GitHubHttpClient httpClient) {
        GitHubApiClient apiClient =
                new GitHubApiClient(httpClient, new GitHubResponseParser(new ObjectMapper()),
                        new GitHubTokenProvider(
                                Map.of(
                                        "GITHUB_TOKEN",
                                        "test-token"
                                )
                        )
                );

        return new GitHubPullRequestService(apiClient);
    }

    private GitCommit commit(String hash) {
        return new GitCommit(hash, hash, "Test Author", OffsetDateTime.parse("2026-09-09T12:00:00Z"),
                "feat: test change"
        );
    }

    private GitHubHttpResponse responseWithPullRequest(int number, String title) {
        return new GitHubHttpResponse(
                200,
                """
                        [
                          {
                            "id": %d,
                            "number": %d,
                            "title": "%s",
                            "state": "closed",
                            "html_url": "https://github.com/jpmad/ReleaseNotesGenerator/pull/%d",
                            "user": {
                              "login": "jpmad",
                              "html_url": "https://github.com/jpmad"
                            }
                          }
                        ]
                        """.formatted(
                        1000 + number,
                        number,
                        title,
                        number
                )
        );
    }
}