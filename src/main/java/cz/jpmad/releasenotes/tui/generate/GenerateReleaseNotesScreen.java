package cz.jpmad.releasenotes.tui.generate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.ListPromptBuilder;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import cz.jpmad.releasenotes.ai.ollama.OllamaModel;
import cz.jpmad.releasenotes.config.ApplicationServicesFactory;
import cz.jpmad.releasenotes.ai.ollama.OllamaException;
import cz.jpmad.releasenotes.docker.DockerOllamaService;
import cz.jpmad.releasenotes.docker.DockerOllamaState;
import cz.jpmad.releasenotes.tui.TuiHeader;

import java.net.URI;
import java.util.Comparator;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Path;

@RequiredArgsConstructor
public class GenerateReleaseNotesScreen {

    private static final String RANGE = "range";
    private static final String AUTOMATIC_RANGE = "automatic";
    private static final String CUSTOM_RANGE = "custom";

    private static final String FROM = "from";
    private static final String TO = "to";

    private static final String GITHUB = "github";
    private static final String AI = "ai";

    private static final String ENABLED = "enabled";
    private static final String DISABLED = "disabled";

    private static final String MODEL = "model";
    private static final String LANGUAGE = "language";
    private static final String OLLAMA_URL = "ollama-url";

    private static final String OUTPUT = "output";
    private static final String TERMINAL_OUTPUT = "terminal";
    private static final String DEFAULT_FILE = "default-file";
    private static final String CUSTOM_FILE = "custom-file";
    private static final String OUTPUT_PATH = "output-path";

    private static final String CONFIRMATION = "confirmation";
    private static final String GENERATE = "generate";
    private static final String BACK = "back";

    private static final String CUSTOM_REFERENCE = "custom-reference";
    private static final String HEAD_REFERENCE = "head-reference";
    private static final String TAG_REFERENCE_PREFIX = "tag-reference:";

    private static final String OLLAMA_RECOVERY = "ollama-recovery";
    private static final String START_OLLAMA = "start-ollama";
    private static final String CONTINUE_WITHOUT_AI = "continue-without-ai";

    private static final int OLLAMA_STARTUP_ATTEMPTS = 10;
    private static final long OLLAMA_STARTUP_DELAY_MILLIS = 500;

    @NonNull
    private final LineReader lineReader;

    @NonNull
    private final Terminal terminal;

    @NonNull
    private final ApplicationServicesFactory servicesFactory;

    @NonNull
    private final TuiHeader tuiHeader;

