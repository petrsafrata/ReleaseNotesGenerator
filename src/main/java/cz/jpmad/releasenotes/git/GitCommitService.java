package cz.jpmad.releasenotes.git;

import cz.jpmad.releasenotes.release.ReleaseRange;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GitCommitService {

    private static final String LOG_FORMAT = "%H%x1f%h%x1f%an%x1f%aI%x1f%s%x1e";

    @NonNull
    private final GitCommandExecutor gitCommandExecutor;

    @NonNull
    private final GitLogParser gitLogParser;

    public List<GitCommit> findCommits(ReleaseRange range) {
        String gitRange = range.from()
                .map(from -> from + ".." + range.to())
                .orElse(range.to());

        String output = gitCommandExecutor.execute(
                "log",
                gitRange,
                "--reverse",
                "--format=" + LOG_FORMAT
        );
        return gitLogParser.parse(output);
    }
}
