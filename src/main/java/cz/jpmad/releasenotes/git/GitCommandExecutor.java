package cz.jpmad.releasenotes.git;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GitCommandExecutor {

    private final Path workingDirectory;

    public GitCommandExecutor(Path workingDirectory) {
        this.workingDirectory = workingDirectory
                .toAbsolutePath()
                .normalize();
    }

    public String execute(String... arguments) {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new GitCommandException("Git command failed with exit code %d: %s".formatted(exitCode, output));
            }
            return output;
        } catch (IOException exception) {
            throw new GitCommandException("Unable to start Git. Make sure Git is installed.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GitCommandException("Git command execution was interrupted.", exception);
        }
    }
}
