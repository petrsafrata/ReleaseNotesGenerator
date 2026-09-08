package cz.jpmad.releasenotes.git;

import cz.jpmad.releasenotes.release.ReleaseRange;

import java.util.List;

public class GitCommitService {

    private static final String LOG_FORMAT = "%H%x1f%h%x1f%an%x1f%aI%x1f%s%x1e";

    private final GitCommandExecutor gitCommandExecutor;
    private final GitLogParser gitLogParser;

    public GitCommitService(GitCommandExecutor gitCommandExecutor, GitLogParser gitLogParser) {
        this.gitCommandExecutor = gitCommandExecutor;
        this.gitLogParser = gitLogParser;
    }

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
