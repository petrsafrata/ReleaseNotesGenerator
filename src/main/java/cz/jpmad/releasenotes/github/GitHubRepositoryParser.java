package cz.jpmad.releasenotes.github;

import java.net.URI;
import java.util.Optional;

public class GitHubRepositoryParser {

    private static final String GITHUB_HOST = "github.com";
    private static final String SSH_PREFIX = "git@github.com:";

    public Optional<GitHubRepository> parse(String remoteUrl) {
        if (remoteUrl == null || remoteUrl.isBlank()) {
            return Optional.empty();
        }

        String repositoryPath = extractRepositoryPath(remoteUrl.trim());

        if (repositoryPath == null) {
            return Optional.empty();
        }
        return createRepository(repositoryPath);
    }

    private String extractRepositoryPath(String remoteUrl) {
        if (remoteUrl.startsWith(SSH_PREFIX)) {
            return remoteUrl.substring(SSH_PREFIX.length());
        }

        try {
            URI uri = URI.create(remoteUrl);
            if (!GITHUB_HOST.equalsIgnoreCase(uri.getHost())) {
                return null;
            }
            return uri.getPath();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Optional<GitHubRepository> createRepository(String repositoryPath) {
        String normalizedPath = repositoryPath;

        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        while (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }

        if (normalizedPath.endsWith(".git")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 4);
        }

        String[] parts = normalizedPath.split("/");

        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new GitHubRepository(parts[0], parts[1]));
    }
}
