package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangeCategorizerTest {

    private final ChangeCategorizer categorizer = new ChangeCategorizer();

    @ParameterizedTest
    @CsvSource({
            "feat: add CLI, FEATURES",
            "fix(git): handle missing tag, BUG_FIXES",
            "docs: update README, DOCUMENTATION",
            "refactor(core): simplify parser, REFACTORING",
            "test: add parser tests, TESTS",
            "chore: update dependencies, INTERNAL_CHANGES",
            "perf: improve parsing speed, PERFORMANCE",
            "build: configure Maven, BUILD_SYSTEM",
            "ci: add build workflow, CI_CD"
    })
    void categorizesConventionalCommit(String message, ChangeCategory expectedCategory) {
        GitCommit commit = createCommit(message);

        ReleaseChange change = categorizer.categorize(List.of(commit)).getFirst();
        assertEquals(expectedCategory, change.category());
    }

    @Test
    void removesTypeAndScopeFromDescription() {
        GitCommit commit = createCommit("feat(cli): add generate command");
        ReleaseChange change = categorizer.categorize(List.of(commit)).getFirst();
        assertEquals(
                "add generate command",
                change.description()
        );
    }

    @Test
    void supportsBreakingChangeMarker() {
        GitCommit commit = createCommit("feat(api)!: change response format");
        ReleaseChange change = categorizer.categorize(List.of(commit)).getFirst();
        assertEquals(
                ChangeCategory.FEATURES,
                change.category()
        );
        assertEquals(
                "change response format",
                change.description()
        );
    }

    @Test
    void putsUnknownMessageIntoOtherChanges() {
        GitCommit commit = createCommit("Update application configuration");

        ReleaseChange change = categorizer.categorize(List.of(commit)).getFirst();
        assertEquals(
                ChangeCategory.OTHER_CHANGES,
                change.category()
        );
        assertEquals(
                commit.message(),
                change.description()
        );
    }

    private GitCommit createCommit(String message) {
        return new GitCommit("1234567890", "1234567", "Petr Šafrata",
                OffsetDateTime.parse("2026-09-06T18:30:00+02:00"), message);
    }
}