package cz.jpmad.releasenotes.tui.generate;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerateReleaseNotesConfigurationTest {

    @Test
    void createsArgumentsForDefaultGeneration() {
        GenerateReleaseNotesConfiguration configuration =
                new GenerateReleaseNotesConfiguration(
                        null,
                        "HEAD",
                        false,
                        false,
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(
                List.of("generate"),
                configuration.toArguments()
        );
    }

    @Test
    void createsArgumentsForCompleteGeneration() {
        GenerateReleaseNotesConfiguration configuration =
                new GenerateReleaseNotesConfiguration(
                        "0.0.3",
                        "0.0.4",
                        true,
                        true,
                        "gemma3:4b",
                        "en",
                        "http://localhost:11434",
                        "RELEASE_NOTES.md"
                );

        assertEquals(
                List.of(
                        "generate",
                        "--from",
                        "0.0.3",
                        "--to",
                        "0.0.4",
                        "--github",
                        "--ai",
                        "--model",
                        "gemma3:4b",
                        "--language",
                        "en",
                        "--ollama-url",
                        "http://localhost:11434",
                        "--output",
                        "RELEASE_NOTES.md"
                ),
                configuration.toArguments()
        );
    }
}