package cz.jpmad.releasenotes.tui.generate;

import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import cz.jpmad.releasenotes.docker.DockerException;
import cz.jpmad.releasenotes.git.GitCommandException;
import cz.jpmad.releasenotes.tui.CommandLineTerminalAdapter;
import cz.jpmad.releasenotes.tui.TuiConsole;
import cz.jpmad.releasenotes.tui.TuiHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class GenerateReleaseNotesWorkflow {

    private final ApplicationServicesFactory servicesFactory;
    private final TuiHeader tuiHeader;
    private final TuiConsole tuiConsole;
    private final CommandLineTerminalAdapter commandLineTerminalAdapter;

    public void run(Terminal terminal, LineReader lineReader) {
        tuiConsole.clear(terminal);

        try {
            GenerateReleaseNotesScreen screen =
                    new GenerateReleaseNotesScreen(
                            lineReader,
                            terminal,
                            servicesFactory,
                            tuiHeader
                    );

            Optional<GenerateReleaseNotesConfiguration> configuration = screen.show();

            if (configuration.isEmpty()) {
                return;
            }

            executeGeneration(configuration.orElseThrow(), terminal);

            tuiConsole.waitForReturn(
                    terminal,
                    lineReader
            );
        } catch (GitCommandException exception) {
            log.warn("Unable to load Git references.", exception);
            showError(terminal, lineReader, exception.getMessage());
        } catch (DockerException exception) {
            log.warn("Unable to prepare the local Ollama container.", exception);
            showError(terminal, lineReader, exception.getMessage());
        } catch (OllamaException exception) {
            log.warn("Unable to load installed Ollama models.", exception);
            showError(terminal, lineReader, exception.getMessage(),
                    "Open Manage Ollama AI to start Ollama or download a model."
            );
        } catch (IllegalArgumentException exception) {
            log.warn("Invalid Ollama URL was entered.", exception);
            showError(terminal, lineReader, "Invalid Ollama API URL.");
        } catch (IOException exception) {
            log.error("Unable to display the release notes generation screen.", exception);
            showError(terminal, lineReader, "Unable to display the generation screen.");
        } finally {
            tuiConsole.clear(terminal);
        }
    }

    private void executeGeneration(GenerateReleaseNotesConfiguration configuration, Terminal terminal) {
        tuiConsole.clearWithHeader(terminal);
        tuiConsole.printPageHeader(
                terminal,
                "◆ Generating release notes",
                "Please wait while the selected sources are processed."
        );

        try {
            int exitCode = commandLineTerminalAdapter.execute(
                    terminal,
                    configuration.toArguments()
                            .toArray(String[]::new)
            );

            tuiConsole.printGenerationResult(
                    terminal,
                    exitCode
            );
        } catch (RuntimeException exception) {
            log.error("Release notes generation from TUI failed.", exception);
            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
            terminal.writer().flush();
        }
    }

    private void showError(Terminal terminal, LineReader lineReader, String message, String... additionalMessages) {
        terminal.writer().println("Error: " + message);

        for (String additionalMessage : additionalMessages) {
            terminal.writer().println(additionalMessage);
        }

        terminal.writer().flush();

        tuiConsole.waitForReturn(
                terminal,
                lineReader
        );
    }
}