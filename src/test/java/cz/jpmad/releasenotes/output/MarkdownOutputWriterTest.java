package cz.jpmad.releasenotes.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownOutputWriterTest {

    private final MarkdownOutputWriter writer = new MarkdownOutputWriter();

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsNewReleaseNotesDocument() throws IOException {
        Path output = temporaryDirectory.resolve("RELEASE_NOTES.md");

        writer.write(
                output,
                """
                ## 0.0.1

                ### Features

                - Initial feature.
                """
        );

        String content = Files.readString(output);

        assertTrue(content.startsWith("# Release Notes"));
        assertTrue(content.contains("## 0.0.1"));
    }

    @Test
    void prependsNewVersion() throws IOException {
        Path output = temporaryDirectory.resolve("RELEASE_NOTES.md");

        writer.write(output, "## 0.0.1\n\nFirst release.\n");
        writer.write(output, "## 0.0.2\n\nSecond release.\n");

        String content = Files.readString(output);

        assertTrue(
                content.indexOf("## 0.0.2")
                        < content.indexOf("## 0.0.1")
        );
    }

    @Test
    void replacesExistingVersion() throws IOException {
        Path output = temporaryDirectory.resolve("RELEASE_NOTES.md");

        writer.write(output, "## 0.0.1\n\nOld content.\n");
        writer.write(output, "## 0.0.1\n\nUpdated content.\n");

        String content = Files.readString(output);

        assertTrue(content.contains("Updated content."));
        assertTrue(!content.contains("Old content."));
        assertEquals(
                content.indexOf("## 0.0.1"),
                content.lastIndexOf("## 0.0.1")
        );
    }
}