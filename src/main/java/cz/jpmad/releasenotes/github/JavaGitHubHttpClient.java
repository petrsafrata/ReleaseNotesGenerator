package cz.jpmad.releasenotes.github;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class JavaGitHubHttpClient implements GitHubHttpClient {

    private static final String API_VERSION = "2026-03-10";
    private static final String USER_AGENT = "release-notes-generator";

    private final HttpClient httpClient;

    public JavaGitHubHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public GitHubHttpResponse get(URI uri, String token) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + token)
                .header("X-GitHub-Api-Version", API_VERSION)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return new GitHubHttpResponse(response.statusCode(), response.body());
        } catch (IOException exception) {
            throw new GitHubApiException("Unable to communicate with GitHub API.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GitHubApiException("GitHub API request was interrupted.", exception);
        }
    }
}
