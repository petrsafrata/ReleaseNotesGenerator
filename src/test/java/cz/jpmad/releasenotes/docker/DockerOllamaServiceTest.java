package cz.jpmad.releasenotes.docker;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerOllamaServiceTest {

    @Test
    void reportsRunningContainer() {
        DockerCommandRunner commandRunner = arguments -> {
            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            return successful("true");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        assertEquals(DockerOllamaState.RUNNING, service.status());
    }

    @Test
    void reportsStoppedContainer() {
        DockerCommandRunner commandRunner = arguments -> {
            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            return successful("false");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        assertEquals(DockerOllamaState.STOPPED, service.status());
    }

    @Test
    void reportsMissingContainer() {
        DockerCommandRunner commandRunner = arguments -> {
            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            return failed("No such container");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        assertEquals(DockerOllamaState.NOT_CREATED, service.status());
    }

    @Test
    void createsMissingContainer() {
        List<List<String>> executedCommands = new ArrayList<>();

        DockerCommandRunner commandRunner = arguments -> {
            executedCommands.add(Arrays.asList(arguments));

            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            if (arguments[0].equals("container")) {
                return failed("No such container");
            }

            return successful("container-id");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        assertEquals(DockerOllamaState.RUNNING, service.start());
        assertTrue(executedCommands.contains(List.of(
                "run",
                "-d",
                "--name",
                "release-notes-ollama",
                "-v",
                "release-notes-ollama-data:/root/.ollama",
                "-p",
                "11434:11434",
                "ollama/ollama"
        )));
    }

    @Test
    void startsExistingStoppedContainer() {
        List<List<String>> executedCommands = new ArrayList<>();

        DockerCommandRunner commandRunner = arguments -> {
            executedCommands.add(Arrays.asList(arguments));

            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            if (arguments[0].equals("container")) {
                return successful("false");
            }

            return successful("release-notes-ollama");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        assertEquals(DockerOllamaState.RUNNING, service.start());
        assertTrue(executedCommands.contains(List.of("start", "release-notes-ollama")));
    }

    @Test
    void doesNotStartRunningContainerAgain() {
        List<List<String>> executedCommands = new ArrayList<>();

        DockerCommandRunner commandRunner = arguments -> {
            executedCommands.add(Arrays.asList(arguments));

            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            return successful("true");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        assertEquals(DockerOllamaState.RUNNING, service.start());
        assertEquals(2, executedCommands.size());
    }

    @Test
    void stopsRunningContainer() {
        List<List<String>> executedCommands = new ArrayList<>();

        DockerCommandRunner commandRunner = arguments -> {
            executedCommands.add(Arrays.asList(arguments));

            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            if (arguments[0].equals("container")) {
                return successful("true");
            }

            return successful("release-notes-ollama");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        assertEquals(DockerOllamaState.STOPPED, service.stop());
        assertTrue(executedCommands.contains(List.of("stop", "release-notes-ollama")));
    }

    @Test
    void reportsUnavailableDocker() {
        DockerCommandRunner commandRunner = arguments -> failed(
                "Cannot connect to the Docker daemon"
        );

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        DockerException exception = assertThrows(DockerException.class, service::status);

        assertEquals(
                "Docker is not available. Make sure Docker Desktop or Docker Engine is running.",
                exception.getMessage()
        );
    }

    @Test
    void reportsContainerCreationFailure() {
        DockerCommandRunner commandRunner = arguments -> {
            if (arguments[0].equals("version")) {
                return successful("28.0.0");
            }

            if (arguments[0].equals("container")) {
                return failed("No such container");
            }

            return failed("Port 11434 is already in use");
        };

        DockerOllamaService service = new DockerOllamaService(commandRunner);

        DockerException exception = assertThrows(DockerException.class, service::start);

        assertEquals(
                "Unable to create the Ollama container. Port 11434 is already in use",
                exception.getMessage()
        );
    }

    private DockerCommandResult successful(String output) {
        return new DockerCommandResult(0, output);
    }

    private DockerCommandResult failed(String output) {
        return new DockerCommandResult(1, output);
    }
}