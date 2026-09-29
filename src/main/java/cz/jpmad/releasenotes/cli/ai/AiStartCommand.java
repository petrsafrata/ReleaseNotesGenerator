package cz.jpmad.releasenotes.cli.ai;

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
            DockerOllamaService service = parentCommand.servicesFactory().createDockerOllamaService();
            DockerOllamaState currentState = service.status();

            if (currentState == DockerOllamaState.RUNNING) {
                parentCommand.out().println("Ollama container is already running.");
                return 0;
            }

            service.start();

            if (currentState == DockerOllamaState.NOT_CREATED) {
                parentCommand.out().println("Ollama container was created and started.");
            } else {
                parentCommand.out().println("Ollama container was started.");
            }

            parentCommand.out().println("Ollama API: http://localhost:11434");

            return 0;
        } catch (DockerException exception) {
            parentCommand.printError(exception);
            return 1;
        }
    }
}