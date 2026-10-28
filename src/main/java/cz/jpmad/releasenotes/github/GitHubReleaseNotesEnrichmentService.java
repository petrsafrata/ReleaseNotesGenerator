package cz.jpmad.releasenotes.github;

import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.git.GitRepositoryInfo;
import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseChangeEnricher;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class GitHubReleaseNotesEnrichmentService {

    @NonNull
    private final GitHubApiClient apiClient;

    @NonNull
    private final GitHubRepositoryParser repositoryParser;

    @NonNull
    private final GitHubPullRequestService pullRequestService;

    @NonNull
    private final ReleaseChangeEnricher changeEnricher;

    public GitHubEnrichmentResult enrich(GitRepositoryInfo repositoryInfo, List<GitCommit> commits,
                                         List<ReleaseChange> changes) {
        String remoteUrl = repositoryInfo.remoteUrl()
                .orElseThrow(() -> new GitHubApiException("Git remote 'origin' is not configured.")
                );

        GitHubRepository repository = repositoryParser.parse(remoteUrl)
                .orElseThrow(() -> new GitHubApiException("Remote 'origin' is not a supported "
                        + "GitHub repository URL.")
                );

        GitHubRepositoryDetails details = apiClient.getRepository(repository);
        List<GitHubPullRequestAssociation> associations = pullRequestService.findPullRequests(repository, commits);
        List<ReleaseChange> enrichedChanges = changeEnricher.enrich(changes, associations);

        log.debug("Loaded GitHub repository: {}", details.fullName());
        log.debug("Loaded {} GitHub pull request associations.", associations.size());

        return new GitHubEnrichmentResult(
                enrichedChanges,
                details.fullName(),
                associations.size()
        );
    }
}