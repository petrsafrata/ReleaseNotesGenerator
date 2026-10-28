package cz.jpmad.releasenotes.tui;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import picocli.CommandLine;

import java.io.PrintWriter;

@RequiredArgsConstructor
public class CommandLineTerminalAdapter {

    @NonNull
    private final CommandLine commandLine;

    public void configure(Terminal terminal) {
        PrintWriter writer = terminal.writer();
        configure(commandLine, writer);
    }

    public int execute(Terminal terminal, String... arguments) {
        try {
            return commandLine.execute(arguments);
        } finally {
            flush(terminal);
        }
    }

    public void flush(Terminal terminal) {
        flush(commandLine);
        terminal.writer().flush();
        terminal.flush();
    }

    private void configure(CommandLine currentCommandLine, PrintWriter writer) {
        currentCommandLine.setOut(writer);
        currentCommandLine.setErr(writer);

        for (CommandLine subcommand : currentCommandLine.getSubcommands().values()) {
            configure(subcommand, writer);
        }
    }

    private void flush(CommandLine currentCommandLine) {
        currentCommandLine.getOut().flush();
        currentCommandLine.getErr().flush();

        for (CommandLine subcommand : currentCommandLine.getSubcommands().values()) {
            flush(subcommand);
        }
    }
}