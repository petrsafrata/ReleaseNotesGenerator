package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiCommandTest {

    @Mock
    private ApplicationServicesFactory servicesFactory;

    @Mock
    private DockerOllamaService dockerOllamaService;

    @Mock
    private OllamaApiClient ollamaApiClient;

    private StringWriter standardOutput;
    private StringWriter errorOutput;
    private CommandLine commandLine;

    @BeforeEach
    void setUp() {
        standardOutput = new StringWriter();
        errorOutput = new StringWriter();

        commandLine = new CommandLine(
                new AiCommand(servicesFactory)
        );

        commandLine.setOut(new PrintWriter(standardOutput, true));
        commandLine.setErr(new PrintWriter(errorOutput, true));
    }

    @Test
    void startsNewOllamaContainer() {
        when(servicesFactory.createDockerOllamaService()).thenReturn(dockerOllamaService);
        when(dockerOllamaService.status()).thenReturn(DockerOllamaState.NOT_CREATED);

        int exitCode = commandLine.execute("start");

        assertEquals(0, exitCode);
        assertTrue(standardOutput.toString().contains(
                "Ollama container was created and started."
        ));
        assertTrue(standardOutput.toString().contains(
                "Ollama API: http://localhost:11434"
        ));

        verify(dockerOllamaService).start();
    }

    @Test
    void reportsRunningOllamaContainer() {
        when(servicesFactory.createDockerOllamaService()).thenReturn(dockerOllamaService);
        when(dockerOllamaService.status()).thenReturn(DockerOllamaState.RUNNING);

        int exitCode = commandLine.execute("status");

        assertEquals(0, exitCode);
        assertTrue(standardOutput.toString().contains(
                "Ollama container is running."
        ));
        assertTrue(standardOutput.toString().contains(
                "Ollama API: http://localhost:11434"
        ));
    }

    @Test
    void stopsRunningOllamaContainer() {
        when(servicesFactory.createDockerOllamaService()).thenReturn(dockerOllamaService);
        when(dockerOllamaService.status()).thenReturn(DockerOllamaState.RUNNING);

        int exitCode = commandLine.execute("stop");

        assertEquals(0, exitCode);
        assertTrue(standardOutput.toString().contains(
                "Ollama container was stopped."
        ));

        verify(dockerOllamaService).stop();
    }

    @Test
    void reportsWhenNoModelsAreInstalled() {
        when(servicesFactory.createOllamaApiClient(
                URI.create("http://localhost:11434")
        )).thenReturn(ollamaApiClient);

        when(ollamaApiClient.getModels()).thenReturn(List.of());

        int exitCode = commandLine.execute("models");

        assertEquals(0, exitCode);
        assertTrue(standardOutput.toString().contains(
                "No Ollama models are installed."
        ));
        assertTrue(standardOutput.toString().contains(
                "Install one using: release-notes ai pull <model>"
        ));
    }

    @Test
    void pullsOllamaModel() {
        when(servicesFactory.createOllamaApiClient(
                URI.create("http://localhost:11434")
        )).thenReturn(ollamaApiClient);

        int exitCode = commandLine.execute(
                "pull",
                "gemma3:4b"
        );

        assertEquals(0, exitCode);
        assertTrue(standardOutput.toString().contains(
                "Downloading Ollama model: gemma3:4b"
        ));
        assertTrue(standardOutput.toString().contains(
                "Ollama model downloaded successfully: gemma3:4b"
        ));

        verify(ollamaApiClient).pullModel("gemma3:4b");
    }

    @Test
    void returnsFailureWhenDockerCommandFails() {
        when(servicesFactory.createDockerOllamaService()).thenReturn(dockerOllamaService);
        when(dockerOllamaService.status()).thenThrow(
                new DockerException("Docker is not available.")
        );

        int exitCode = commandLine.execute("status");

        assertEquals(1, exitCode);
        assertTrue(errorOutput.toString().contains(
                "Error: Docker is not available."
        ));
    }
}