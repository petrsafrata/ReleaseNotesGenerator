package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import cz.jpmad.releasenotes.docker.ProcessDockerCommandRunner;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "stop",
        description = "Stop the Ollama Docker container.",
        mixinStandardHelpOptions = true
)
public class AiStopCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            DockerOllamaService service = createService();
            DockerOllamaState currentState = service.status();

            if (currentState == DockerOllamaState.NOT_CREATED) {
                System.out.println("Ollama container has not been created.");
                return 0;
            }

            if (currentState == DockerOllamaState.STOPPED) {
                System.out.println("Ollama container is already stopped.");
                return 0;
            }

            service.stop();
            System.out.println("Ollama container was stopped.");

            return 0;
        } catch (DockerException exception) {
            System.err.println("Error: " + exception.getMessage());
            return 1;
        }
    }

    private DockerOllamaService createService() {
        return new DockerOllamaService(new ProcessDockerCommandRunner());
    }
}