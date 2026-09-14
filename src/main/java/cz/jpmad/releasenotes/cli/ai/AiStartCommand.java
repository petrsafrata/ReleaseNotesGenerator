package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import cz.jpmad.releasenotes.docker.ProcessDockerCommandRunner;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "start",
        description = "Start the Ollama Docker container.",
        mixinStandardHelpOptions = true
)
public class AiStartCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            DockerOllamaService service = createService();
            DockerOllamaState currentState = service.status();

            if (currentState == DockerOllamaState.RUNNING) {
                System.out.println("Ollama container is already running.");
                return 0;
            }

            service.start();

            if (currentState == DockerOllamaState.NOT_CREATED) {
                System.out.println("Ollama container was created and started.");
            } else {
                System.out.println("Ollama container was started.");
            }

            System.out.println("Ollama API: http://localhost:11434");

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