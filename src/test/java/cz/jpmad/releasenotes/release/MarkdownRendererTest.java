package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitCommit;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer =
            new MarkdownRenderer();

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
    void keepsCategoriesInDefinedOrder() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        List<ReleaseChange> changes = List.of(createChange(ChangeCategory.BUG_FIXES, "fix problem",
                        "abc1234"), createChange(ChangeCategory.FEATURES, "add feature",
                "def5678")
        );

        String markdown = renderer.render(range, changes);
        int featuresPosition = markdown.indexOf("## Features");
        int bugFixesPosition = markdown.indexOf("## Bug Fixes");
        assertTrue(featuresPosition < bugFixesPosition);
    }

    @Test
    void doesNotRenderEmptyCategories() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        List<ReleaseChange> changes = List.of(createChange(ChangeCategory.FEATURES, "add feature",
                "abc1234"));

        String markdown = renderer.render(range, changes);

        assertTrue(markdown.contains("## Features"));
        assertFalse(markdown.contains("## Bug Fixes"));
    }

    @Test
    void rendersMessageWhenNoChangesWereFound() {
        ReleaseRange range = new ReleaseRange(Optional.of("v0.0.1"), "v0.0.2");

        String markdown = renderer.render(range, List.of());
        assertTrue(markdown.contains("No changes found."));
    }

    private ReleaseChange createChange(ChangeCategory category, String description, String shortHash) {
        GitCommit commit = new GitCommit(
                "1234567890",
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
