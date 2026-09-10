package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubLabel(
        long id,
        String name,
        String color,
        String description
) {
}