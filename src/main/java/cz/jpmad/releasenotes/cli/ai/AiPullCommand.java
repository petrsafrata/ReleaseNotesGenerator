package cz.jpmad.releasenotes.cli.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.jpmad.releasenotes.ai.ollama.JavaOllamaHttpClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaApiClient;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.ai.ollama.OllamaRequestSerializer;
import cz.jpmad.releasenotes.ai.ollama.OllamaResponseParser;
import picocli.CommandLine;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "pull",
        description = "Download a model to Ollama.",
        mixinStandardHelpOptions = true
)
public class AiPullCommand implements Callable<Integer> {

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
    private String ollamaUrl;

    @Override
    public Integer call() {
        try {
            OllamaApiClient client = createClient();

            System.out.println("Downloading Ollama model: " + model);
            System.out.println("This may take several minutes.");
            System.out.flush();

            client.pullModel(model);

            System.out.println("Ollama model downloaded successfully: " + model);
            return 0;
        } catch (IllegalArgumentException exception) {
            System.err.println("Error: Invalid Ollama URL: " + ollamaUrl);
            return 1;
        } catch (OllamaException exception) {
            System.err.println("Error: " + exception.getMessage());
            return 1;
        }
    }

    private OllamaApiClient createClient() {
        ObjectMapper objectMapper = new ObjectMapper();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        return new OllamaApiClient(
                URI.create(ollamaUrl),
                new JavaOllamaHttpClient(httpClient),
                new OllamaRequestSerializer(objectMapper),
                new OllamaResponseParser(objectMapper)
        );
    }
}