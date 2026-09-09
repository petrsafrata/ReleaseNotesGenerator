package cz.jpmad.releasenotes.github;

import java.util.Map;
import java.util.Optional;

public class GitHubTokenProvider {

    private static final String TOKEN_VARIABLE = "GITHUB_TOKEN";

    private final Map<String, String> environment;

    public GitHubTokenProvider() {
        this(System.getenv());
    }

    GitHubTokenProvider(Map<String, String> environment) {
        this.environment = environment;
    }

    public Optional<String> findToken() {
        return Optional.ofNullable(environment.get(TOKEN_VARIABLE))
                .map(String::trim)
                .filter(token -> !token.isBlank());
    }

    public String requireToken() {
        return findToken().orElseThrow(()
                -> new GitHubApiException("GitHub token is not configured. Set the GITHUB_TOKEN environment variable."));
    }
}
