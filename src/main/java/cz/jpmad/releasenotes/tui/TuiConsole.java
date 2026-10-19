package cz.jpmad.releasenotes.tui;

import lombok.RequiredArgsConstructor;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

@RequiredArgsConstructor
public class TuiConsole {

    private final TuiHeader tuiHeader;

    public void clear(Terminal terminal) {
        terminal.puts(InfoCmp.Capability.clear_screen);

        terminal.writer().flush();
        terminal.flush();
    }

    public void printHeader(Terminal terminal) {
        tuiHeader.print(terminal);
    }

    public void clearWithHeader(Terminal terminal) {
        clear(terminal);
        printHeader(terminal);
    }

    public void printPageHeader(Terminal terminal, String title) {
        printPageHeader(terminal, title, null);
    }

    public void printPageHeader(Terminal terminal, String title, String description) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT
                        .bold()
                        .foreground(AttributedStyle.CYAN))
                .append(title)
                .style(AttributedStyle.DEFAULT)
                .toAnsi(terminal)
        );

        if (description != null && !description.isBlank()) {
            terminal.writer().println(new AttributedStringBuilder()
                    .style(
                            AttributedStyle.DEFAULT.faint()
                    )
                    .append(description)
                    .style(AttributedStyle.DEFAULT)
                    .toAnsi(terminal)
            );
        }

        terminal.writer().println();
        terminal.writer().flush();
    }

    public void printGenerationResult(Terminal terminal, int exitCode) {
        terminal.writer().println();

        if (exitCode == 0) {
            printStatus(terminal, "✓ Generation completed successfully", AttributedStyle.GREEN);
        } else {
            printStatus(terminal, "✗ Generation failed", AttributedStyle.RED);
        }

        terminal.writer().flush();
    }

    public boolean waitForReturn(Terminal terminal, LineReader lineReader) {
        terminal.writer().println();
        terminal.writer().flush();

        try {
            lineReader.readLine(new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT
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

    public String createCommandPrompt(Terminal terminal) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                .append("release-notes")
                .style(AttributedStyle.DEFAULT)
                .append("> ")
                .toAnsi(terminal);
    }

    public void printGoodbye(Terminal terminal) {
        terminal.writer().println();

        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT
                        .bold()
                        .foreground(AttributedStyle.CYAN)
                )
                .append("Thank you for using Release Notes Generator!")
                .style(AttributedStyle.DEFAULT)
                .toAnsi(terminal)
        );

        terminal.writer().println();
        terminal.writer().flush();
    }

    private void printStatus(Terminal terminal, String message, int color
    ) {
        terminal.writer().println(new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT
                        .bold()
                        .foreground(color)
                )
                .append(message)
                .style(AttributedStyle.DEFAULT)
                .toAnsi(terminal)
        );
    }
}