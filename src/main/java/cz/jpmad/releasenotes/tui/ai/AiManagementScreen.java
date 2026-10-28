package cz.jpmad.releasenotes.tui.ai;

import cz.jpmad.releasenotes.tui.TuiHeader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class AiManagementScreen {

    private static final String ACTION = "action";
    private static final String MODEL = "model";

    private static final String STATUS = "status";
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String MODELS = "models";
    private static final String PULL = "pull";
    private static final String BACK = "back";

    @NonNull
    private final LineReader lineReader;

    @NonNull
    private final Terminal terminal;

    @NonNull
    private final TuiHeader tuiHeader;

    public Optional<AiManagementAction> show() throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt();
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        promptBuilder.createListPrompt()
                .name(ACTION)
                .message("Manage Ollama AI:")
                .newItem(STATUS)
                .text("Show status")
                .add()
                .newItem(START)
                .text("Start Ollama")
                .add()
                .newItem(STOP)
                .text("Stop Ollama")
                .add()
                .newItem(MODELS)
                .text("List installed models")
                .add()
                .newItem(PULL)
                .text("Download a model")
                .add()
                .newItem(BACK)
                .text("Back to the main menu")
                .add()
                .addPrompt();

        Map<String, PromptResultItemIF> result =
                consolePrompt.prompt(
                        tuiHeader.create(),
                        promptBuilder.build()
                );

        if (result == null || result.isEmpty()) {
            return Optional.empty();
        }

        PromptResultItemIF resultItem =
                result.get(ACTION);

        if (resultItem == null) {
            return Optional.empty();
        }

        return Optional.of(resolveAction(resultItem.getResult()));
    }

    public Optional<String> promptModel() throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt();
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        promptBuilder.createInputPrompt()
                .name(MODEL)
                .message("Ollama model:")
                .defaultValue("gemma3:4b")
                .addPrompt();

        Map<String, PromptResultItemIF> result =
                consolePrompt.prompt(
                        tuiHeader.create(),
                        promptBuilder.build()
                );

        if (result == null || result.isEmpty()) {
            return Optional.empty();
        }

        PromptResultItemIF resultItem = result.get(MODEL);

        if (resultItem == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(resultItem.getResult());
    }

    private AiManagementAction resolveAction(String action) {
        return switch (action) {
            case STATUS -> AiManagementAction.STATUS;
            case START -> AiManagementAction.START;
            case STOP -> AiManagementAction.STOP;
            case MODELS -> AiManagementAction.MODELS;
            case PULL -> AiManagementAction.PULL;
            case BACK -> AiManagementAction.BACK;
            default -> throw new IllegalStateException("Unknown Ollama AI action: " + action);
        };
    }

    private ConsolePrompt createConsolePrompt() {
        ConsolePrompt.UiConfig uiConfig =
                new ConsolePrompt.UiConfig(
                        ">",
                        "( )",
                        "(x)",
                        "( )"
                );

        uiConfig.setCancellableFirstPrompt(true);

        return new ConsolePrompt(lineReader, terminal, uiConfig);
    }
}