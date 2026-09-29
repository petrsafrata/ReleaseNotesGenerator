package cz.jpmad.releasenotes.cli;

import cz.jpmad.releasenotes.ai.AiException;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationRequest;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationResult;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationService;
import cz.jpmad.releasenotes.git.GitCommandException;
import cz.jpmad.releasenotes.github.GitHubApiException;
import cz.jpmad.releasenotes.output.MarkdownOutputWriter;
import cz.jpmad.releasenotes.release.ReleaseRangeException;
import cz.jpmad.releasenotes.generation.ReleaseNotesGenerationProgress;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.function.Consumer;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Log4j2
@CommandLine.Command(
        name = "generate",
        description = "Generate release notes between two Git references.",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Callable<Integer> {

    private final ApplicationServicesFactory servicesFactory;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec commandSpec;

    @CommandLine.Option(
            names = "--from",
            description = "Previous release reference. "
                    + "Defaults to the tag preceding the target release."
    )
    private String from;

    @CommandLine.Option(
            names = "--to",
            description = "Target tagged reference.",
            defaultValue = "HEAD"
    )
    private String to;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Write release notes to a Markdown file."
    )
    private Path output;

    @CommandLine.Option(
            names = "--github",
            description = "Enrich release notes with data from GitHub API."
    )
    private boolean githubEnabled;

    @CommandLine.Option(
            names = "--ai",
            description = "Improve release notes using the local Ollama AI service."
    )
    private boolean aiEnabled;

    @CommandLine.Option(
            names = "--model",
            description = "Ollama model used to improve release notes.",
            defaultValue = "gemma3:4b"
    )
    private String model;

    @CommandLine.Option(
            names = "--language",
            description = "Language used for AI-generated release notes.",
            defaultValue = "en"
    )
    private String language;

    @CommandLine.Option(
            names = "--ollama-url",
            description = "Base URL of the Ollama API.",
            defaultValue = "http://localhost:11434"
    )
    private URI ollamaUrl;

    public GenerateCommand() {
        this(new ApplicationServicesFactory());
    }

    GenerateCommand(ApplicationServicesFactory servicesFactory) {
        this.servicesFactory = servicesFactory;
    }

    @Override
    public Integer call() {
        try {
            ReleaseNotesGenerationService generationService =
                    servicesFactory.createGenerationService(Path.of("."), ollamaUrl);

            ReleaseNotesGenerationRequest request = new ReleaseNotesGenerationRequest(
                    from,
                    to,
                    githubEnabled,
                    aiEnabled,
                    model,
                    language
            );

            ReleaseNotesGenerationResult result = generationService.generate(
                    request,
                    createProgressListener()
            );

            printGenerationDetails(result);
            writeOutput(result.markdown());
            printCompletionMessage();
            return 0;
        } catch (AiException | GitCommandException | GitHubApiException | OllamaException
                 | ReleaseRangeException | IOException exception
        ) {
            log.error("Release notes generation failed.", exception);
            commandSpec.commandLine().getErr().println("Error: " + exception.getMessage());
            return 1;
        }
    }

    private void printGenerationDetails(ReleaseNotesGenerationResult result) {
        result.githubRepositoryFullName().ifPresent(repositoryName -> {
            commandSpec.commandLine().getErr().println("GitHub repository loaded: " + repositoryName);
            commandSpec.commandLine().getErr().println(
                    "GitHub pull requests loaded: " + result.githubPullRequestCount()
            );
        });

        if (result.aiImproved()) {
            commandSpec.commandLine().getErr().println(
                    "Release notes improved using Ollama model: " + model
            );
        }
    }

    private void writeOutput(String markdown) throws IOException {
        if (output == null) {
            commandSpec.commandLine().getOut().print(markdown);
            return;
        }

        MarkdownOutputWriter markdownOutputWriter = new MarkdownOutputWriter();
        Path writtenFile = markdownOutputWriter.write(output, markdown);

        commandSpec.commandLine().getErr().println("Release notes written to: " + writtenFile);
    }

    private Consumer<ReleaseNotesGenerationProgress> createProgressListener() {
        return progress -> {
            PrintWriter outputWriter =
                    commandSpec.commandLine().getErr();

            outputWriter.printf(
                    "[%d/%d] %s%n",
                    progress.currentStep(),
                    progress.totalSteps(),
                    progress.message()
            );

            outputWriter.flush();
        };
    }

    private void printCompletionMessage() {
        PrintWriter outputWriter =
                commandSpec.commandLine().getErr();

        outputWriter.println(
                "Release notes generated successfully."
        );

        outputWriter.flush();
    }
}