package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.ai.ollama.OllamaModel;
import cz.jpmad.releasenotes.config.ApplicationDefaults;
import picocli.CommandLine;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "models",
        description = "List models installed in Ollama.",
        mixinStandardHelpOptions = true
)
public class AiModelsCommand implements Callable<Integer> {

    @CommandLine.ParentCommand
    private AiCommand parentCommand;

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
            List<OllamaModel> models = client.getModels();

            if (models.isEmpty()) {
                parentCommand.out().println("No Ollama models are installed.");
                parentCommand.out().println("Install one using: release-notes ai pull <model>");
                return CommandLine.ExitCode.OK;
            }

            parentCommand.out().println("Installed Ollama models:");

            for (OllamaModel model : models) {
                printModel(model);
            }
            return CommandLine.ExitCode.OK;
        } catch (OllamaException exception) {
            parentCommand.printError("Unable to load installed Ollama models.", exception);
            parentCommand.err().println("Make sure Ollama is running or specify --ollama-url.");
            return CommandLine.ExitCode.SOFTWARE;
        }
    }

    private void printModel(OllamaModel model) {
        parentCommand.out().println();
        parentCommand.out().println("Name: " + model.name());
        parentCommand.out().println("Size: " + formatSize(model.size()));

        if (model.details() == null) {
            return;
        }

        parentCommand.out().println("Parameters: " + valueOrUnknown(model.details().parameterSize()));
        parentCommand.out().println("Quantization: " + valueOrUnknown(model.details().quantizationLevel()));
    }

    private String formatSize(long bytes) {
        double gigabytes = bytes / 1024.0 / 1024.0 / 1024.0;

        if (gigabytes >= 1) {
            return "%.2f GB".formatted(gigabytes);
        }

        double megabytes = bytes / 1024.0 / 1024.0;
        return "%.2f MB".formatted(megabytes);
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}