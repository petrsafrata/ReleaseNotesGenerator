package cz.jpmad.releasenotes.generation;

import cz.jpmad.releasenotes.ai.AiGenerationResult;
import cz.jpmad.releasenotes.ai.AiReleaseNotesService;
import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.git.GitCommitService;
import cz.jpmad.releasenotes.git.GitRepositoryInfo;
import cz.jpmad.releasenotes.git.GitRepositoryService;
import cz.jpmad.releasenotes.github.GitHubEnrichmentResult;
import cz.jpmad.releasenotes.github.GitHubReleaseNotesEnrichmentService;
import cz.jpmad.releasenotes.release.ChangeCategorizer;
import cz.jpmad.releasenotes.release.MarkdownRenderer;
import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseRange;
import cz.jpmad.releasenotes.release.ReleaseRangeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Log4j2
@RequiredArgsConstructor
public class ReleaseNotesGenerationService {

    private final GitRepositoryService repositoryService;
    private final ReleaseRangeResolver rangeResolver;
    private final GitCommitService commitService;
    private final ChangeCategorizer changeCategorizer;
    private final GitHubReleaseNotesEnrichmentService githubEnrichmentService;
    private final AiReleaseNotesService aiReleaseNotesService;
    private final MarkdownRenderer markdownRenderer;

    public ReleaseNotesGenerationResult generate(
            ReleaseNotesGenerationRequest request
    ) {
        return generate(
                request,
                ignored -> {
                }
        );
    }

    public ReleaseNotesGenerationResult generate(
            ReleaseNotesGenerationRequest request,
            Consumer<ReleaseNotesGenerationProgress> progressListener
    ) {
        Objects.requireNonNull(
                request,
                "Release notes generation request must not be null."
        );

        Objects.requireNonNull(
                progressListener,
                "Release notes generation progress listener must not be null."
        );

        int totalSteps = calculateTotalSteps(request);
        int currentStep = 1;

        reportProgress(
                progressListener,
                currentStep++,
                totalSteps,
                "Reading Git repository and resolving release range..."
        );

        GitRepositoryInfo repositoryInfo =
                repositoryService.getRepositoryInfo();

        ReleaseRange range =
                rangeResolver.resolve(
                        request.from(),
                        request.to()
                );

        reportProgress(
                progressListener,
                currentStep++,
                totalSteps,
                "Loading and categorizing commits..."
        );

        List<GitCommit> commits =
                commitService.findCommits(range);

        List<ReleaseChange> changes =
                changeCategorizer.categorize(commits);

        Optional<String> githubRepositoryFullName =
                Optional.empty();

        int githubPullRequestCount = 0;

        if (request.githubEnabled()) {
            reportProgress(
                    progressListener,
                    currentStep++,
                    totalSteps,
                    "Loading GitHub pull requests..."
            );

            GitHubEnrichmentResult githubResult =
                    githubEnrichmentService.enrich(
                            repositoryInfo,
                            commits,
                            changes
                    );

            changes = githubResult.changes();

            githubRepositoryFullName =
                    Optional.of(
                            githubResult.repositoryFullName()
                    );

            githubPullRequestCount =
                    githubResult.pullRequestCount();
        }

        String summary = null;
        boolean aiImproved = false;

        if (request.aiEnabled()) {
            reportProgress(
                    progressListener,
                    currentStep++,
                    totalSteps,
                    "Improving release notes using AI model "
                            + request.model()
                            + "..."
            );

            if (!changes.isEmpty()) {
                AiGenerationResult aiResult =
                        aiReleaseNotesService.improve(
                                request.model(),
                                request.language(),
                                range,
                                changes
                        );

                summary = aiResult.summary();
                changes = aiResult.changes();
                aiImproved = true;

                log.debug(
                        "Release notes improved using AI model: {}",
                        request.model()
                );
            }
        }

        reportProgress(
                progressListener,
                currentStep,
                totalSteps,
                "Rendering Markdown..."
        );

        String markdown =
                markdownRenderer.render(
                        range,
                        changes,
                        summary
                );

        return new ReleaseNotesGenerationResult(
                markdown,
                githubRepositoryFullName,
                githubPullRequestCount,
                aiImproved
        );
    }

    private int calculateTotalSteps(
            ReleaseNotesGenerationRequest request
    ) {
        int totalSteps = 3;

        if (request.githubEnabled()) {
            totalSteps++;
        }

        if (request.aiEnabled()) {
            totalSteps++;
        }

        return totalSteps;
    }

    private void reportProgress(
            Consumer<ReleaseNotesGenerationProgress> progressListener,
            int currentStep,
            int totalSteps,
            String message
    ) {
        progressListener.accept(
                new ReleaseNotesGenerationProgress(
                        currentStep,
                        totalSteps,
                        message
                )
        );
    }
}