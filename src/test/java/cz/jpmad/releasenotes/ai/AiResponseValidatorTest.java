package cz.jpmad.releasenotes.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiResponseValidatorTest {

    private final AiResponseValidator validator = new AiResponseValidator();

    @Test
    void acceptsMatchingResponse() {
        AiReleaseNotesRequest request = createRequest(
                new AiReleaseChange("pr-2", "FEATURES", "Add label categorization"),
                new AiReleaseChange("commit-abc123", "BUG_FIXES", "Handle missing value")
        );

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "This release improves categorization and error handling.",
                List.of(
                        new AiImprovedChange("pr-2", "Added pull request label categorization."),
                        new AiImprovedChange("commit-abc123", "Improved handling of missing values.")
                )
        );

        assertDoesNotThrow(() -> validator.validate(request, response));
    }

    @Test
    void acceptsDifferentResponseOrder() {
        AiReleaseNotesRequest request = createRequest(
                new AiReleaseChange("pr-2", "FEATURES", "Add label categorization"),
                new AiReleaseChange("commit-abc123", "BUG_FIXES", "Handle missing value")
        );

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "This release improves the application.",
                List.of(
                        new AiImprovedChange("commit-abc123", "Improved handling of missing values."),
                        new AiImprovedChange("pr-2", "Added pull request label categorization.")
                )
        );

        assertDoesNotThrow(() -> validator.validate(request, response));
    }

    @Test
    void rejectsMissingChange() {
        AiReleaseNotesRequest request = createRequest(
                new AiReleaseChange("pr-2", "FEATURES", "Add label categorization"),
                new AiReleaseChange("commit-abc123", "BUG_FIXES", "Handle missing value")
        );

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "This release improves categorization.",
                List.of(
                        new AiImprovedChange("pr-2", "Added pull request label categorization.")
                )
        );

        AiException exception = assertThrows(
                AiException.class,
                () -> validator.validate(request, response)
        );

        assertEquals("AI response change identifiers do not match the request.", exception.getMessage());
    }

    @Test
    void rejectsUnexpectedChange() {
        AiReleaseNotesRequest request = createRequest(
                new AiReleaseChange("pr-2", "FEATURES", "Add label categorization")
        );

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "This release improves categorization.",
                List.of(
                        new AiImprovedChange("pr-2", "Added pull request label categorization."),
                        new AiImprovedChange("invented-change", "Added an invented feature.")
                )
        );

        AiException exception = assertThrows(
                AiException.class,
                () -> validator.validate(request, response)
        );

        assertEquals("AI response change identifiers do not match the request.", exception.getMessage());
    }

    @Test
    void rejectsDuplicateChange() {
        AiReleaseNotesRequest request = createRequest(
                new AiReleaseChange("pr-2", "FEATURES", "Add label categorization")
        );

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "This release improves categorization.",
                List.of(
                        new AiImprovedChange("pr-2", "First description."),
                        new AiImprovedChange("pr-2", "Second description.")
                )
        );

        AiException exception = assertThrows(
                AiException.class,
                () -> validator.validate(request, response)
        );

        assertEquals("AI response contains duplicate change identifier: pr-2", exception.getMessage());
    }

    @Test
    void rejectsEmptyDescription() {
        AiReleaseNotesRequest request = createRequest(
                new AiReleaseChange("pr-2", "FEATURES", "Add label categorization")
        );

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "This release improves categorization.",
                List.of(
                        new AiImprovedChange("pr-2", "")
                )
        );

        AiException exception = assertThrows(
                AiException.class,
                () -> validator.validate(request, response)
        );

        assertEquals("AI response contains an empty description for change: pr-2", exception.getMessage());
    }

    @Test
    void rejectsEmptySummary() {
        AiReleaseNotesRequest request = createRequest(
                new AiReleaseChange("pr-2", "FEATURES", "Add label categorization")
        );

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "",
                List.of(
                        new AiImprovedChange("pr-2", "Added pull request label categorization.")
                )
        );

        AiException exception = assertThrows(
                AiException.class,
                () -> validator.validate(request, response)
        );

        assertEquals("AI release summary must not be empty.", exception.getMessage());
    }

    private AiReleaseNotesRequest createRequest(AiReleaseChange... changes) {
        return new AiReleaseNotesRequest("0.0.4", "en", List.of(changes));
    }
}