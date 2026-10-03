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
import cz.jpmad.releasenotes.release.ChangeCategory;
import cz.jpmad.releasenotes.release.MarkdownRenderer;
import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseRange;
import cz.jpmad.releasenotes.release.ReleaseRangeResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseNotesGenerationServiceTest {

    @Mock
    private GitRepositoryService repositoryService;

    @Mock
    private ReleaseRangeResolver rangeResolver;

    @Mock
    private GitCommitService commitService;

    @Mock
    private ChangeCategorizer changeCategorizer;

    @Mock
    private GitHubReleaseNotesEnrichmentService githubEnrichmentService;

    @Mock
    private AiReleaseNotesService aiReleaseNotesService;

    @Mock
    private MarkdownRenderer markdownRenderer;

    @InjectMocks
    private ReleaseNotesGenerationService service;

    @Test
    void generatesLocalReleaseNotesWithoutOptionalIntegrations() {
        GitRepositoryInfo repositoryInfo = mock(GitRepositoryInfo.class);
        ReleaseRange range = new ReleaseRange(Optional.of("0.0.3"), "0.0.4");
        GitCommit commit = createCommit("abc123", "feat: add label categorization");
        ReleaseChange change = createChange(commit, "add label categorization");

        ReleaseNotesGenerationRequest request = new ReleaseNotesGenerationRequest(
                null,
                "HEAD",
                false,
                false,
                "gemma3:4b",
                "en"
        );

        when(repositoryService.getRepositoryInfo()).thenReturn(repositoryInfo);
        when(rangeResolver.resolve(null, "HEAD")).thenReturn(range);
        when(commitService.findCommits(range)).thenReturn(List.of(commit));
        when(changeCategorizer.categorize(List.of(commit))).thenReturn(List.of(change));
        when(markdownRenderer.render(range, List.of(change), null)).thenReturn("generated markdown");

        List<ReleaseNotesGenerationProgress> progress = new ArrayList<>();

        ReleaseNotesGenerationResult result =
                service.generate(
                        request,
                        progress::add
                );

        assertEquals("generated markdown", result.markdown());
        assertTrue(result.githubRepositoryFullName().isEmpty());
        assertEquals(0, result.githubPullRequestCount());
        assertFalse(result.aiImproved());
        assertEquals(
                List.of(new ReleaseNotesGenerationProgress(
                                1,
                                3,
                                "Reading Git repository and resolving release range..."
                        ),
                        new ReleaseNotesGenerationProgress(
                                2,
                                3,
                                "Loading and categorizing commits..."
                        ),
                        new ReleaseNotesGenerationProgress(
                                3,
                                3,
                                "Rendering Markdown..."
                        )
                ),
                progress
        );
        verifyNoInteractions(githubEnrichmentService);
        verifyNoInteractions(aiReleaseNotesService);
    }

    @Test
    void generatesReleaseNotesWithGitHubAndAi() {
        GitRepositoryInfo repositoryInfo = mock(GitRepositoryInfo.class);
        ReleaseRange range = new ReleaseRange(Optional.of("0.0.3"), "0.0.4");
        GitCommit commit = createCommit("abc123", "feat: add label categorization");
        ReleaseChange localChange = createChange(commit, "add label categorization");
        ReleaseChange githubChange = createChange(commit, "Add pull request label categorization");
        ReleaseChange aiChange = createChange(commit, "Adds categorization based on pull request labels.");

        ReleaseNotesGenerationRequest request = new ReleaseNotesGenerationRequest(
                null,
                "HEAD",
                true,
                true,
                "gemma3:4b",
                "en"
        );

        GitHubEnrichmentResult githubResult = new GitHubEnrichmentResult(
                List.of(githubChange),
                "BombiXDev/TestRepoForGenerator",
                1
        );

        AiGenerationResult aiResult = new AiGenerationResult(
                "This release improves pull request categorization.",
                List.of(aiChange)
        );

        when(repositoryService.getRepositoryInfo()).thenReturn(repositoryInfo);
        when(rangeResolver.resolve(null, "HEAD")).thenReturn(range);
        when(commitService.findCommits(range)).thenReturn(List.of(commit));
        when(changeCategorizer.categorize(List.of(commit))).thenReturn(List.of(localChange));
        when(githubEnrichmentService.enrich(
                repositoryInfo,
                List.of(commit),
                List.of(localChange)
        )).thenReturn(githubResult);
        when(aiReleaseNotesService.improve(
                "gemma3:4b",
                "en",
                range,
                List.of(githubChange)
        )).thenReturn(aiResult);
        when(markdownRenderer.render(
                range,
                List.of(aiChange),
                "This release improves pull request categorization."
        )).thenReturn("generated AI markdown");

        List<ReleaseNotesGenerationProgress> progress = new ArrayList<>();

        ReleaseNotesGenerationResult result =
                service.generate(
                        request,
                        progress::add
                );

        assertEquals("generated AI markdown", result.markdown());
        assertEquals(
                "BombiXDev/TestRepoForGenerator",
                result.githubRepositoryFullName().orElseThrow()
        );
        assertEquals(1, result.githubPullRequestCount());
        assertTrue(result.aiImproved());
        assertEquals(
                List.of(new ReleaseNotesGenerationProgress(
                                1,
                                5,
                                "Reading Git repository and resolving release range..."
                        ),
                        new ReleaseNotesGenerationProgress(
                                2,
                                5,
                                "Loading and categorizing commits..."
                        ),
                        new ReleaseNotesGenerationProgress(
                                3,
                                5,
                                "Loading GitHub pull requests..."
                        ),
                        new ReleaseNotesGenerationProgress(
                                4,
                                5,
                                "Improving release notes using AI model gemma3:4b..."
                        ),
                        new ReleaseNotesGenerationProgress(
                                5,
                                5,
                                "Rendering Markdown..."
                        )
                ),
                progress
        );
    }

    @Test
    void skipsAiWhenNoChangesWereFound() {
        GitRepositoryInfo repositoryInfo = mock(GitRepositoryInfo.class);
        ReleaseRange range = new ReleaseRange(Optional.of("0.0.3"), "0.0.4");

        ReleaseNotesGenerationRequest request = new ReleaseNotesGenerationRequest(
                null,
                "HEAD",
                false,
                true,
                "gemma3:4b",
                "en"
        );

        when(repositoryService.getRepositoryInfo()).thenReturn(repositoryInfo);
        when(rangeResolver.resolve(null, "HEAD")).thenReturn(range);
        when(commitService.findCommits(range)).thenReturn(List.of());
        when(changeCategorizer.categorize(List.of())).thenReturn(List.of());
        when(markdownRenderer.render(range, List.of(), null)).thenReturn("No changes found.");

        ReleaseNotesGenerationResult result = service.generate(request);

        assertEquals("No changes found.", result.markdown());
        assertFalse(result.aiImproved());

        verifyNoInteractions(aiReleaseNotesService);
    }

    private GitCommit createCommit(String hash, String message) {
        return new GitCommit(
                hash,
                hash,
                "Petr Šafrata",
                OffsetDateTime.parse("2026-09-28T12:00:00Z"),
                message
        );
    }

    private ReleaseChange createChange(GitCommit commit, String description) {
        return new ReleaseChange(
                ChangeCategory.FEATURES,
                description,
                commit
        );
    }
}