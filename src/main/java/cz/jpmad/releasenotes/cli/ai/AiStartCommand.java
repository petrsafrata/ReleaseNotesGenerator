package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.config.ApplicationDefaults;
import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "start",
        description = "Start the Ollama Docker container.",
        mixinStandardHelpOptions = true
)
public class AiStartCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private AiCommand parentCommand;

    @Override
    public Integer call() {
        try {
            DockerOllamaService service = parentCommand.createDockerOllamaService();
            DockerOllamaState currentState = service.status();

            if (currentState == DockerOllamaState.RUNNING) {
                parentCommand.out().println("Ollama container is already running.");
                return CommandLine.ExitCode.OK;
            }

            service.start();

            if (currentState == DockerOllamaState.NOT_CREATED) {
                parentCommand.out().println("Ollama container was created and started.");
            } else {
                parentCommand.out().println("Ollama container was started.");
            }

            parentCommand.out().println("Ollama API: " + ApplicationDefaults.OLLAMA_URL);
            return CommandLine.ExitCode.OK;
        } catch (DockerException exception) {
            parentCommand.printError("Unable to start the Ollama container.", exception);
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}