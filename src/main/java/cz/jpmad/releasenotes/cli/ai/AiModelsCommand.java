package cz.jpmad.releasenotes.cli.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jpmad.releasenotes.ai.ollama.*;
import picocli.CommandLine;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "models",
        description = "List models installed in Ollama.",
        mixinStandardHelpOptions = true
)
public class AiModelsCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = "--ollama-url",
            description = "Ollama API URL.",
            defaultValue = "http://localhost:11434"
    )
    private String ollamaUrl;

    @Override
    public Integer call() {
        try {
            OllamaApiClient client = createClient();
            List<OllamaModel> models = client.getModels();

            if (models.isEmpty()) {
                System.out.println("No Ollama models are installed.");
                System.out.println("Install one using: release-notes ai pull <model>");
                return 0;
            }

            System.out.println("Installed Ollama models:");

            for (OllamaModel model : models) {
                System.out.println();
                System.out.println("Name: " + model.name());
                System.out.println("Size: " + formatSize(model.size()));

                if (model.details() != null) {
                    System.out.println("Parameters: " + valueOrUnknown(model.details().parameterSize()));
                    System.out.println("Quantization: " + valueOrUnknown(model.details().quantizationLevel()));
                }
            }

            return 0;
        } catch (IllegalArgumentException exception) {
            System.err.println("Error: Invalid Ollama URL: " + ollamaUrl);
            return 1;
        } catch (OllamaException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.err.println("Make sure Ollama is running or specify --ollama-url.");
            return 1;
        }
    }

    private OllamaApiClient createClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        return new OllamaApiClient(
                URI.create(ollamaUrl),
                new JavaOllamaHttpClient(httpClient),
                new OllamaRequestSerializer(new ObjectMapper()),
                new OllamaResponseParser(new ObjectMapper())
        );
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