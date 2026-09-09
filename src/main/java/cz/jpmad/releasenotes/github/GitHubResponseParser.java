package cz.jpmad.releasenotes.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class GitHubResponseParser {

    private final ObjectMapper objectMapper;

    public GitHubResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public GitHubRepositoryDetails parseRepository(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, GitHubRepositoryDetails.class);
        } catch (JsonProcessingException exception) {
            throw new GitHubApiException("Unable to parse GitHub repository response.", exception);
        }
    }

    public List<GitHubPullRequest> parsePullRequests(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new GitHubApiException("Unable to parse GitHub pull request response.", exception);
        }
    }
}