package cz.jpmad.releasenotes.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jpmad.releasenotes.ai.AiException;
import cz.jpmad.releasenotes.ai.AiGenerationResult;
import cz.jpmad.releasenotes.ai.AiPromptBuilder;
import cz.jpmad.releasenotes.ai.AiPromptLoader;
import cz.jpmad.releasenotes.ai.AiReleaseNotesMapper;
import cz.jpmad.releasenotes.ai.AiReleaseNotesService;
import cz.jpmad.releasenotes.ai.AiResponseParser;
import cz.jpmad.releasenotes.ai.AiResponseValidator;
import cz.jpmad.releasenotes.ai.ollama.JavaOllamaHttpClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.ai.ollama.OllamaReleaseNotesClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaRequestSerializer;
import cz.jpmad.releasenotes.ai.ollama.OllamaResponseParser;
import cz.jpmad.releasenotes.git.GitCommandException;
import cz.jpmad.releasenotes.git.GitCommandExecutor;
import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.git.GitCommitService;
import cz.jpmad.releasenotes.git.GitLogParser;
import cz.jpmad.releasenotes.git.GitRepositoryInfo;
import cz.jpmad.releasenotes.git.GitRepositoryService;
import cz.jpmad.releasenotes.git.GitTagService;
import cz.jpmad.releasenotes.github.GitHubApiClient;
import cz.jpmad.releasenotes.github.GitHubApiException;
import cz.jpmad.releasenotes.github.GitHubPullRequestAssociation;
import cz.jpmad.releasenotes.github.GitHubPullRequestService;
import cz.jpmad.releasenotes.github.GitHubRepository;
import cz.jpmad.releasenotes.github.GitHubRepositoryDetails;
import cz.jpmad.releasenotes.github.GitHubRepositoryParser;
import cz.jpmad.releasenotes.github.GitHubResponseParser;
import cz.jpmad.releasenotes.github.GitHubTokenProvider;
import cz.jpmad.releasenotes.github.JavaGitHubHttpClient;
import cz.jpmad.releasenotes.output.MarkdownOutputWriter;
import cz.jpmad.releasenotes.release.ChangeCategorizer;
import cz.jpmad.releasenotes.release.MarkdownRenderer;
import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseChangeEnricher;
import cz.jpmad.releasenotes.release.ReleaseRange;
import cz.jpmad.releasenotes.release.ReleaseRangeException;
import cz.jpmad.releasenotes.release.ReleaseRangeResolver;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "generate",
        description = "Generate release notes between two Git references.",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = "--from",
            description = "Previous release reference. "
                    + "Defaults to the tag preceding the target release."
    )
    private String from;

    @CommandLine.Option(
            names = "--to",
            description = "Target tagged reference.",
            defaultValue = "HEAD"
    )
    private String to;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Write release notes to a Markdown file."
    )
    private Path output;

    @CommandLine.Option(
            names = "--github",
            description = "Enrich release notes with data from GitHub API."
    )
    private boolean githubEnabled;

    @CommandLine.Option(
            names = "--ai",
            description = "Improve release notes using the local Ollama AI service."
    )
    private boolean aiEnabled;

    @CommandLine.Option(
            names = "--model",
            description = "Ollama model used to improve release notes.",
            defaultValue = "gemma3:4b"
    )
    private String model;

    @CommandLine.Option(
            names = "--language",
            description = "Language used for AI-generated release notes.",
            defaultValue = "en"
    )
    private String language;

    @CommandLine.Option(
            names = "--ollama-url",
            description = "Base URL of the Ollama API.",
            defaultValue = "http://localhost:11434"
    )
    private URI ollamaUrl;

    @Override
    public Integer call() {
        try {
            GitCommandExecutor gitCommandExecutor = new GitCommandExecutor(Path.of("."));
            GitRepositoryService gitRepositoryService = new GitRepositoryService(gitCommandExecutor);
            GitTagService gitTagService = new GitTagService(gitCommandExecutor);
            ReleaseRangeResolver rangeResolver = new ReleaseRangeResolver(gitTagService);
            GitCommitService gitCommitService = new GitCommitService(gitCommandExecutor, new GitLogParser());
            ChangeCategorizer changeCategorizer = new ChangeCategorizer();
            MarkdownRenderer markdownRenderer = new MarkdownRenderer();
            GitRepositoryInfo repositoryInfo = gitRepositoryService.getRepositoryInfo();
            ReleaseRange range = rangeResolver.resolve(from, to);
            List<GitCommit> commits = gitCommitService.findCommits(range);
            List<ReleaseChange> changes = changeCategorizer.categorize(commits);
            String summary = null;

            if (githubEnabled) {
                changes = enrichWithGitHubData(repositoryInfo, commits, changes);
            }

            if (aiEnabled && !changes.isEmpty()) {
                AiGenerationResult result = improveWithAi(range, changes);
                summary = result.summary();
                changes = result.changes();
            }

            String markdown = markdownRenderer.render(range, changes, summary);
            writeOutput(markdown);

            return 0;
        } catch (AiException | GitCommandException | GitHubApiException | OllamaException
                 | ReleaseRangeException | IOException exception
        ) {
            System.err.println("Error: " + exception.getMessage());
            return 1;
        }
    }

    private void writeOutput(String markdown) throws IOException {
        if (output == null) {
            System.out.print(markdown);
            return;
        }

        MarkdownOutputWriter markdownOutputWriter = new MarkdownOutputWriter();
        Path writtenFile = markdownOutputWriter.write(output, markdown);

        System.err.println("Release notes written to: " + writtenFile);
    }

    private List<ReleaseChange> enrichWithGitHubData(GitRepositoryInfo repositoryInfo, List<GitCommit> commits,
                                                     List<ReleaseChange> changes) {
        String remoteUrl = repositoryInfo.remoteUrl()
                .orElseThrow(() -> new GitHubApiException("Git remote 'origin' is not configured.")
                );

        GitHubRepository repository =
                new GitHubRepositoryParser()
                        .parse(remoteUrl)
                        .orElseThrow(() -> new GitHubApiException("Remote 'origin' is not a supported "
                                + "GitHub repository URL.")
                        );

        GitHubApiClient gitHubApiClient = createGitHubApiClient();
        GitHubRepositoryDetails details = gitHubApiClient.getRepository(repository);

        GitHubPullRequestService pullRequestService = new GitHubPullRequestService(gitHubApiClient);
        List<GitHubPullRequestAssociation> associations = pullRequestService.findPullRequests(repository, commits);

        ReleaseChangeEnricher changeEnricher = new ReleaseChangeEnricher();
        List<ReleaseChange> enrichedChanges = changeEnricher.enrich(changes, associations);

        System.err.println("GitHub repository loaded: " + details.fullName());
        System.err.println("GitHub pull requests loaded: " + associations.size());

        return enrichedChanges;
    }

    private AiGenerationResult improveWithAi(ReleaseRange range, List<ReleaseChange> changes) {
        AiReleaseNotesService aiReleaseNotesService = createAiReleaseNotesService();
        AiGenerationResult result = aiReleaseNotesService.improve(model, language, range, changes);

        System.err.println("Release notes improved using Ollama model: " + model);

        return result;
    }

    private GitHubApiClient createGitHubApiClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        return new GitHubApiClient(
                new JavaGitHubHttpClient(httpClient),
                new GitHubResponseParser(
                        new ObjectMapper()
                ),
                new GitHubTokenProvider()
        );
    }

    private AiReleaseNotesService createAiReleaseNotesService() {
        ObjectMapper objectMapper = new ObjectMapper();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        OllamaApiClient ollamaApiClient = new OllamaApiClient(
                ollamaUrl,
                new JavaOllamaHttpClient(httpClient),
                new OllamaRequestSerializer(objectMapper),
                new OllamaResponseParser(objectMapper)
        );

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
}