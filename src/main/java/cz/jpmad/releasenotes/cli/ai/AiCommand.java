package cz.jpmad.releasenotes.cli.ai;

import picocli.CommandLine;

@CommandLine.Command(
        name = "ai",
        description = "Manage the local Ollama AI service.",
        mixinStandardHelpOptions = true,
        subcommands = {
                AiStartCommand.class,
                AiStatusCommand.class,
                AiStopCommand.class,
                AiModelsCommand.class,
                AiPullCommand.class
        }
)
public class AiCommand implements Runnable {

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}