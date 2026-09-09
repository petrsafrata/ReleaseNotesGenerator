package cz.jpmad.releasenotes.github;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubTokenProviderTest {

    @Test
    void returnsConfiguredToken() {
        GitHubTokenProvider provider = new GitHubTokenProvider(Map.of("GITHUB_TOKEN", "test-token"));
        assertEquals("test-token", provider.requireToken());
    }

    @Test
    void trimsConfiguredToken() {
        GitHubTokenProvider provider = new GitHubTokenProvider(Map.of("GITHUB_TOKEN", "  test-token  "));
        assertEquals("test-token", provider.requireToken());
    }

    @Test
    void returnsEmptyWhenTokenIsMissing() {
        GitHubTokenProvider provider = new GitHubTokenProvider(Map.of());
        assertTrue(provider.findToken().isEmpty());
    }

    @Test
    void rejectsMissingToken() {
        GitHubTokenProvider provider = new GitHubTokenProvider(Map.of());
        GitHubApiException exception = assertThrows(GitHubApiException.class, provider::requireToken);

        assertEquals("GitHub token is not configured. Set the GITHUB_TOKEN environment variable.",
                exception.getMessage());
    }

    @Test
    void rejectsBlankToken() {
        GitHubTokenProvider provider = new GitHubTokenProvider(Map.of("GITHUB_TOKEN", "   "));
        assertThrows(GitHubApiException.class, provider::requireToken);
    }
}
