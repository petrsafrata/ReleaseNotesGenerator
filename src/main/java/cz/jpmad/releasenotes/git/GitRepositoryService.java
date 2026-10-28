package cz.jpmad.releasenotes.git;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
public class GitRepositoryService {

    @NonNull
    private final GitCommandExecutor gitCommandExecutor;

    public GitRepositoryInfo getRepositoryInfo() {
        Path root = getRepositoryRoot();
        String currentBranch = getCurrentBranch();
        Optional<String> remoteUrl = getRemoteUrl();

        return new GitRepositoryInfo(root, currentBranch, remoteUrl);
    }

    public Path getRepositoryRoot() {
        String repositoryRoot = gitCommandExecutor.execute(
                "rev-parse",
                "--show-toplevel"
        );
        return Path.of(repositoryRoot)
                .toAbsolutePath()
                .normalize();
    }

    private String getCurrentBranch() {
        String branch = gitCommandExecutor.execute(
                "branch",
                "--show-current"
        );

        if (branch.isBlank()) {
            return "HEAD (detached)";
        }
        return branch;
    }

    private Optional<String> getRemoteUrl() {
        try {
            String remoteUrl = gitCommandExecutor.execute(
                    "remote",
                    "get-url",
                    "origin"
            );
            return Optional.of(remoteUrl);
        } catch (GitCommandException exception) {
            return Optional.empty();
        }
    }
}
