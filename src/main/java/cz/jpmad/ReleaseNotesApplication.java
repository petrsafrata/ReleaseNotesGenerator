package cz.jpmad;

import cz.jpmad.releasenotes.cli.RootCommand;
import cz.jpmad.releasenotes.tui.ReleaseNotesTui;
import picocli.CommandLine;

public class ReleaseNotesApplication {

    private ReleaseNotesApplication() {}

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(
                new RootCommand()
        );

        int exitCode;

        if (args.length == 0) {
            exitCode = new ReleaseNotesTui(commandLine).run();
        } else {
            exitCode = commandLine.execute(args);
        }

        System.exit(exitCode);
    }
}