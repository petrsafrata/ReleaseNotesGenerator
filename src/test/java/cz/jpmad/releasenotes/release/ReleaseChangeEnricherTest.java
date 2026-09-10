package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.github.GitHubPullRequest;
import cz.jpmad.releasenotes.github.GitHubPullRequestAssociation;
import cz.jpmad.releasenotes.github.GitHubUser;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseChangeEnricherTest {

    private final ReleaseChangeEnricher enricher = new ReleaseChangeEnricher();

    @Test
    void enrichesChangeWithPullRequest() {
        ReleaseChange change = createChange("commit-one", "add interactive mode");
        GitHubPullRequest pullRequest = createPullRequest(42, "Add interactive generation mode");
        GitHubPullRequestAssociation association = new GitHubPullRequestAssociation(pullRequest, Set.of("commit-one"));

        List<ReleaseChange> result = enricher.enrich(List.of(change), List.of(association));

        assertEquals(1, result.size());

        ReleaseChange enrichedChange = result.getFirst();

        assertEquals(ChangeCategory.FEATURES, enrichedChange.category());
        assertEquals("Add interactive generation mode", enrichedChange.description());
        assertTrue(enrichedChange.pullRequest().isPresent());
        assertEquals(42, enrichedChange.pullRequest().orElseThrow().number());
    }

    @Test
    void keepsChangeWithoutPullRequest() {
        ReleaseChange change = createChange("commit-one", "add interactive mode");

        List<ReleaseChange> result = enricher.enrich(List.of(change), List.of());

        assertEquals(List.of(change), result);
        assertFalse(result.getFirst().pullRequest().isPresent());
    }

    @Test
    void combinesChangesBelongingToSamePullRequest() {
        ReleaseChange firstChange = createChange("commit-one", "add interactive mode");
        ReleaseChange secondChange = createChange("commit-two", "add interactive prompt");

        GitHubPullRequestAssociation association =
                new GitHubPullRequestAssociation(
                        createPullRequest(
                                42,
                                "Add interactive generation mode"
                        ),
                        Set.of(
                                "commit-one",
                                "commit-two"
                        )
                );

        List<ReleaseChange> result = enricher.enrich(List.of(firstChange, secondChange), List.of(association));

        assertEquals(1, result.size());
        assertEquals(42, result.getFirst().pullRequest().orElseThrow().number());
    }

    @Test
    void keepsUnassociatedChangesWhileRemovingDuplicates() {
        ReleaseChange firstChange = createChange("commit-one", "first PR commit");
        ReleaseChange secondChange = createChange("commit-two", "second PR commit");
        ReleaseChange standaloneChange = createChange("commit-three", "standalone change");

        GitHubPullRequestAssociation association =
                new GitHubPullRequestAssociation(
                        createPullRequest(
                                42,
                                "Combined pull request"
                        ),
                        Set.of(
                                "commit-one",
                                "commit-two"
                        )
                );

        List<ReleaseChange> result = enricher.enrich(List.of(firstChange, secondChange, standaloneChange), List.of(association));

        assertEquals(2, result.size());
        assertTrue(result.get(0).pullRequest().isPresent());
        assertEquals("standalone change", result.get(1).description());
        assertFalse(result.get(1).pullRequest().isPresent());
    }

    @Test
    void usesFeatureCategoryInsteadOfMergeCommitCategory() {
        ReleaseChange mergeChange = createChange(
                "merge-commit",
                "Merge pull request #42",
                ChangeCategory.OTHER_CHANGES
        );
        ReleaseChange featureChange = createChange(
                "feature-commit",
                "add interactive mode",
                ChangeCategory.FEATURES
        );
        ReleaseChange testChange = createChange(
                "test-commit",
                "cover interactive mode",
                ChangeCategory.TESTS
        );

        GitHubPullRequestAssociation association = new GitHubPullRequestAssociation(
                createPullRequest(42, "Add interactive generation mode"),
                Set.of("merge-commit", "feature-commit", "test-commit")
        );

        List<ReleaseChange> result = enricher.enrich(
                List.of(mergeChange, featureChange, testChange),
                List.of(association)
        );

        assertEquals(1, result.size());
        assertEquals(ChangeCategory.FEATURES, result.getFirst().category());
        assertEquals("Add interactive generation mode", result.getFirst().description());
    }

    private ReleaseChange createChange(String hash, String description) {
        GitCommit commit = new GitCommit(hash, hash, "Petr Šafrata",
                OffsetDateTime.parse(
                        "2026-09-10T12:00:00Z"
                ),
                "feat: " + description
        );

        return new ReleaseChange(ChangeCategory.FEATURES, description, commit);
    }

    private GitHubPullRequest createPullRequest(int number, String title) {
        return new GitHubPullRequest(
                1000L + number,
                number,
                title,
                "closed",
                "https://github.com/jpmad/"
                        + "ReleaseNotesGenerator/pull/"
                        + number,
                new GitHubUser(
                        "jpmad",
                        "https://github.com/jpmad"
                )
        );
    }

    private ReleaseChange createChange(String hash, String description, ChangeCategory category) {
        GitCommit commit = new GitCommit(hash, hash, "Petr Šafrata",
                OffsetDateTime.parse(
                        "2026-09-10T12:00:00Z"
                ),
                description
        );

        return new ReleaseChange(category, description, commit);
    }
}