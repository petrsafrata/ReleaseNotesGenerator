package cz.jpmad.releasenotes.docker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProcessDockerCommandRunner implements DockerCommandRunner {

    @Override
    public DockerCommandResult execute(String... arguments) {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.addAll(List.of(arguments));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
            int exitCode = process.waitFor();

            return new DockerCommandResult(exitCode, output);
        } catch (IOException exception) {
            throw new DockerException("Unable to start Docker. Make sure Docker is installed.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DockerException("Docker command execution was interrupted.", exception);
        }
    }
}
