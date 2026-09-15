package cz.jpmad.releasenotes.ai;

import java.util.HashSet;
import java.util.Set;

public class AiResponseValidator {

    public void validate(AiReleaseNotesRequest request, AiReleaseNotesResponse response) {
        if (response == null) {
            throw new AiException("AI release notes response must not be null.");
        }

        if (response.summary() == null || response.summary().isBlank()) {
            throw new AiException("AI release summary must not be empty.");
        }

        validateInputIdentifiers(request);
        validateResponseChanges(response);
        validateMatchingIdentifiers(request, response);
    }

    private void validateInputIdentifiers(AiReleaseNotesRequest request) {
        Set<String> identifiers = new HashSet<>();

        for (AiReleaseChange change : request.changes()) {
            if (change.id() == null || change.id().isBlank()) {
                throw new AiException("AI request contains an empty change identifier.");
            }

            if (!identifiers.add(change.id())) {
                throw new AiException("AI request contains duplicate change identifier: " + change.id());
            }
        }
    }

    private void validateResponseChanges(AiReleaseNotesResponse response) {
        Set<String> identifiers = new HashSet<>();

        for (AiImprovedChange change : response.changes()) {
            if (change.id() == null || change.id().isBlank()) {
                throw new AiException("AI response contains an empty change identifier.");
            }

            if (!identifiers.add(change.id())) {
                throw new AiException("AI response contains duplicate change identifier: " + change.id());
            }

            if (change.description() == null || change.description().isBlank()) {
                throw new AiException("AI response contains an empty description for change: " + change.id());
            }
        }
    }

    private void validateMatchingIdentifiers(AiReleaseNotesRequest request, AiReleaseNotesResponse response) {
        Set<String> requestIdentifiers = new HashSet<>();
        Set<String> responseIdentifiers = new HashSet<>();

        for (AiReleaseChange change : request.changes()) {
            requestIdentifiers.add(change.id());
        }

        for (AiImprovedChange change : response.changes()) {
            responseIdentifiers.add(change.id());
        }

        if (!requestIdentifiers.equals(responseIdentifiers)) {
            throw new AiException("AI response change identifiers do not match the request.");
        }
    }
}