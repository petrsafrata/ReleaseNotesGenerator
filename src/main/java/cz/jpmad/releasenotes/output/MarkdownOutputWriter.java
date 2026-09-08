package cz.jpmad.releasenotes.output;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownOutputWriter {

    private static final String DOCUMENT_HEADING = "# Release Notes";

    public Path write(Path output, String releaseMarkdown) throws IOException {
        Path normalizedOutput = output
                .toAbsolutePath()
                .normalize();

        String existingContent = readExistingContent(normalizedOutput);
        String updatedContent = updateDocument(existingContent, releaseMarkdown);
        Files.writeString(normalizedOutput, updatedContent, StandardCharsets.UTF_8);

        return normalizedOutput;
    }

    private String readExistingContent(Path output) throws IOException {
        if (!Files.exists(output)) {
            return "";
        }

        return Files.readString(output, StandardCharsets.UTF_8);
    }

    private String updateDocument(String existingContent, String releaseMarkdown) {
        String normalizedRelease = releaseMarkdown.strip();
        String versionHeading = extractVersionHeading(normalizedRelease);

        if (existingContent.isBlank()) {
            return createDocument(normalizedRelease);
        }

        if (containsVersion(existingContent, versionHeading)) {
            return replaceVersion(existingContent, versionHeading, normalizedRelease);
        }

        return prependVersion(existingContent, normalizedRelease);
    }

    private String createDocument(String releaseMarkdown) {
        return DOCUMENT_HEADING
                + "\n\n"
                + releaseMarkdown
                + "\n";
    }

    private String extractVersionHeading(String releaseMarkdown) {
        return releaseMarkdown.lines()
                .findFirst()
                .filter(line -> line.startsWith("## "))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Release Markdown must start with a version heading."
                ));
    }

    private boolean containsVersion(String existingContent, String versionHeading) {
        return existingContent.lines().anyMatch(versionHeading::equals);
    }

    private String replaceVersion(String existingContent, String versionHeading, String releaseMarkdown) {
        Pattern versionSectionPattern = Pattern.compile(
                "(?ms)^"
                        + Pattern.quote(versionHeading)
                        + "\\R.*?(?=^##\\s+|\\z)"
        );

        Matcher matcher = versionSectionPattern.matcher(existingContent);

        return matcher.replaceFirst(
                Matcher.quoteReplacement(
                        releaseMarkdown + "\n\n"
                )
        ).stripTrailing() + "\n";
    }

    private String prependVersion(String existingContent, String releaseMarkdown) {
        String existingWithoutHeading = removeDocumentHeading(existingContent);

        return DOCUMENT_HEADING
                + "\n\n"
                + releaseMarkdown
                + "\n\n"
                + existingWithoutHeading.strip()
                + "\n";
    }

    private String removeDocumentHeading(String existingContent) {
        String strippedContent = existingContent.strip();

        if (strippedContent.startsWith(DOCUMENT_HEADING)) {
            return strippedContent.substring(DOCUMENT_HEADING.length()).strip();
        }
        return strippedContent;
    }
}
