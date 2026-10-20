package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.config.ApplicationDefaults;
import picocli.CommandLine;

import java.net.URI;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "pull",
        description = "Download a model to Ollama.",
        mixinStandardHelpOptions = true
)
public class AiPullCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private AiCommand parentCommand;

    @CommandLine.Parameters(
            index = "0",
            paramLabel = "<model>",
            description = "Ollama model to download."
    )
    private String model;

    @CommandLine.Option(
            names = "--ollama-url",
            description = "Ollama API URL.",
            defaultValue = ApplicationDefaults.OLLAMA_URL
    )
    private URI ollamaUrl;

    @Override
    public Integer call() {
        try {
            OllamaApiClient client = parentCommand.createOllamaApiClient(ollamaUrl);
            parentCommand.out().println("Downloading Ollama model: " + model);
            parentCommand.out().println("This may take several minutes.");
            parentCommand.out().flush();

            client.pullModel(model);

            parentCommand.out().println("Ollama model downloaded successfully: " + model);
            return CommandLine.ExitCode.OK;
        } catch (OllamaException exception) {
            parentCommand.printError("Unable to download the Ollama model.", exception);
            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}