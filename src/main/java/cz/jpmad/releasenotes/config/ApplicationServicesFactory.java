package cz.jpmad.releasenotes.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import cz.jpmad.releasenotes.ai.AiPromptBuilder;
import cz.jpmad.releasenotes.ai.AiPromptLoader;
import cz.jpmad.releasenotes.ai.AiReleaseNotesMapper;
import cz.jpmad.releasenotes.ai.AiReleaseNotesService;
import cz.jpmad.releasenotes.ai.AiResponseParser;
import cz.jpmad.releasenotes.ai.AiResponseValidator;
import cz.jpmad.releasenotes.ai.ollama.JavaOllamaHttpClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaReleaseNotesClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaRequestSerializer;
import cz.jpmad.releasenotes.ai.ollama.OllamaResponseParser;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationService;
import cz.jpmad.releasenotes.git.GitCommandExecutor;
import cz.jpmad.releasenotes.git.GitCommitService;
import cz.jpmad.releasenotes.git.GitLogParser;
import cz.jpmad.releasenotes.git.GitRepositoryService;
import cz.jpmad.releasenotes.git.GitTagService;
import cz.jpmad.releasenotes.github.GitHubApiClient;
import cz.jpmad.releasenotes.github.GitHubPullRequestService;
import cz.jpmad.releasenotes.github.GitHubReleaseNotesEnrichmentService;
import cz.jpmad.releasenotes.github.GitHubRepositoryParser;
import cz.jpmad.releasenotes.github.GitHubResponseParser;
import cz.jpmad.releasenotes.github.GitHubTokenProvider;
import cz.jpmad.releasenotes.github.JavaGitHubHttpClient;
import cz.jpmad.releasenotes.release.ChangeCategorizer;
import cz.jpmad.releasenotes.release.MarkdownRenderer;
import cz.jpmad.releasenotes.release.ReleaseChangeEnricher;
import cz.jpmad.releasenotes.release.ReleaseRangeResolver;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.ProcessDockerCommandRunner;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;

public class ApplicationServicesFactory {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public ApplicationServicesFactory() {
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public ReleaseNotesGenerationService createGenerationService(Path workingDirectory, URI ollamaUrl) {
        GitCommandExecutor gitCommandExecutor = new GitCommandExecutor(workingDirectory);
        GitTagService gitTagService = new GitTagService(gitCommandExecutor);

        return new ReleaseNotesGenerationService(
                new GitRepositoryService(gitCommandExecutor),
                new ReleaseRangeResolver(gitTagService),
                new GitCommitService(
                        gitCommandExecutor,
                        new GitLogParser()
                ),
                new ChangeCategorizer(),
                createGitHubEnrichmentService(),
                createAiReleaseNotesService(ollamaUrl),
                new MarkdownRenderer()
        );
    }

    private GitHubReleaseNotesEnrichmentService createGitHubEnrichmentService() {
        GitHubApiClient apiClient = createGitHubApiClient();

        return new GitHubReleaseNotesEnrichmentService(
                apiClient,
                new GitHubRepositoryParser(),
                new GitHubPullRequestService(apiClient),
                new ReleaseChangeEnricher()
        );
    }

    private AiReleaseNotesService createAiReleaseNotesService(URI ollamaUrl) {
        OllamaApiClient ollamaApiClient = createOllamaApiClient(ollamaUrl);

        OllamaReleaseNotesClient ollamaReleaseNotesClient = new OllamaReleaseNotesClient(
                ollamaApiClient,
                new AiResponseParser(objectMapper)
        );

        return new AiReleaseNotesService(
                ollamaReleaseNotesClient,
                new AiPromptBuilder(
                        objectMapper,
                        new AiPromptLoader(objectMapper)
                ),
                new AiResponseValidator(),
                new AiReleaseNotesMapper()
        );
    }

    private GitHubApiClient createGitHubApiClient() {
        return new GitHubApiClient(
                new JavaGitHubHttpClient(httpClient),
                new GitHubResponseParser(objectMapper),
                new GitHubTokenProvider()
        );
    }

    public DockerOllamaService createDockerOllamaService() {
        return new DockerOllamaService(
                new ProcessDockerCommandRunner()
        );
    }

    public OllamaApiClient createOllamaApiClient(URI ollamaUrl) {
        return new OllamaApiClient(
                ollamaUrl,
                new JavaOllamaHttpClient(httpClient),
                new OllamaRequestSerializer(objectMapper),
                new OllamaResponseParser(objectMapper)
        );
    }

    public GitTagService createGitTagService(Path workingDirectory) {
        return new GitTagService(
                new GitCommandExecutor(workingDirectory)
        );
    }
}