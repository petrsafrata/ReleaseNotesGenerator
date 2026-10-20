package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "stop",
        description = "Stop the Ollama Docker container.",
        mixinStandardHelpOptions = true
)
public class AiStopCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private AiCommand parentCommand;

    @Override
    public Integer call() {
        try {
            DockerOllamaService service = parentCommand.createDockerOllamaService();
            DockerOllamaState currentState = service.status();

            if (currentState == DockerOllamaState.NOT_CREATED) {
                parentCommand.out().println("Ollama container has not been created.");
                return CommandLine.ExitCode.OK;
            }

            if (currentState == DockerOllamaState.STOPPED) {
                parentCommand.out().println("Ollama container is already stopped.");
                return CommandLine.ExitCode.OK;
            }

            service.stop();
            parentCommand.out().println("Ollama container was stopped.");
            return CommandLine.ExitCode.OK;
        } catch (DockerException exception) {
            parentCommand.printError("Unable to stop the Ollama container.", exception);
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}