package cz.jpmad.releasenotes.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jpmad.releasenotes.git.GitCommit;
import cz.jpmad.releasenotes.release.ChangeCategory;
import cz.jpmad.releasenotes.release.ReleaseChange;
import cz.jpmad.releasenotes.release.ReleaseRange;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiReleaseNotesServiceTest {

    @Test
    void improvesReleaseChanges() {
        AtomicReference<String> suppliedModel = new AtomicReference<>();

        AiReleaseNotesClient client = (model, prompt) -> {
            suppliedModel.set(model);

            return new AiReleaseNotesResponse(
                    "This release improves configuration handling.",
                    List.of(
                            new AiImprovedChange(
                                    "commit-abc123",
                                    "Improved handling of missing configuration values."
                            )
                    )
            );
        };

        ObjectMapper objectMapper = new ObjectMapper();

        AiReleaseNotesService service = new AiReleaseNotesService(
                client,
                new AiPromptBuilder(
                        objectMapper,
                        new AiPromptLoader(objectMapper)
                ),
                new AiResponseValidator(),
                new AiReleaseNotesMapper()
        );

        ReleaseChange change = new ReleaseChange(
                ChangeCategory.BUG_FIXES,
                "handle missing configuration value",
                new GitCommit(
                        "abc123",
                        "abc123",
                        "Petr Šafrata",
                        OffsetDateTime.parse("2026-09-15T12:00:00Z"),
                        "fix: handle missing configuration value"
                )
        );

        AiGenerationResult result = service.improve(
                "gemma3:4b",
                "en",
                new ReleaseRange(Optional.of("0.0.3"), "0.0.4"),
                List.of(change)
        );

        assertEquals("gemma3:4b", suppliedModel.get());
        assertEquals("This release improves configuration handling.", result.summary());
        assertEquals(1, result.changes().size());
        assertEquals(
                "Improved handling of missing configuration values.",
                result.changes().getFirst().description()
        );
        assertEquals(ChangeCategory.BUG_FIXES, result.changes().getFirst().category());
    }
}