package cz.jpmad.releasenotes.tui;

import cz.jpmad.releasenotes.tui.ai.AiManagementWorkflow;
import cz.jpmad.releasenotes.tui.command.CommandModeWorkflow;
import cz.jpmad.releasenotes.tui.generate.GenerateReleaseNotesWorkflow;
import lombok.extern.log4j.Log4j2;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;
import cz.jpmad.releasenotes.config.ApplicationServicesFactory;

import java.io.IOException;
import java.util.Map;

import org.jline.keymap.KeyMap;
import org.jline.reader.Reference;

@Log4j2
public class ReleaseNotesTui {

    private static final String MAIN_MENU = "main-menu";
    private static final String GENERATE = "generate";
    private static final String AI = "ai";
    private static final String COMMAND_MODE = "command-mode";
    private static final String EXIT = "exit";

    private final CommandLine commandLine;
    private final ApplicationServicesFactory servicesFactory;
    private final TuiHeader tuiHeader;
    private final TuiConsole tuiConsole;
    private final CommandLineTerminalAdapter commandLineTerminalAdapter;
    private final CommandModeWorkflow commandModeWorkflow;
    private final GenerateReleaseNotesWorkflow generateReleaseNotesWorkflow;
    private final AiManagementWorkflow aiManagementWorkflow;

    public ReleaseNotesTui(CommandLine commandLine) {
        this.commandLine = commandLine;
        this.servicesFactory = new ApplicationServicesFactory();
        this.tuiHeader = new TuiHeader();
        this.tuiConsole = new TuiConsole(tuiHeader);
        this.commandLineTerminalAdapter = new CommandLineTerminalAdapter(commandLine);
        this.commandModeWorkflow = new CommandModeWorkflow(commandLineTerminalAdapter, tuiConsole);
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
            log.error("Unable to initialize the terminal interface.", exception);
            System.err.println("Error: Unable to initialize the terminal interface.");
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

    private ConsolePrompt createConsolePrompt(LineReader lineReader, Terminal terminal) {
        ConsolePrompt.UiConfig uiConfig = new ConsolePrompt.UiConfig(
                ">",
                "( )",
                "(x)",
                "( )"
        );

        uiConfig.setCancellableFirstPrompt(false);

        return new ConsolePrompt(
                lineReader,
                terminal,
                uiConfig
        );
    }

    private void runMainMenu(Terminal terminal, LineReader lineReader) throws IOException {
        boolean running = true;

        while (running) {
            String selectedItem = showMainMenu(
                    lineReader,
                    terminal
            );

            switch (selectedItem) {
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
                default -> throw new IllegalStateException(
                        "Unknown TUI menu item: " + selectedItem
                );
            }
        }
    }

    private String showMainMenu(LineReader lineReader, Terminal terminal) throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt(
                lineReader,
                terminal
        );

        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        promptBuilder.createListPrompt()
                .name(MAIN_MENU)
                .message("What do you want to do?")
                .newItem(GENERATE)
                .text("Generate release notes")
                .add()
                .newItem(AI)
                .text("Manage Ollama AI")
                .add()
                .newItem(COMMAND_MODE)
                .text("Command mode")
                .add()
                .newItem(EXIT)
                .text("Exit")
                .add()
                .addPrompt();

        Map<String, PromptResultItemIF> result =
                consolePrompt.prompt(
                        tuiHeader.create(),
                        promptBuilder.build()
                );

        if (result == null || result.isEmpty()) {
            return EXIT;
        }

        return result.get(MAIN_MENU).getResult();
    }
}