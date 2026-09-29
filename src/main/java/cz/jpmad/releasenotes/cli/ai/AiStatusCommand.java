package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "status",
        description = "Show the status of the Ollama Docker container.",
        mixinStandardHelpOptions = true
)
public class AiStatusCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private AiCommand parentCommand;

    @Override
    public Integer call() {
        try {
            DockerOllamaService service = parentCommand.servicesFactory().createDockerOllamaService();
            DockerOllamaState state = service.status();

            switch (state) {
                case RUNNING -> {
                    parentCommand.out().println("Ollama container is running.");
                    parentCommand.out().println("Ollama API: http://localhost:11434");
                }
                case STOPPED -> parentCommand.out().println("Ollama container is stopped.");
                case NOT_CREATED -> parentCommand.out().println("Ollama container has not been created.");
            }

            return 0;
        } catch (DockerException exception) {
            parentCommand.printError(exception);
            return 1;
        }
    }
}