package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
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
            defaultValue = "http://localhost:11434"
    )
    private URI ollamaUrl;

    @Override
    public Integer call() {
        try {
            OllamaApiClient client = parentCommand.servicesFactory().createOllamaApiClient(ollamaUrl);

            parentCommand.out().println("Downloading Ollama model: " + model);
            parentCommand.out().println("This may take several minutes.");
            parentCommand.out().flush();

            client.pullModel(model);

            parentCommand.out().println("Ollama model downloaded successfully: " + model);
            return 0;
        } catch (OllamaException exception) {
            parentCommand.printError(exception);
            return 1;
        }
    }
}