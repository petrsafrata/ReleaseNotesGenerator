package cz.jpmad.releasenotes.tui.command;

import cz.jpmad.releasenotes.tui.CommandLineTerminalAdapter;
import cz.jpmad.releasenotes.tui.TuiConsole;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

import java.util.Locale;

@Log4j2
@RequiredArgsConstructor
public class CommandModeWorkflow {

    private final CommandLineTerminalAdapter commandLineTerminalAdapter;
    private final TuiConsole tuiConsole;

    /**
     * @return true when the complete application should be closed
     */
    public boolean run(Terminal terminal, LineReader lineReader) {
        showPage(terminal);

        while (true) {
            String input;

            try {
                input = lineReader
                        .readLine(tuiConsole.createCommandPrompt(terminal))
                        .strip();
            } catch (UserInterruptException | EndOfFileException exception) {
                return false;
            }

            if (input.isBlank()) {
                continue;
            }

            String normalizedInput = input.toLowerCase(Locale.ROOT);

            switch (normalizedInput) {
                case "back" -> {
                    return false;
                }
                case "exit", "quit" -> {
                    return true;
                }
                case "clear", "cls" -> {
                    showPage(terminal);
                    continue;
                }
                default -> executeCommand(input, lineReader, terminal);
            }
        }
    }

    private void showPage(Terminal terminal) {
        tuiConsole.clearWithHeader(terminal);
        tuiConsole.printPageHeader(terminal, "◆ Command mode");

        terminal.writer().println("Enter a release-notes command.");
        terminal.writer().println("Type 'back' or press Esc to return to the main menu.");
        terminal.writer().println("Type 'exit' to close the application.");
        terminal.writer().println("Type 'clear' to clear the screen.");
        terminal.writer().println();
        terminal.writer().flush();
        terminal.flush();
    }

    private void executeCommand(String input, LineReader lineReader, Terminal terminal) {
        try {
            ParsedLine parsedLine = lineReader.getParser().parse(
                    input,
                    input.length(),
                    Parser.ParseContext.ACCEPT_LINE
            );

            String[] arguments = parsedLine.words().toArray(String[]::new);

            commandLineTerminalAdapter.execute(
                    terminal,
                    arguments
            );
        } catch (RuntimeException exception) {
            log.error("Command mode execution failed.", exception);

            terminal.writer().println(
                    "Error: " + exception.getMessage()
            );
            terminal.writer().flush();
        }
    }
}