package cz.jpmad.releasenotes.tui.ai;

import cz.jpmad.releasenotes.tui.CommandLineTerminalAdapter;
import cz.jpmad.releasenotes.tui.TuiConsole;
import cz.jpmad.releasenotes.tui.TuiHeader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class AiManagementWorkflow {

    @NonNull
    private final TuiHeader tuiHeader;

    @NonNull
    private final TuiConsole tuiConsole;

    @NonNull
    private final CommandLineTerminalAdapter commandLineTerminalAdapter;

    public void run(Terminal terminal, LineReader lineReader) {
        try {
            AiManagementScreen screen = new AiManagementScreen(lineReader, terminal, tuiHeader);

            boolean running = true;

            while (running) {
                tuiConsole.clear(terminal);

                Optional<AiManagementAction> selectedAction = screen.show();

                if (selectedAction.isEmpty()) {
                    return;
                }

                switch (selectedAction.orElseThrow()) {
                    case STATUS -> {
                        if (runCommand(
                                terminal,
                                lineReader,
                                "status"
                        )) {
                            return;
                        }
                    }
                    case START -> {
                        if (runCommand(
                                terminal,
                                lineReader,
                                "start"
                        )) {
                            return;
                        }
                    }
                    case STOP -> {
                        if (runCommand(
                                terminal,
                                lineReader,
                                "stop"
                        )) {
                            return;
                        }
                    }
                    case MODELS -> {
                        if (runCommand(
                                terminal,
                                lineReader,
                                "models"
                        )) {
                            return;
                        }
                    }
                    case PULL -> {
                        tuiConsole.clear(terminal);

                        Optional<String> model = screen.promptModel();

                        if (model.isEmpty()) {
                            return;
                        }

                        if (runCommand(terminal, lineReader, "pull", model.orElseThrow())) {
                            return;
                        }
                    }
                    case BACK -> running = false;
                }
            }
        } catch (IOException exception) {
            log.error("Unable to display the Ollama AI management screen.", exception);
            terminal.writer().println(
                    "Error: Unable to display the Ollama AI management screen."
            );
            terminal.writer().flush();

            tuiConsole.waitForReturn(
                    terminal,
                    lineReader
            );
        } finally {
            tuiConsole.clear(terminal);
        }
    }

    private boolean runCommand(Terminal terminal, LineReader lineReader, String... arguments) {
        tuiConsole.clearWithHeader(terminal);
        tuiConsole.printPageHeader(
                terminal,
                "◆ Ollama AI"
        );

        String[] commandArguments = new String[arguments.length + 1];

        commandArguments[0] = "ai";

        System.arraycopy(
                arguments,
                0,
                commandArguments,
                1,
                arguments.length
        );

        try {
            commandLineTerminalAdapter.execute(
                    terminal,
                    commandArguments
            );
        } catch (RuntimeException exception) {
            log.error("Ollama AI command execution failed.", exception);
            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
            terminal.writer().flush();
        }

        return tuiConsole.waitForReturn(
                terminal,
                lineReader
        );
    }
}