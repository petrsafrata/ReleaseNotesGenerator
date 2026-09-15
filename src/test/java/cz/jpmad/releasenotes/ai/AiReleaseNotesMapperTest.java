package cz.jpmad.releasenotes.ai;

import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.github.GitHubPullRequest;
import cz.jpmad.releasenotes.github.GitHubUser;
import cz.jpmad.releasenotes.release.ChangeCategory;
import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseRange;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiReleaseNotesMapperTest {

    private final AiReleaseNotesMapper mapper = new AiReleaseNotesMapper();

    @Test
    void createsRequestFromCommitAndPullRequestChanges() {
        ReleaseRange range = new ReleaseRange(Optional.of("0.0.3"), "0.0.4");

        ReleaseChange commitChange = createChange("abc123", "Handle missing value");
        ReleaseChange pullRequestChange = createChange("def456", "Add GitHub integration")
                .withPullRequest(createPullRequest());

        AiReleaseNotesRequest request = mapper.createRequest(
                range,
                "en",
                List.of(commitChange, pullRequestChange)
        );

        assertEquals("0.0.4", request.version());
        assertEquals("en", request.language());
        assertEquals(2, request.changes().size());
        assertEquals("commit-abc123", request.changes().get(0).id());
        assertEquals("pr-42", request.changes().get(1).id());
        assertEquals("FEATURES", request.changes().get(1).category());
    }

    @Test
    void appliesDescriptionsWithoutChangingMetadata() {
        ReleaseChange commitChange = createChange("abc123", "Handle missing value");
        ReleaseChange pullRequestChange = createChange("def456", "Add GitHub integration")
                .withPullRequest(createPullRequest());

        AiReleaseNotesResponse response = new AiReleaseNotesResponse(
                "This release improves GitHub integration.",
                List.of(
                        new AiImprovedChange("commit-abc123", "Improved handling of missing values."),
                        new AiImprovedChange("pr-42", "Added GitHub integration.")
                )
        );

        List<ReleaseChange> result = mapper.applyResponse(
                List.of(commitChange, pullRequestChange),
                response
        );

        assertEquals("Improved handling of missing values.", result.get(0).description());
        assertEquals("Added GitHub integration.", result.get(1).description());
        assertEquals(commitChange.category(), result.get(0).category());
        assertEquals(commitChange.sourceCommit(), result.get(0).sourceCommit());
        assertTrue(result.get(1).pullRequest().isPresent());
        assertEquals(42, result.get(1).pullRequest().orElseThrow().number());
    }

    private ReleaseChange createChange(String hash, String description) {
        GitCommit commit = new GitCommit(
                hash,
                hash,
                "Petr Šafrata",
                OffsetDateTime.parse("2026-09-15T12:00:00Z"),
                description
        );

        return new ReleaseChange(ChangeCategory.FEATURES, description, commit);
    }

    private GitHubPullRequest createPullRequest() {
        return new GitHubPullRequest(
                1042,
                42,
                "Add GitHub integration",
                "closed",
                "https://github.com/jpmad/ReleaseNotesGenerator/pull/42",
                new GitHubUser("jpmad", "https://github.com/jpmad")
        );
    }
}