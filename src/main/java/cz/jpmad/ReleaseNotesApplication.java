package cz.jpmad;


import cz.jpmad.releasenotes.cli.RootCommand;
import picocli.CommandLine;

public class ReleaseNotesApplication {

    private ReleaseNotesApplication() {}

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RootCommand()).execute(args);
        System.exit(exitCode);
    }
}