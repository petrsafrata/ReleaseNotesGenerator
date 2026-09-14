package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import cz.jpmad.releasenotes.docker.ProcessDockerCommandRunner;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "status",
        description = "Show the status of the Ollama Docker container.",
        mixinStandardHelpOptions = true
)
public class AiStatusCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            DockerOllamaService service = createService();
            DockerOllamaState state = service.status();

            switch (state) {
                case RUNNING -> {
                    System.out.println("Ollama container is running.");
                    System.out.println("Ollama API: http://localhost:11434");
                }
                case STOPPED -> System.out.println("Ollama container is stopped.");
                case NOT_CREATED -> System.out.println("Ollama container has not been created.");
            }

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