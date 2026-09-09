package cz.jpmad.releasenotes.github;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitHubHttpStatusTest {

    @Test
    void resolvesKnownStatusCode() {
        assertEquals(GitHubHttpStatus.NOT_FOUND, GitHubHttpStatus.fromCode(404));
    }

    @Test
    void resolvesUnknownStatusCode() {
        assertEquals(GitHubHttpStatus.UNKNOWN, GitHubHttpStatus.fromCode(418));
    }

    @Test
    void exposesStatusThroughResponse() {
        GitHubHttpResponse response = new GitHubHttpResponse(200, "{}");
        assertEquals(GitHubHttpStatus.OK, response.status());
        assertEquals("OK", response.status().label());
    }
}