package cz.jpmad.releasenotes.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
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

            if (githubEnabled) {
                changes = enrichWithGitHubData(repositoryInfo, commits, changes);
            }

            String markdown = markdownRenderer.render(range, changes);
            writeOutput(markdown);

            return 0;
        } catch (GitCommandException | GitHubApiException | ReleaseRangeException | IOException exception
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
}