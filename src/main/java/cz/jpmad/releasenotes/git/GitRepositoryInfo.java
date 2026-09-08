package cz.jpmad.releasenotes.git;

import java.nio.file.Path;
import java.util.Optional;

public record GitRepositoryInfo(
        Path root,
        String currentBranch,
        Optional<String> remoteUrl
) {
}