    public Optional<GenerateReleaseNotesConfiguration> show() throws IOException {
        Optional<String> rangeResult = promptList(
                RANGE,
                "Select the release range:",
                new PromptOption(
                        AUTOMATIC_RANGE,
                        "Previous tag -> HEAD"
                ),
                new PromptOption(
                        CUSTOM_RANGE,
                        "Custom Git references"
                )
        );

        if (rangeResult.isEmpty()) {
            return Optional.empty();
        }

        String from = null;
        String to = "HEAD";

        if (CUSTOM_RANGE.equals(rangeResult.get())) {
            List<String> tags = loadGitTags();

            Optional<String> fromResult = promptGitReference(
                    FROM,
                    "Select the previous release reference:",
                    tags,
                    false
            );

            if (fromResult.isEmpty()) {
                return Optional.empty();
            }

            Optional<String> toResult = promptGitReference(
                    TO,
                    "Select the target release reference:",
                    tags,
                    true
            );

            if (toResult.isEmpty()) {
                return Optional.empty();
            }

            from = emptyToNull(fromResult.get());
            to = toResult.get();
        }

        Optional<String> githubResult = promptList(
                GITHUB,
                "Use GitHub enrichment?",
                new PromptOption(
                        DISABLED,
                        "No"
                ),
                new PromptOption(
                        ENABLED,
                        "Yes"
                )
        );

        if (githubResult.isEmpty()) {
            return Optional.empty();
        }

        boolean githubEnabled = ENABLED.equals(
                githubResult.get()
        );

        Optional<String> aiResult = promptList(
                AI,
                "Improve release notes using AI?",
                new PromptOption(
                        DISABLED,
                        "No"
                ),
                new PromptOption(
                        ENABLED,
                        "Yes"
                )
        );

        if (aiResult.isEmpty()) {
            return Optional.empty();
        }

        boolean aiEnabled = ENABLED.equals(
                aiResult.get()
        );

        String model = null;
        String language = null;
        String ollamaUrl = null;

        if (aiEnabled) {
            Optional<String> ollamaUrlResult = promptInput(
                    OLLAMA_URL,
                    "Ollama API URL:",
                    "http://localhost:11434"
            );

            if (ollamaUrlResult.isEmpty()) {
                return Optional.empty();
            }

            ollamaUrl = ollamaUrlResult.get();
            URI ollamaUri = URI.create(ollamaUrl);

            List<OllamaModel> installedModels;

            try {
                installedModels = loadInstalledModels(ollamaUri);
            } catch (OllamaException exception) {
                if (!isLocalOllama(ollamaUri)) {
                    throw exception;
                }

                DockerOllamaService dockerOllamaService =
                        servicesFactory.createDockerOllamaService();

                DockerOllamaState state =
                        dockerOllamaService.status();

                if (state == DockerOllamaState.RUNNING) {
                    throw exception;
                }

                Optional<String> recoveryResult =
                        promptOllamaRecovery(state);

                if (recoveryResult.isEmpty()
                        || BACK.equals(recoveryResult.get())) {
                    return Optional.empty();
                }

                if (CONTINUE_WITHOUT_AI.equals(
                        recoveryResult.get()
                )) {
                    aiEnabled = false;
                    ollamaUrl = null;
                    installedModels = List.of();
                } else {
                    dockerOllamaService.start();

                    installedModels =
                            waitForInstalledModels(ollamaUri);
                }
            }

            if (aiEnabled) {
                if (installedModels.isEmpty()) {
                    throw new OllamaException(
                            "No Ollama models are installed. "
                                    + "Download one from Manage Ollama AI."
                    );
                }

                Optional<String> modelResult =
                        promptModel(installedModels);

                if (modelResult.isEmpty()) {
                    return Optional.empty();
                }

                Optional<String> languageResult = promptList(
                        LANGUAGE,
                        "AI output language:",
                        new PromptOption(
                                "en",
                                "English"
                        ),
                        new PromptOption(
                                "cs",
                                "Czech"
                        )
                );

                if (languageResult.isEmpty()) {
                    return Optional.empty();
                }

                model = modelResult.get();
                language = languageResult.get();
            }
        }

        Optional<String> outputResult = promptList(
                OUTPUT,
                "Select the output:",
                new PromptOption(
                        TERMINAL_OUTPUT,
                        "Show in terminal"
                ),
                new PromptOption(
                        DEFAULT_FILE,
                        "Write to RELEASE_NOTES.md"
                ),
                new PromptOption(
                        CUSTOM_FILE,
                        "Write to another file"
                )
        );

        if (outputResult.isEmpty()) {
            return Optional.empty();
        }

        String output = resolveOutput(
                outputResult.get()
        );

        if (CUSTOM_FILE.equals(outputResult.get())) {
            Optional<String> outputPathResult = promptInput(
                    OUTPUT_PATH,
                    "Markdown output file:",
                    "RELEASE_NOTES.md"
            );

            if (outputPathResult.isEmpty()) {
                return Optional.empty();
            }

            output = emptyToNull(
                    outputPathResult.get()
            );
        }

        GenerateReleaseNotesConfiguration configuration =
                new GenerateReleaseNotesConfiguration(
                        from,
                        to,
                        githubEnabled,
                        aiEnabled,
                        model,
                        language,
                        ollamaUrl,
                        output
                );

        if (!confirm(configuration)) {
            return Optional.empty();
        }

        return Optional.of(configuration);
    }

    private Optional<String> promptList(String name,
                                        String message,
                                        PromptOption... options)
            throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt();
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        ListPromptBuilder listPromptBuilder =
                promptBuilder.createListPrompt()
                        .name(name)
                        .message(message);

        for (PromptOption option : options) {
            listPromptBuilder.newItem(option.name())
                    .text(option.text())
                    .add();
        }

        listPromptBuilder.addPrompt();

