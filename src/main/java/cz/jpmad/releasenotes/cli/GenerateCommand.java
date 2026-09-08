package cz.jpmad.releasenotes.cli;

import cz.jpmad.releasenotes.git.*;
import cz.jpmad.releasenotes.output.MarkdownOutputWriter;
import cz.jpmad.releasenotes.release.*;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "generate",
        description = "Generate release notes between two Git references.",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = "--from",
            description = "Start Git reference. Defaults to the latest tag."
    )
    private String from;

    @CommandLine.Option(
            names = "--to",
            description = "Target Git reference.",
            defaultValue = "HEAD"
    )
    private String to;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Write release notes to a Markdown file."
    )
    private Path output;

    @Override
    public Integer call() {
        try {
            GitCommandExecutor gitCommandExecutor = new GitCommandExecutor(Path.of("."));
            GitRepositoryService gitRepositoryService = new GitRepositoryService(gitCommandExecutor);
            GitTagService gitTagService = new GitTagService(gitCommandExecutor);
            ReleaseRangeResolver rangeResolver = new ReleaseRangeResolver(gitTagService);
            GitCommitService gitCommitService = new GitCommitService(gitCommandExecutor, new GitLogParser());
            ChangeCategorizer changeCategorizer = new ChangeCategorizer();
            MarkdownRenderer markdownRenderer = new MarkdownRenderer();

            gitRepositoryService.getRepositoryInfo();
            ReleaseRange range = rangeResolver.resolve(from, to);
            List<GitCommit> commits = gitCommitService.findCommits(range);
            List<ReleaseChange> changes = changeCategorizer.categorize(commits);
            String markdown = markdownRenderer.render(range, changes);
            writeOutput(markdown);
            return 0;
        } catch (GitCommandException | ReleaseRangeException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private void writeOutput(String markdown) throws IOException {
        if (output == null) {
            System.out.print(markdown);
            return;
        }
        MarkdownOutputWriter markdownOutputWriter = new MarkdownOutputWriter();
        Path writtenFile = markdownOutputWriter.write(
                output,
                markdown
        );
        System.err.println("Release notes written to: " + writtenFile);
    }
}
