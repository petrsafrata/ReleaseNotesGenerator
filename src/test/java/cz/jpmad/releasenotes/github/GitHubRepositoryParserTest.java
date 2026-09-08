package cz.jpmad.releasenotes.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubRepositoryParserTest {

    private final GitHubRepositoryParser parser = new GitHubRepositoryParser();

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/jpmad/ReleaseNotesGenerator.git",
            "https://github.com/jpmad/ReleaseNotesGenerator",
            "git@github.com:jpmad/ReleaseNotesGenerator.git",
            "ssh://git@github.com/jpmad/ReleaseNotesGenerator.git"
    })
    void parsesSupportedGitHubRemoteUrls(String remoteUrl) {
        GitHubRepository repository = parser.parse(remoteUrl).orElseThrow();

        assertEquals("jpmad", repository.owner());
        assertEquals("ReleaseNotesGenerator", repository.name());
        assertEquals(
                "jpmad/ReleaseNotesGenerator",
                repository.fullName()
        );
    }

    @Test
    void returnsEmptyForNonGitHubRemote() {
        var result = parser.parse("https://gitlab.com/jpmad/project.git");
        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForInvalidRemote() {
        assertTrue(parser.parse("invalid-url").isEmpty());
        assertTrue(parser.parse("").isEmpty());
    }
}