package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.github.GitHubLabel;
import cz.jpmad.releasenotes.github.GitHubPullRequest;
import cz.jpmad.releasenotes.github.GitHubUser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PullRequestCategoryResolverTest {

    private final PullRequestCategoryResolver resolver = new PullRequestCategoryResolver();

    @Test
    void resolvesCategoryFromPullRequestLabel() {
        GitHubPullRequest pullRequest = createPullRequest(
                List.of(new GitHubLabel(1, "bug", "d73a4a", "Something is not working"))
        );

        ChangeCategory category = resolver.resolve(pullRequest, Set.of(ChangeCategory.FEATURES));

        assertEquals(ChangeCategory.BUG_FIXES, category);
    }

    @Test
    void resolvesCategoryFromCommitWhenKnownLabelIsMissing() {
        GitHubPullRequest pullRequest = createPullRequest(
                List.of(new GitHubLabel(1, "priority: high", "b60205", "High priority"))
        );

        ChangeCategory category = resolver.resolve(pullRequest, Set.of(
                ChangeCategory.OTHER_CHANGES,
                ChangeCategory.FEATURES,
                ChangeCategory.TESTS
        ));

        assertEquals(ChangeCategory.FEATURES, category);
    }

    @Test
    void ignoresLabelNameCase() {
        GitHubPullRequest pullRequest = createPullRequest(
                List.of(new GitHubLabel(1, "Documentation", "0075ca", null))
        );

        ChangeCategory category = resolver.resolve(pullRequest, Set.of(ChangeCategory.FEATURES));

        assertEquals(ChangeCategory.DOCUMENTATION, category);
    }

    @Test
    void usesDefinedPriorityForMultipleLabels() {
        GitHubPullRequest pullRequest = createPullRequest(
                List.of(
                        new GitHubLabel(1, "documentation", "0075ca", null),
                        new GitHubLabel(2, "feature", "a2eeef", null)
                )
        );

        ChangeCategory category = resolver.resolve(pullRequest, Set.of(ChangeCategory.OTHER_CHANGES));

        assertEquals(ChangeCategory.FEATURES, category);
    }

    @Test
    void returnsOtherChangesWhenNoCategoryCanBeResolved() {
        GitHubPullRequest pullRequest = createPullRequest(List.of());

        ChangeCategory category = resolver.resolve(pullRequest, Set.of(ChangeCategory.OTHER_CHANGES));

        assertEquals(ChangeCategory.OTHER_CHANGES, category);
    }

    private GitHubPullRequest createPullRequest(List<GitHubLabel> labels) {
        return new GitHubPullRequest(
                1042,
                42,
                "Update application",
                "closed",
                "https://github.com/jpmad/ReleaseNotesGenerator/pull/42",
                new GitHubUser("jpmad", "https://github.com/jpmad"),
                labels
        );
    }
}