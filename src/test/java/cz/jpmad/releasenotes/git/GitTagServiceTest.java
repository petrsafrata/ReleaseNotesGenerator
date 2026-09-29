package cz.jpmad.releasenotes.git;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GitTagServiceTest {

    @Test
    void loadsTagsInGitOrder() {
        AtomicReference<String[]> suppliedArguments =
                new AtomicReference<>();

        GitCommandExecutor executor =
                new GitCommandExecutor(Path.of(".")) {
                    @Override
                    public String execute(String... arguments) {
                        suppliedArguments.set(arguments);

                        return """
                                0.0.4
                                0.0.3
                                0.0.2
                                """;
                    }
                };

        GitTagService service = new GitTagService(executor);

        List<String> tags = service.findTags();

        assertArrayEquals(
                new String[]{
                        "tag",
                        "--sort=-creatordate"
                },
                suppliedArguments.get()
        );

        assertEquals(
                List.of(
                        "0.0.4",
                        "0.0.3",
                        "0.0.2"
                ),
                tags
        );
    }

    @Test
    void returnsEmptyListWhenRepositoryHasNoTags() {
        GitCommandExecutor executor =
                new GitCommandExecutor(Path.of(".")) {
                    @Override
                    public String execute(String... arguments) {
                        return "";
                    }
                };

        GitTagService service = new GitTagService(executor);

        assertEquals(
                List.of(),
                service.findTags()
        );
    }

    @Test
    void removesBlankAndDuplicateTags() {
        GitCommandExecutor executor =
                new GitCommandExecutor(Path.of(".")) {
                    @Override
                    public String execute(String... arguments) {
                        return """
                                0.0.3

                                0.0.2
                                0.0.3
                                """;
                    }
                };

        GitTagService service = new GitTagService(executor);

        assertEquals(
                List.of(
                        "0.0.3",
                        "0.0.2"
                ),
                service.findTags()
        );
    }
}