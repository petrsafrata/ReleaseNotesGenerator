package cz.jpmad.releasenotes.github;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class GitHubTokenProvider {

    private static final String TOKEN_VARIABLE = "GITHUB_TOKEN";

    @NonNull
    private final Map<String, String> environment;

    public GitHubTokenProvider() {
        this(System.getenv());
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
