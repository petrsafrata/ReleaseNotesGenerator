package cz.jpmad.releasenotes.cli;

import cz.jpmad.releasenotes.cli.ai.AiCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "release-notes",
        description = "Generate release notes from Git repository changes.",
        mixinStandardHelpOptions = true,
        version = "Release Notes Generator 0.1.0",
        subcommands = {
                GenerateCommand.class,
                AiCommand.class,
                CommandLine.HelpCommand.class
        }
)
public class RootCommand implements Runnable {

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec commandSpec;

    @Override
    public void run() {
        commandSpec.commandLine().usage(
                commandSpec.commandLine().getOut()
        );
    }
}