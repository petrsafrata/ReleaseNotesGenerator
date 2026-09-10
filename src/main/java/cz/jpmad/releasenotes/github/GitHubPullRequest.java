package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubPullRequest(

        long id,

        int number,

        String title,

        String state,

        @JsonProperty("html_url")
        String htmlUrl,

        GitHubUser user,

        List<GitHubLabel> labels
) {
        public GitHubPullRequest(long id, int number, String title, String state, String htmlUrl, GitHubUser user) {
                this(id, number, title, state, htmlUrl, user, List.of());
        }

        public GitHubPullRequest {
                labels = labels == null ? List.of() : List.copyOf(labels);
        }
}