package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubPullRequest(

        long id,

        int number,

        String title,

        String state,

        @JsonProperty("html_url")
        String htmlUrl,

        GitHubUser user
) {
}