        return prompt(
                consolePrompt,
                promptBuilder,
                name
        );
    }

    private Optional<String> promptInput(String name,
                                         String message,
                                         String defaultValue)
            throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt();
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        if (defaultValue == null) {
            promptBuilder.createInputPrompt()
                    .name(name)
                    .message(message)
                    .addPrompt();
        } else {
            promptBuilder.createInputPrompt()
                    .name(name)
                    .message(message)
                    .defaultValue(defaultValue)
                    .addPrompt();
        }

        return prompt(
                consolePrompt,
                promptBuilder,
                name
        );
    }

    private Optional<String> prompt(ConsolePrompt consolePrompt,
                                    PromptBuilder promptBuilder,
                                    String resultName)
            throws IOException {
        Map<String, PromptResultItemIF> result =
                consolePrompt.prompt(
                        tuiHeader.create(),
                        promptBuilder.build()
                );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        PromptResultItemIF resultItem = result.get(resultName);

        if (resultItem == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                resultItem.getResult()
        );
    }

    private boolean confirm(GenerateReleaseNotesConfiguration configuration)
            throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt();
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        promptBuilder.createListPrompt()
                .name(CONFIRMATION)
                .message("Continue?")
                .newItem(GENERATE)
                .text("Generate release notes")
                .add()
                .newItem(BACK)
                .text("Back to the main menu")
                .add()
                .addPrompt();

        List<AttributedString> header = tuiHeader.create();

        header.add(
                new AttributedString("Release Notes Configuration", AttributedStyle.DEFAULT
                        .bold()
                        .foreground(AttributedStyle.CYAN)
                )
        );
        header.add(new AttributedString(""));
        header.add(new AttributedString(
                        "Range: " + configuration.rangeDescription()
                )
        );
        header.add(new AttributedString(
                        "GitHub: " + enabledLabel(
                                configuration.githubEnabled()
                        )
                )
        );
        header.add(new AttributedString(
                        "AI: " + enabledLabel(
                                configuration.aiEnabled()
                        )
                )
        );
        header.add(new AttributedString(
                        "Model: " + valueOrDisabled(
                                configuration.aiEnabled(),
                                configuration.model()
                        )
                )
        );
        header.add(new AttributedString(
                        "Language: " + valueOrDisabled(
                                configuration.aiEnabled(),
                                configuration.language()
                        )
                )
        );
        header.add(
                new AttributedString(
                        "Output: " + configuration.outputDescription()
                )
        );
        header.add(new AttributedString(""));
        Map<String, PromptResultItemIF> result =
                consolePrompt.prompt(
                        header,
                        promptBuilder.build()
                );

        return !result.isEmpty()
                && GENERATE.equals(
                result.get(CONFIRMATION).getResult()
        );
    }

    private ConsolePrompt createConsolePrompt() {
        ConsolePrompt.UiConfig uiConfig = new ConsolePrompt.UiConfig(
                ">",
                "( )",
                "(x)",
                "( )"
        );

        uiConfig.setCancellableFirstPrompt(true);

        return new ConsolePrompt(
                lineReader,
                terminal,
                uiConfig
        );
    }

    private String resolveOutput(String selectedOutput) {
        return switch (selectedOutput) {
            case TERMINAL_OUTPUT -> null;
            case DEFAULT_FILE -> "RELEASE_NOTES.md";
            case CUSTOM_FILE -> null;
            default -> throw new IllegalStateException(
                    "Unknown output option: " + selectedOutput
            );
        };
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }

    private String enabledLabel(boolean enabled) {
        return enabled ? "Enabled" : "Disabled";
    }

    private String valueOrDisabled(boolean enabled, String value) {
        return enabled ? value : "Disabled";
    }

    private List<OllamaModel> loadInstalledModels(URI ollamaUri) {
        return servicesFactory.createOllamaApiClient(
                        ollamaUri
                )
                .getModels()
                .stream()
                .sorted(
                        Comparator.comparing(
                                OllamaModel::name,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }

    private Optional<String> promptModel(List<OllamaModel> models)
            throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt();
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        ListPromptBuilder listPromptBuilder =
                promptBuilder.createListPrompt()
                        .name(MODEL)
                        .message("Select an Ollama model:");

        for (OllamaModel model : models) {
            listPromptBuilder.newItem(model.name())
                    .text(modelDescription(model))
                    .add();
        }

        listPromptBuilder.addPrompt();

        return prompt(
                consolePrompt,
                promptBuilder,
                MODEL
        );
    }

    private String modelDescription(OllamaModel model) {
        if (model.details() == null) {
            return model.name();
        }

        String parameterSize =
                model.details().parameterSize();

        String quantization =
                model.details().quantizationLevel();

        if (hasValue(parameterSize) && hasValue(quantization)) {
            return model.name()
                    + " ("
                    + parameterSize
                    + ", "
                    + quantization
                    + ")";
        }

        if (hasValue(parameterSize)) {
            return model.name()
                    + " ("
                    + parameterSize
                    + ")";
        }

        if (hasValue(quantization)) {
            return model.name()
                    + " ("
                    + quantization
                    + ")";
        }

        return model.name();
    }

    private List<String> loadGitTags() {
        return servicesFactory.createGitTagService(
                Path.of(".")
        ).findTags();
    }

    private Optional<String> promptGitReference(
            String name,
            String message,
            List<String> tags,
            boolean includeHead
    ) throws IOException {
        if (tags.isEmpty() && !includeHead) {
            return promptInput(
                    name,
                    "Enter the Git reference:",
                    null
            );
        }

        ConsolePrompt consolePrompt = createConsolePrompt();
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        ListPromptBuilder listPromptBuilder =
                promptBuilder.createListPrompt()
                        .name(name)
                        .message(message);

        if (includeHead) {
            listPromptBuilder.newItem(HEAD_REFERENCE)
                    .text("HEAD")
                    .add();
        }

        for (String tag : tags) {
            listPromptBuilder.newItem(
                            TAG_REFERENCE_PREFIX + tag
                    )
                    .text(tag)
                    .add();
        }

        listPromptBuilder.newItem(CUSTOM_REFERENCE)
                .text("Enter another Git reference")
                .add();

        listPromptBuilder.addPrompt();

        Optional<String> selectedReference = prompt(
                consolePrompt,
                promptBuilder,
                name
        );

        if (selectedReference.isEmpty()) {
            return Optional.empty();
        }

        if (CUSTOM_REFERENCE.equals(selectedReference.get())) {
            return promptInput(
                    name,
                    "Enter the Git reference:",
                    null
            );
        }

        if (HEAD_REFERENCE.equals(selectedReference.get())) {
            return Optional.of("HEAD");
        }

        if (selectedReference.get().startsWith(TAG_REFERENCE_PREFIX)) {
            return Optional.of(
                    selectedReference.get().substring(
                            TAG_REFERENCE_PREFIX.length()
                    )
            );
        }

        throw new IllegalStateException(
                "Unknown Git reference selection: "
                        + selectedReference.get()
        );
    }

    private Optional<String> promptOllamaRecovery(
            DockerOllamaState state
    ) throws IOException {
        String message = switch (state) {
            case NOT_CREATED -> "Ollama container has not been created:";
            case STOPPED -> "Ollama container is stopped:";
            case RUNNING -> "Ollama container is running:";
        };

        return promptList(
                OLLAMA_RECOVERY,
                message,
                new PromptOption(
                        START_OLLAMA,
                        "Start Ollama"
                ),
                new PromptOption(
                        CONTINUE_WITHOUT_AI,
                        "Continue without AI"
                ),
                new PromptOption(
                        BACK,
                        "Back to the main menu"
                )
        );
    }

    private boolean isLocalOllama(URI ollamaUri) {
        String host = ollamaUri.getHost();

        if (host == null) {
            return false;
        }

        return host.equalsIgnoreCase("localhost")
                || host.equals("127.0.0.1")
                || host.equals("::1");
    }

    private List<OllamaModel> waitForInstalledModels(
            URI ollamaUri
    ) {
        OllamaException lastException = null;

        for (int attempt = 0;
             attempt < OLLAMA_STARTUP_ATTEMPTS;
             attempt++) {
            try {
                return loadInstalledModels(ollamaUri);
            } catch (OllamaException exception) {
                lastException = exception;

                if (attempt + 1
                        < OLLAMA_STARTUP_ATTEMPTS) {
                    waitBeforeOllamaRetry();
                }
            }
        }

        throw new OllamaException(
                "Ollama container was started, "
                        + "but its API did not become available.",
                lastException
        );
    }

    private void waitBeforeOllamaRetry() {
        try {
            Thread.sleep(
                    OLLAMA_STARTUP_DELAY_MILLIS
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new OllamaException(
                    "Waiting for the Ollama API was interrupted.",
                    exception
            );
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private record PromptOption(
            String name,
            String text
    ) {
    }
}