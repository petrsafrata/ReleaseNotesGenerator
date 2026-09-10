package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubResponseParserTest {

    private final GitHubResponseParser parser = new GitHubResponseParser(new ObjectMapper());

    @Test
    void parsesRepositoryResponse() {
        String responseBody = """
                {
                  "id": 123456,
                  "full_name": "jpmad/ReleaseNotesGenerator",
                  "description": "Release notes CLI tool",
                  "default_branch": "master",
                  "html_url": "https://github.com/jpmad/ReleaseNotesGenerator",
                  "private": false,
                  "stargazers_count": 42
                }
                """;

        GitHubRepositoryDetails repository = parser.parseRepository(responseBody);

        assertEquals(123456, repository.id());
        assertEquals("jpmad/ReleaseNotesGenerator", repository.fullName());
        assertEquals("Release notes CLI tool", repository.description());
        assertEquals("master", repository.defaultBranch());
        assertEquals("https://github.com/jpmad/ReleaseNotesGenerator", repository.htmlUrl());
        assertFalse(repository.privateRepository());
    }

    @Test
    void parsesNullableDescription() {
        String responseBody = """
                {
                  "id": 123456,
                  "full_name": "jpmad/ReleaseNotesGenerator",
                  "description": null,
                  "default_branch": "master",
                  "html_url": "https://github.com/jpmad/ReleaseNotesGenerator",
                  "private": true
                }
                """;

        GitHubRepositoryDetails repository = parser.parseRepository(responseBody);

        assertNull(repository.description());
        assertTrue(repository.privateRepository());
    }

    @Test
    void parsesPullRequestResponse() {
        String responseBody = """
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
                    },
                    "labels": [
                      {
                        "id": 1,
                        "name": "feature",
                        "color": "a2eeef",
                        "description": "New feature"
                      }
                    ],
                    "unknown_field": "ignored"
                  }
                ]
                """;

        List<GitHubPullRequest> pullRequests = parser.parsePullRequests(responseBody);

        assertEquals(1, pullRequests.size());

        GitHubPullRequest pullRequest = pullRequests.getFirst();

        assertEquals(987654, pullRequest.id());
        assertEquals(42, pullRequest.number());
        assertEquals("Add interactive generation mode", pullRequest.title());
        assertEquals("closed", pullRequest.state());
        assertEquals(
                "https://github.com/jpmad/ReleaseNotesGenerator/pull/42",
                pullRequest.htmlUrl()
        );
        assertEquals("jpmad", pullRequest.user().login());
        assertEquals("https://github.com/jpmad", pullRequest.user().htmlUrl());
        assertEquals(1, pullRequest.labels().size());
        assertEquals("feature", pullRequest.labels().getFirst().name());
    }

    @Test
    void parsesEmptyPullRequestResponse() {
        List<GitHubPullRequest> pullRequests = parser.parsePullRequests("[]");

        assertTrue(pullRequests.isEmpty());
    }

    @Test
    void rejectsInvalidRepositoryJson() {
        GitHubApiException exception = assertThrows(GitHubApiException.class,
                () -> parser.parseRepository("invalid JSON")
        );

        assertEquals("Unable to parse GitHub repository response.", exception.getMessage());
    }

    @Test
    void rejectsInvalidPullRequestJson() {
        GitHubApiException exception = assertThrows(GitHubApiException.class,
                () -> parser.parsePullRequests("invalid JSON")
        );

        assertEquals("Unable to parse GitHub pull request response.", exception.getMessage());
    }

    @Test
    void parsesPullRequestLabels() {
        String responseBody = """
            [
              {
                "id": 987654,
                "number": 42,
                "title": "Add label categorization",
                "state": "closed",
                "html_url": "https://github.com/jpmad/ReleaseNotesGenerator/pull/42",
                "user": {
                  "login": "jpmad",
                  "html_url": "https://github.com/jpmad"
                },
                "labels": [
                  {
                    "id": 123,
                    "name": "enhancement",
                    "color": "a2eeef",
                    "description": "New feature or request"
                  }
                ]
              }
            ]
            """;

        List<GitHubPullRequest> pullRequests = parser.parsePullRequests(responseBody);
        GitHubLabel label = pullRequests.getFirst().labels().getFirst();

        assertEquals(123, label.id());
        assertEquals("enhancement", label.name());
        assertEquals("a2eeef", label.color());
        assertEquals("New feature or request", label.description());
    }
}