package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine;

import java.io.PrintWriter;

@Log4j2
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

    private final ApplicationServicesFactory servicesFactory;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec commandSpec;

    public AiCommand() {
        this(new ApplicationServicesFactory());
    }

    AiCommand(ApplicationServicesFactory servicesFactory) {
        this.servicesFactory = servicesFactory;
    }

    @Override
    public void run() {
        commandSpec.commandLine().usage(out());
    }

    ApplicationServicesFactory servicesFactory() {
        return servicesFactory;
    }

    PrintWriter out() {
        return commandSpec.commandLine().getOut();
    }

    PrintWriter err() {
        return commandSpec.commandLine().getErr();
    }

    void printError(Exception exception) {
        log.error("AI command failed.", exception);
        err().println("Error: " + exception.getMessage());
    }
}