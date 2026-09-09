package cz.jpmad.releasenotes.github;

import java.net.URI;

@FunctionalInterface
public interface GitHubHttpClient {

    GitHubHttpResponse get(URI uri, String token);
}