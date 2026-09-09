package cz.jpmad.releasenotes.github;

import java.net.URI;
import java.util.List;

public class GitHubApiClient {

    private static final String API_BASE_URL = "https://api.github.com";

    private final GitHubHttpClient httpClient;
    private final GitHubResponseParser responseParser;
    private final GitHubTokenProvider tokenProvider;

    public GitHubApiClient(GitHubHttpClient httpClient, GitHubResponseParser responseParser,
                           GitHubTokenProvider tokenProvider) {
        this.httpClient = httpClient;
        this.responseParser = responseParser;
        this.tokenProvider = tokenProvider;
    }

    public GitHubRepositoryDetails getRepository(GitHubRepository repository) {
        String token = tokenProvider.requireToken();

        URI uri = URI.create(API_BASE_URL + "/repos/" + repository.owner() + "/" + repository.name());
        GitHubHttpResponse response = httpClient.get(uri, token);
        validateResponse(response);

        return responseParser.parseRepository(response.body());
    }

    public List<GitHubPullRequest> getPullRequestsForCommit(GitHubRepository repository, String commitHash) {
        String token = tokenProvider.requireToken();

        URI uri = URI.create(API_BASE_URL + "/repos/" + repository.owner() + "/" + repository.name() + "/commits/" + commitHash + "/pulls");
        GitHubHttpResponse response = httpClient.get(uri, token);
        validateResponse(response);

        return responseParser.parsePullRequests(response.body());
    }

    private void validateResponse(GitHubHttpResponse response) {
        switch (response.status()) {
            case OK -> {
                return;
            }
            case UNAUTHORIZED ->
                    throw new GitHubApiException("GitHub authentication failed. Check the configured token.");
            case FORBIDDEN -> throw new GitHubApiException("GitHub API access was forbidden.");
            case NOT_FOUND -> throw new GitHubApiException("GitHub resource was not found "
                    + "or the token does not have access."
            );
            case CONFLICT -> throw new GitHubApiException("GitHub could not process the repository state.");
            case TOO_MANY_REQUESTS -> throw new GitHubApiException("GitHub API rate limit was exceeded.");
            case UNKNOWN -> throw new GitHubApiException("GitHub API request failed with status code "
                    + response.statusCode() + "."
            );
        }
    }
}