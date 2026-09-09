package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubApiClientTest {

    @Test
    void loadsRepositoryDetails() {
        AtomicReference<URI> requestedUri = new AtomicReference<>();
        AtomicReference<String> suppliedToken = new AtomicReference<>();

        GitHubHttpClient httpClient = (uri, token) -> {
            requestedUri.set(uri);
            suppliedToken.set(token);

            return new GitHubHttpResponse(
                    200,
                    """
                            {
                              "id": 123456,
                              "full_name": "jpmad/ReleaseNotesGenerator",
                              "description": "Release notes CLI tool",
                              "default_branch": "master",
                              "html_url": "https://github.com/jpmad/ReleaseNotesGenerator",
                              "private": false
                            }
                            """
            );
        };

        GitHubApiClient client = createClient(httpClient);

        GitHubRepositoryDetails details = client.getRepository(new GitHubRepository(
                        "jpmad",
                        "ReleaseNotesGenerator"
                )
        );

        assertEquals(URI.create("https://api.github.com/repos/jpmad/ReleaseNotesGenerator"), requestedUri.get());
        assertEquals("test-token", suppliedToken.get());
        assertEquals("jpmad/ReleaseNotesGenerator", details.fullName());
    }

    @Test
    void loadsPullRequestsForCommit() {
        AtomicReference<URI> requestedUri = new AtomicReference<>();
        AtomicReference<String> suppliedToken = new AtomicReference<>();

        GitHubHttpClient httpClient = (uri, token) -> {
            requestedUri.set(uri);
            suppliedToken.set(token);

            return new GitHubHttpResponse(
                    200,
                    """
                            [
                              {
                                "id": 987654,
                                "number": 42,
                                "title": "Add interactive generation mode",
                                "state": "closed",
                                "html_url": "https://github.com/jpmad/ReleaseNotesGenerator/pull/42",
                                "user": {
                                  "login": "jpmad",
                                  "html_url": "https://github.com/jpmad"
                                }
                              }
                            ]
                            """
            );
        };

        GitHubApiClient client = createClient(httpClient);

        List<GitHubPullRequest> pullRequests = client.getPullRequestsForCommit(new GitHubRepository(
                "jpmad", "ReleaseNotesGenerator"), "f63ce26123456789"
        );

        assertEquals(URI.create("https://api.github.com/repos/"
                        + "jpmad/ReleaseNotesGenerator/"
                        + "commits/f63ce26123456789/pulls"
                ), requestedUri.get()
        );
        assertEquals(
                "test-token",
                suppliedToken.get()
        );
        assertEquals(1, pullRequests.size());
        assertEquals(42, pullRequests.getFirst().number());
    }

    @Test
    void returnsEmptyListWhenCommitHasNoPullRequest() {
        GitHubHttpClient httpClient = (uri, token) -> new GitHubHttpResponse(200, "[]");

        GitHubApiClient client = createClient(httpClient);

        List<GitHubPullRequest> pullRequests = client.getPullRequestsForCommit(
                new GitHubRepository(
                        "jpmad",
                        "ReleaseNotesGenerator"
                ),
                "f63ce26123456789"
        );

        assertTrue(pullRequests.isEmpty());
    }

    @Test
    void reportsMissingResource() {
        GitHubHttpClient httpClient = (uri, token) -> new GitHubHttpResponse(
                404,
                """
                        {
                          "message": "Not Found"
                        }
                        """
        );

        GitHubApiClient client = createClient(httpClient);

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> client.getRepository(
                        new GitHubRepository(
                                "jpmad",
                                "missing-repository"
                        )
                )
        );

        assertEquals("GitHub resource was not found or the token does not have access.", exception.getMessage());
    }

    @Test
    void reportsAuthenticationFailure() {
        GitHubHttpClient httpClient = (uri, token) ->
                new GitHubHttpResponse(401, "");

        GitHubApiClient client = createClient(httpClient);

        GitHubApiException exception = assertThrows(GitHubApiException.class,
                () -> client.getRepository(new GitHubRepository(
                        "jpmad",
                        "ReleaseNotesGenerator")
                )
        );

        assertEquals("GitHub authentication failed. Check the configured token.", exception.getMessage());
    }

    @Test
    void reportsRepositoryConflict() {
        GitHubHttpClient httpClient = (uri, token) ->
                new GitHubHttpResponse(409, "");

        GitHubApiClient client = createClient(httpClient);

        GitHubApiException exception = assertThrows(GitHubApiException.class,
                () -> client.getPullRequestsForCommit(new GitHubRepository(
                        "jpmad", "ReleaseNotesGenerator"
                ), "f63ce26123456789")
        );

        assertEquals("GitHub could not process the repository state.", exception.getMessage());
    }

    private GitHubApiClient createClient(GitHubHttpClient httpClient) {
        return new GitHubApiClient(httpClient, new GitHubResponseParser(new ObjectMapper()),
                new GitHubTokenProvider(
                        Map.of(
                                "GITHUB_TOKEN",
                                "test-token"
                        )
                )
        );
    }
}