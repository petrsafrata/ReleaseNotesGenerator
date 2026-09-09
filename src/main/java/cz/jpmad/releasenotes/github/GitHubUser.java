package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubUser(

        String login,

        @JsonProperty("html_url")
        String htmlUrl
) {
}
