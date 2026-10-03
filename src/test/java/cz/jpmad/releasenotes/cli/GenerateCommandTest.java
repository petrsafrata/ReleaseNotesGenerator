package cz.jpmad.releasenotes.cli;

import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationRequest;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationResult;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCommandTest {

    @Mock
    private ApplicationServicesFactory servicesFactory;

    @Mock
    private ReleaseNotesGenerationService generationService;

    private StringWriter standardOutput;
    private StringWriter errorOutput;
    private CommandLine commandLine;

    @BeforeEach
    void setUp() {
        standardOutput = new StringWriter();
        errorOutput = new StringWriter();

        commandLine = new CommandLine(
                new GenerateCommand(servicesFactory)
        );

        commandLine.setOut(new PrintWriter(standardOutput, true));
        commandLine.setErr(new PrintWriter(errorOutput, true));
    }

    @Test
    void generatesReleaseNotesUsingCommandOptions() {
        when(servicesFactory.createGenerationService(
                any(Path.class),
                any(URI.class)
        )).thenReturn(generationService);

        when(generationService.generate(
                any(ReleaseNotesGenerationRequest.class),
                any()
        )).thenReturn(
                new ReleaseNotesGenerationResult(
                        "generated markdown",
                        Optional.of(
                                "BombiXDev/TestRepoForGenerator"
                        ),
                        1,
                        true
                )
        );

        int exitCode = commandLine.execute(
                "--from",
                "0.0.3",
                "--to",
                "0.0.4",
                "--github",
                "--ai",
                "--model",
                "gemma3:4b",
                "--language",
                "en"
        );

        assertEquals(0, exitCode);
        assertEquals(
                "generated markdown",
                standardOutput.toString()
        );

        assertTrue(errorOutput.toString().contains(
                "GitHub repository loaded: "
                        + "BombiXDev/TestRepoForGenerator"
        ));

        assertTrue(errorOutput.toString().contains(
                "GitHub pull requests loaded: 1"
        ));

        assertTrue(errorOutput.toString().contains(
                "Release notes improved using Ollama model: gemma3:4b"
        ));

        ArgumentCaptor<ReleaseNotesGenerationRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        ReleaseNotesGenerationRequest.class
                );

        verify(generationService).generate(
                requestCaptor.capture(),
                any()
        );

        ReleaseNotesGenerationRequest request =
                requestCaptor.getValue();

        assertEquals("0.0.3", request.from());
        assertEquals("0.0.4", request.to());
        assertTrue(request.githubEnabled());
        assertTrue(request.aiEnabled());
        assertEquals("gemma3:4b", request.model());
        assertEquals("en", request.language());
    }
}