package cz.jpmad.releasenotes.git;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitLogParserTest {

    private final GitLogParser parser = new GitLogParser();

    @Test
    void parsesGitLogOutput() {
        String output =
                "1234567890\u001f1234567\u001fPetr Šafrata\u001f"
                        + "2026-09-06T18:30:00+02:00\u001f"
                        + "feat(cli): add generate command\u001e";

        List<GitCommit> commits = parser.parse(output);

        assertEquals(1, commits.size());

        GitCommit commit = commits.getFirst();

        assertEquals("1234567890", commit.hash());
        assertEquals("1234567", commit.shortHash());
        assertEquals("Petr Šafrata", commit.author());
        assertEquals(
                "feat(cli): add generate command",
                commit.message()
        );
        assertEquals(2026, commit.date().getYear());
    }

    @Test
    void returnsEmptyListForEmptyOutput() {
        assertTrue(parser.parse("").isEmpty());
    }
}
