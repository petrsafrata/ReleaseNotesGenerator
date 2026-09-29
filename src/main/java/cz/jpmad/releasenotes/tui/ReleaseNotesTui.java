package cz.jpmad.releasenotes.tui;

import lombok.extern.log4j.Log4j2;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliJLineCompleter;
import cz.jpmad.releasenotes.tui.generate.GenerateReleaseNotesConfiguration;
import cz.jpmad.releasenotes.tui.generate.GenerateReleaseNotesScreen;
import cz.jpmad.releasenotes.tui.ai.AiManagementAction;
import cz.jpmad.releasenotes.tui.ai.AiManagementScreen;
import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.git.GitCommandException;
import cz.jpmad.releasenotes.docker.DockerException;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

    public ReleaseNotesTui(CommandLine commandLine) {
        this.commandLine = commandLine;
        this.servicesFactory = new ApplicationServicesFactory();
    }

    public int run() {
        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {
            configureCommandLine(terminal);

            if (Terminal.TYPE_DUMB.equals(terminal.getType())) {
                commandLine.usage(terminal.writer());
                return 0;
            }

            LineReader lineReader = createLineReader(terminal);

            printWelcome(terminal);
            runMainMenu(terminal, lineReader);
            printGoodbye(terminal);

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

    private void configureCommandLine(Terminal terminal) {
        configureCommandLine(
                commandLine,
                terminal
        );
    }

    private void configureCommandLine(CommandLine currentCommandLine,
                                      Terminal terminal) {
        currentCommandLine.setOut(terminal.writer());
        currentCommandLine.setErr(terminal.writer());

        for (CommandLine subcommand :
                currentCommandLine.getSubcommands().values()) {
            configureCommandLine(
                    subcommand,
                    terminal
            );
        }
    }

    private void runMainMenu(Terminal terminal, LineReader lineReader) throws IOException {
        boolean running = true;

        while (running) {
            String selectedItem = showMainMenu(
                    lineReader,
                    terminal
            );

            switch (selectedItem) {
                case GENERATE -> runGenerateScreen(
                        terminal,
                        lineReader
                );
                case AI -> runAiScreen(
                        terminal,
                        lineReader
                );
                case COMMAND_MODE -> {
                    clearScreen(terminal);

                    running = !runCommandMode(
                            terminal,
                            lineReader
                    );

                    clearScreen(terminal);
                }
                case EXIT -> running = false;
                default -> throw new IllegalStateException(
                        "Unknown TUI menu item: " + selectedItem
                );
            }
        }
    }

    private void runGenerateScreen(Terminal terminal, LineReader lineReader) {
        clearScreen(terminal);

        try {
            GenerateReleaseNotesScreen screen =
                    new GenerateReleaseNotesScreen(
                            lineReader,
                            terminal,
                            servicesFactory
                    );

            Optional<GenerateReleaseNotesConfiguration> configuration =
                    screen.show();

            if (configuration.isEmpty()) {
                return;
            }

            try {
                clearScreen(terminal);
                printGenerationHeader(terminal);


                commandLine.execute(configuration.get()
                                .toArguments()
                                .toArray(String[]::new)
                );
            } catch (RuntimeException exception) {
                log.error(
                        "Release notes generation from TUI failed.",
                        exception
                );

                terminal.writer().println(
                        "Error: " + exception.getMessage()
                );
            } finally {
                flushCommandOutput(
                        commandLine,
                        terminal
                );
            }

            waitForReturn(
                    terminal,
                    lineReader
            );
        } catch (GitCommandException exception) {
            log.warn(
                    "Unable to load Git references.",
                    exception
            );

            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
            terminal.writer().flush();

            waitForReturn(
                    terminal,
                    lineReader
            );
        } catch (DockerException exception) {
            log.warn(
                    "Unable to prepare the local Ollama container.",
                    exception
            );

            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
            terminal.writer().flush();

            waitForReturn(
                    terminal,
                    lineReader
            );
        } catch (OllamaException exception) {
            log.warn(
                    "Unable to load installed Ollama models.",
                    exception
            );

            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
            terminal.writer().println(
                    "Open Manage Ollama AI to start Ollama or download a model."
            );
            terminal.writer().flush();

            waitForReturn(
                    terminal,
                    lineReader
            );
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Invalid Ollama URL was entered.",
                    exception
            );

            terminal.writer().println(
                    "Error: Invalid Ollama API URL."
            );
            terminal.writer().flush();

            waitForReturn(
                    terminal,
                    lineReader
            );
        } catch (IOException exception) {
            log.error(
                    "Unable to display the release notes generation screen.",
                    exception
            );

            terminal.writer().println(
                    "Error: Unable to display the generation screen."
            );
            terminal.writer().flush();

            waitForReturn(
                    terminal,
                    lineReader
            );
        } finally {
            clearScreen(terminal);
        }
    }

    private String showMainMenu(LineReader lineReader,
                                Terminal terminal) throws IOException {
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
                consolePrompt.prompt(promptBuilder.build());

        if (result == null) {
            return EXIT;
        }

        return result.get(MAIN_MENU).getResult();
    }

    private void runAiScreen(Terminal terminal,
                             LineReader lineReader) {
        try {
            AiManagementScreen screen = new AiManagementScreen(
                    lineReader,
                    terminal
            );

            boolean running = true;

            while (running) {
                clearScreen(terminal);

                Optional<AiManagementAction> selectedAction =
                        screen.show();

                if (selectedAction.isEmpty()) {
                    return;
                }

                switch (selectedAction.get()) {
                    case STATUS -> {
                        if (runAiCommand(
                                terminal,
                                lineReader,
                                "status"
                        )) {
                            return;
                        }
                    }
                    case START -> {
                        if (runAiCommand(
                                terminal,
                                lineReader,
                                "start"
                        )) {
                            return;
                        }
                    }
                    case STOP -> {
                        if (runAiCommand(
                                terminal,
                                lineReader,
                                "stop"
                        )) {
                            return;
                        }
                    }
                    case MODELS -> {
                        if (runAiCommand(
                                terminal,
                                lineReader,
                                "models"
                        )) {
                            return;
                        }
                    }
                    case PULL -> {
                        clearScreen(terminal);

                        Optional<String> model =
                                screen.promptModel();

                        if (model.isEmpty()) {
                            return;
                        }

                        if (runAiCommand(
                                terminal,
                                lineReader,
                                "pull",
                                model.get()
                        )) {
                            return;
                        }
                    }
                    case BACK -> running = false;
                }
            }
        } catch (IOException exception) {
            log.error(
                    "Unable to display the Ollama AI management screen.",
                    exception
            );

            terminal.writer().println(
                    "Error: Unable to display the Ollama AI management screen."
            );
            terminal.writer().flush();

            waitForReturn(
                    terminal,
                    lineReader
            );
        } finally {
            clearScreen(terminal);
        }
    }

    private boolean runAiCommand(Terminal terminal,
                                 LineReader lineReader,
                                 String... arguments) {
        clearScreen(terminal);

        String[] commandArguments =
                new String[arguments.length + 1];

        commandArguments[0] = "ai";

        System.arraycopy(
                arguments,
                0,
                commandArguments,
                1,
                arguments.length
        );

        try {
            commandLine.execute(commandArguments);
        } catch (RuntimeException exception) {
            log.error(
                    "Ollama AI command execution failed.",
                    exception
            );

            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
        } finally {
            flushCommandOutput(
                    commandLine,
                    terminal
            );
        }

        return waitForReturn(
                terminal,
                lineReader
        );
    }

    private boolean waitForReturn(Terminal terminal,
                                  LineReader lineReader) {
        terminal.writer().println();
        terminal.writer().flush();

        try {
            lineReader.readLine(
                    new AttributedStringBuilder()
                            .style(
                                    AttributedStyle.DEFAULT
                                            .foreground(AttributedStyle.CYAN)
                            )
                            .append("Press Enter to continue or Esc to return to the main menu...")
                            .style(AttributedStyle.DEFAULT)
                            .toAnsi(terminal)
            );

            return false;
        } catch (UserInterruptException | EndOfFileException exception) {
            terminal.writer().println();
            return true;
        } finally {
            terminal.writer().flush();
        }
    }

    private void printGenerationHeader(Terminal terminal) {
        terminal.writer().println(
                new AttributedStringBuilder()
                        .style(
                                AttributedStyle.DEFAULT
                                        .bold()
                                        .foreground(AttributedStyle.CYAN)
                        )
                        .append("Generating release notes")
                        .style(AttributedStyle.DEFAULT)
                        .toAnsi(terminal)
        );

        terminal.writer().println();
        terminal.writer().flush();
    }

    private void clearScreen(Terminal terminal) {
        terminal.puts(
                InfoCmp.Capability.clear_screen
        );
        terminal.writer().flush();
        terminal.flush();
    }

    private boolean runCommandMode(Terminal terminal, LineReader lineReader) {
        terminal.writer().println();
        terminal.writer().println(
                "Command mode. Type back to return to the menu."
        );
        terminal.writer().println(
                "Type exit to close the application."
        );
        terminal.writer().println();
        terminal.flush();

        while (true) {
            String input;

            try {
                input = lineReader.readLine(createPrompt(terminal)).strip();
            } catch (UserInterruptException exception) {
                terminal.writer().println();
                terminal.flush();
                return false;
            } catch (EndOfFileException exception) {
                return false;
            }

            if (input.isBlank()) {
                continue;
            }

            String normalizedInput = input.toLowerCase(Locale.ROOT);

            if (normalizedInput.equals("back")) {
                return false;
            }

            if (normalizedInput.equals("exit")
                    || normalizedInput.equals("quit")) {
                return true;
            }

            if (normalizedInput.equals("clear")) {
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.flush();
                continue;
            }

            executeCommand(input, lineReader, terminal);
        }
    }

    private void executeCommand(String input, LineReader lineReader,
                                Terminal terminal) {
        try {
            ParsedLine parsedLine = lineReader.getParser().parse(
                    input,
                    input.length(),
                    Parser.ParseContext.ACCEPT_LINE
            );

            String[] arguments = parsedLine.words().toArray(String[]::new);
            commandLine.execute(arguments);
        } catch (RuntimeException exception) {
            log.error("Interactive command execution failed.", exception);

            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
        } finally {
            flushCommandOutput(
                    commandLine,
                    terminal
            );
        }
    }

    private void flushCommandOutput(CommandLine currentCommandLine,
                                    Terminal terminal) {
        currentCommandLine.getOut().flush();
        currentCommandLine.getErr().flush();

        for (CommandLine subcommand :
                currentCommandLine.getSubcommands().values()) {
            flushCommandOutput(
                    subcommand,
                    terminal
            );
        }

        terminal.writer().flush();
        terminal.flush();
    }

    private void printWelcome(Terminal terminal) {
        terminal.writer().println(
                new AttributedStringBuilder()
                        .style(
                                AttributedStyle.DEFAULT
                                        .bold()
                                        .foreground(AttributedStyle.CYAN)
                        )
                        .append("Release Notes Generator")
                        .style(AttributedStyle.DEFAULT)
                        .toAnsi(terminal)
        );

        terminal.writer().println(
                "Use the arrow keys and Enter to select an action."
        );
        terminal.writer().println();
        terminal.flush();
    }

    private String createPrompt(Terminal terminal) {
        return new AttributedStringBuilder()
                .style(
                        AttributedStyle.DEFAULT
                                .foreground(AttributedStyle.CYAN)
                )
                .append("release-notes")
                .style(AttributedStyle.DEFAULT)
                .append("> ")
                .toAnsi(terminal);
    }

    private void printGoodbye(Terminal terminal) {
        terminal.writer().println();
        terminal.writer().println("Goodbye.");
        terminal.flush();
    }
}