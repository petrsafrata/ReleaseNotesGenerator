package cz.jpmad.releasenotes.docker;

public class DockerOllamaService {

    private static final String CONTAINER_NAME = "release-notes-ollama";
    private static final String VOLUME_NAME = "release-notes-ollama-data";
    private static final String IMAGE_NAME = "ollama/ollama";
    private static final String OLLAMA_PORT = "11434:11434";

    private final DockerCommandRunner commandRunner;

    public DockerOllamaService(DockerCommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public DockerOllamaState status() {
        requireDocker();

        DockerCommandResult result = commandRunner.execute(
                "container",
                "inspect",
                "--format",
                "{{.State.Running}}",
                CONTAINER_NAME
        );

        if (!result.successful()) {
            return DockerOllamaState.NOT_CREATED;
        }

        if ("true".equalsIgnoreCase(result.output())) {
            return DockerOllamaState.RUNNING;
        }

        return DockerOllamaState.STOPPED;
    }

    public DockerOllamaState start() {
        DockerOllamaState currentState = status();

        if (currentState == DockerOllamaState.RUNNING) {
            return DockerOllamaState.RUNNING;
        }

        if (currentState == DockerOllamaState.STOPPED) {
            DockerCommandResult result = commandRunner.execute("start", CONTAINER_NAME);
            requireSuccessful(result, "Unable to start the Ollama container.");

            return DockerOllamaState.RUNNING;
        }

        DockerCommandResult result = commandRunner.execute(
                "run",
                "-d",
                "--name",
                CONTAINER_NAME,
                "-v",
                VOLUME_NAME + ":/root/.ollama",
                "-p",
                OLLAMA_PORT,
                IMAGE_NAME
        );

        requireSuccessful(result, "Unable to create the Ollama container.");

        return DockerOllamaState.RUNNING;
    }

    public DockerOllamaState stop() {
        DockerOllamaState currentState = status();

        if (currentState == DockerOllamaState.NOT_CREATED) {
            return DockerOllamaState.NOT_CREATED;
        }

        if (currentState == DockerOllamaState.STOPPED) {
            return DockerOllamaState.STOPPED;
        }

        DockerCommandResult result = commandRunner.execute("stop", CONTAINER_NAME);
        requireSuccessful(result, "Unable to stop the Ollama container.");

        return DockerOllamaState.STOPPED;
    }

    private void requireDocker() {
        DockerCommandResult result = commandRunner.execute(
                "version",
                "--format",
                "{{.Server.Version}}"
        );

        if (!result.successful()) {
            throw new DockerException("Docker is not available. Make sure Docker Desktop or Docker Engine is running.");
        }
    }

    private void requireSuccessful(DockerCommandResult result, String message) {
        if (result.successful()) {
            return;
        }

        if (result.output().isBlank()) {
            throw new DockerException(message);
        }

        throw new DockerException(message + " " + result.output());
    }
}