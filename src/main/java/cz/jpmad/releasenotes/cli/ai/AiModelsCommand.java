package cz.jpmad.releasenotes.cli.ai;

import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.ai.ollama.OllamaModel;
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
            defaultValue = "http://localhost:11434"
    )
    private URI ollamaUrl;

    @Override
    public Integer call() {
        try {
            OllamaApiClient client = parentCommand.servicesFactory().createOllamaApiClient(ollamaUrl);
            List<OllamaModel> models = client.getModels();

            if (models.isEmpty()) {
                parentCommand.out().println("No Ollama models are installed.");
                parentCommand.out().println("Install one using: release-notes ai pull <model>");
                return 0;
            }

            parentCommand.out().println("Installed Ollama models:");

            for (OllamaModel model : models) {
                parentCommand.out().println();
                parentCommand.out().println("Name: " + model.name());
                parentCommand.out().println("Size: " + formatSize(model.size()));

                if (model.details() != null) {
                    parentCommand.out().println(
                            "Parameters: " + valueOrUnknown(model.details().parameterSize())
                    );
                    parentCommand.out().println(
                            "Quantization: " + valueOrUnknown(model.details().quantizationLevel())
                    );
                }
            }

            return 0;
        } catch (OllamaException exception) {
            parentCommand.printError(exception);
            parentCommand.err().println("Make sure Ollama is running or specify --ollama-url.");
            return 1;
        }
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
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value;
    }
}