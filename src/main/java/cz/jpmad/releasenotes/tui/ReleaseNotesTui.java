package cz.jpmad.releasenotes.tui;

import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import cz.jpmad.releasenotes.tui.ai.AiManagementWorkflow;
import cz.jpmad.releasenotes.tui.command.CommandModeWorkflow;
import cz.jpmad.releasenotes.tui.generate.GenerateReleaseNotesWorkflow;
import lombok.extern.log4j.Log4j2;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;

import java.io.IOException;

@Log4j2
public class ReleaseNotesTui {

    private final CommandLine commandLine;
    private final TuiConsole tuiConsole;
    private final CommandLineTerminalAdapter commandLineTerminalAdapter;
    private final MainMenuScreen mainMenuScreen;
    private final CommandModeWorkflow commandModeWorkflow;
    private final GenerateReleaseNotesWorkflow generateReleaseNotesWorkflow;
    private final AiManagementWorkflow aiManagementWorkflow;

    public ReleaseNotesTui(CommandLine commandLine) {
        this.commandLine = commandLine;
        ApplicationServicesFactory servicesFactory = new ApplicationServicesFactory();
        TuiHeader tuiHeader = new TuiHeader();
        this.tuiConsole = new TuiConsole(tuiHeader);
        this.commandLineTerminalAdapter = new CommandLineTerminalAdapter(commandLine);
        this.mainMenuScreen = new MainMenuScreen(tuiHeader);
        this.commandModeWorkflow = new CommandModeWorkflow(
                commandLineTerminalAdapter,
                tuiConsole
        );
        this.generateReleaseNotesWorkflow = new GenerateReleaseNotesWorkflow(
                servicesFactory,
                tuiHeader,
                tuiConsole,
                commandLineTerminalAdapter
        );
        this.aiManagementWorkflow = new AiManagementWorkflow(
                tuiHeader,
                tuiConsole,
                commandLineTerminalAdapter
        );
    }

    public int run() {
        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {
            commandLineTerminalAdapter.configure(terminal);

            if (Terminal.TYPE_DUMB.equals(terminal.getType())) {
                commandLine.usage(terminal.writer());
                return 0;
            }

            LineReader lineReader = createLineReader(terminal);

            runMainMenu(terminal, lineReader);
            tuiConsole.printGoodbye(terminal);

            return 0;
        } catch (IOException exception) {
            log.error("Unable to run the terminal interface.", exception);
            System.err.println("Error: Unable to run the terminal interface.");
            return 1;
        }
    }

    private LineReader createLineReader(Terminal terminal) {
        LineReader lineReader = LineReaderBuilder.builder()
                .appName("release-notes")
                .terminal(terminal)
                .completer(
                        new PicocliJLineCompleter(
                                commandLine.getCommandSpec()
                        )
                )
                .option(LineReader.Option.CASE_INSENSITIVE, true)
                .build();

        lineReader.getKeyMaps()
                .get(LineReader.MAIN)
                .bind(
                        new Reference(LineReader.SEND_BREAK),
                        KeyMap.esc()
                );

        return lineReader;
    }

    private void runMainMenu(Terminal terminal, LineReader lineReader) throws IOException {
        boolean running = true;

        while (running) {
            MainMenuAction selectedAction = mainMenuScreen.show(
                    lineReader,
                    terminal
            );

            switch (selectedAction) {
                case GENERATE -> generateReleaseNotesWorkflow.run(
                        terminal,
                        lineReader
                );
                case AI -> aiManagementWorkflow.run(
                        terminal,
                        lineReader
                );
                case COMMAND_MODE -> {
                    boolean exitRequested = commandModeWorkflow.run(
                            terminal,
                            lineReader
                    );

                    if (exitRequested) {
                        running = false;
                    }
                }
                case EXIT -> running = false;
            }
        }
    }
}