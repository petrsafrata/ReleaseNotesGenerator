package cz.jpmad.releasenotes.github;

public record GitHubHttpResponse(
        int statusCode,
        String body
) {
    public GitHubHttpStatus status() {
        return GitHubHttpStatus.fromCode(statusCode);
    }
}

