package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepositoryDetails(

        long id,

        @JsonProperty("full_name")
        String fullName,

        String description,

        @JsonProperty("default_branch")
        String defaultBranch,

        @JsonProperty("html_url")
        String htmlUrl,

        @JsonProperty("private")
        boolean privateRepository
) {
}
