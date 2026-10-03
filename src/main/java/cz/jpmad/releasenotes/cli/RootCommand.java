package cz.jpmad.releasenotes.cli;

import cz.jpmad.releasenotes.cli.ai.AiCommand;
import picocli.CommandLine;
import cz.jpmad.releasenotes.config.ApplicationVersionProvider;

@CommandLine.Command(
        name = "release-notes",
        description = "Generate release notes from Git repository changes.",
        mixinStandardHelpOptions = true,
        versionProvider = ApplicationVersionProvider.class,
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