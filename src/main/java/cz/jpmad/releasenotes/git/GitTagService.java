package cz.jpmad.releasenotes.git;

import java.util.Optional;
import java.util.List;

public class GitTagService implements GitTagProvider {

    private final GitCommandExecutor gitCommandExecutor;

    public GitTagService(GitCommandExecutor gitCommandExecutor) {
        this.gitCommandExecutor = gitCommandExecutor;
    }

    @Override
    public Optional<String> findExactTag(String reference) {
        try {
            String tag = gitCommandExecutor.execute(
                    "describe",
                    "--tags",
                    "--exact-match",
                    reference
            );
            return optionalTag(tag);
        } catch (GitCommandException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> findPreviousTag(String reference) {
        try {
            String tag = gitCommandExecutor.execute(
                    "describe",
                    "--tags",
                    "--abbrev=0",
                    reference + "^"
            );
            return optionalTag(tag);
        } catch (GitCommandException exception) {
            return Optional.empty();
        }
    }

    public List<String> findTags() {
        String output = gitCommandExecutor.execute(
                "tag",
                "--sort=-creatordate"
        );

        if (output == null || output.isBlank()) {
            return List.of();
        }

        return output.lines()
                .map(String::strip)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
    }

    private Optional<String> optionalTag(String tag) {
        return tag == null || tag.isBlank()
                ? Optional.empty()
                : Optional.of(tag);
    }
}
