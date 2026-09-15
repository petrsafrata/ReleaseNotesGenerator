package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.github.GitHubPullRequest;
import cz.jpmad.releasenotes.github.GitHubUser;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void rendersChangesGroupedByCategory() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        List<ReleaseChange> changes = List.of(
                createChange(ChangeCategory.BUG_FIXES, "handle missing tag", "abc1234"),
                createChange(ChangeCategory.FEATURES, "add generate command", "def5678")
        );

        String markdown = renderer.render(range, changes);

        String expectedMarkdown = """
                ## v0.0.2
                
                ### Features
                
                - add generate command (`def5678`)
                
                ### Bug Fixes
                
                - handle missing tag (`abc1234`)
                
                """;

        assertEquals(expectedMarkdown, markdown);
    }

    @Test
    void rendersPullRequestDetails() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        ReleaseChange change = createChange(
                ChangeCategory.FEATURES,
                "add interactive generation mode",
                "def5678"
        ).withPullRequest(
                new GitHubPullRequest(
                        1042,
                        42,
                        "Add interactive generation mode",
                        "closed",
                        "https://github.com/jpmad/"
                                + "ReleaseNotesGenerator/pull/42",
                        new GitHubUser(
                                "jpmad",
                                "https://github.com/jpmad"
                        )
                )
        );

        String markdown = renderer.render(range, List.of(change));

        String expectedMarkdown = """
                ## v0.0.2
                
                ### Features
                
                - Add interactive generation mode ([#42](https://github.com/jpmad/ReleaseNotesGenerator/pull/42)) by [@jpmad](https://github.com/jpmad)
                
                """;

        assertEquals(expectedMarkdown, markdown);
    }

    @Test
    void keepsCategoriesInDefinedOrder() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        List<ReleaseChange> changes = List.of(
                createChange(ChangeCategory.BUG_FIXES, "fix problem", "abc1234"),
                createChange(ChangeCategory.FEATURES, "add feature", "def5678")
        );

        String markdown = renderer.render(range, changes);
        int featuresPosition = markdown.indexOf("### Features");
        int bugFixesPosition = markdown.indexOf("### Bug Fixes");
        assertTrue(featuresPosition < bugFixesPosition);
    }

    @Test
    void doesNotRenderEmptyCategories() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        List<ReleaseChange> changes = List.of(createChange(ChangeCategory.FEATURES, "add feature",
                "abc1234"));

        String markdown = renderer.render(range, changes);

        assertTrue(markdown.contains("### Features"));
        assertFalse(markdown.contains("### Bug Fixes"));
    }

    @Test
    void rendersMessageWhenNoChangesWereFound() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        String markdown = renderer.render(range, List.of());
        assertTrue(markdown.contains("No changes found."));
    }

    @Test
    void rendersPullRequestWithoutAuthor() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.3"), "v0.0.4");

        ReleaseChange change = createChange(
                ChangeCategory.FEATURES,
                "add label categorization",
                "abc1234"
        ).withPullRequest(
                new GitHubPullRequest(
                        1042,
                        42,
                        "Add label categorization",
                        "closed",
                        "https://github.com/jpmad/ReleaseNotesGenerator/pull/42",
                        null
                )
        );

        String markdown = renderer.render(range, List.of(change));

        String expectedMarkdown = """
                ## v0.0.4
                
                ### Features
                
                - Add label categorization ([#42](https://github.com/jpmad/ReleaseNotesGenerator/pull/42))
                
                """;

        assertEquals(expectedMarkdown, markdown);
    }

    @Test
    void rendersAiSummary() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        List<ReleaseChange> changes = List.of(
                createChange(ChangeCategory.FEATURES, "Add interactive generation mode.", "def5678")
        );

        String markdown = renderer.render(
                range,
                changes,
                "This release introduces an interactive mode for generating release notes."
        );

        String expectedMarkdown = """
            ## v0.0.2
            
            This release introduces an interactive mode for generating release notes.
            
            ### Features
            
            - Add interactive generation mode. (`def5678`)
            
            """;

        assertEquals(expectedMarkdown, markdown);
    }

    private ReleaseChange createChange(ChangeCategory category, String description, String shortHash) {
        GitCommit commit = new GitCommit(
                "full-" + shortHash,
                shortHash,
                "Petr Šafrata",
                OffsetDateTime.parse(
                        "2026-09-06T18:30:00+02:00"
                ),
                description
        );

        return new ReleaseChange(category, description, commit);
    }
}
