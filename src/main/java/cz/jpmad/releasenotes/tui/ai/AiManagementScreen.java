package cz.jpmad.releasenotes.tui.ai;

import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class AiManagementScreen {

    private static final String ACTION = "action";
    private static final String MODEL = "model";

    private static final String STATUS = "status";
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String MODELS = "models";
    private static final String PULL = "pull";
    private static final String BACK = "back";

    private final LineReader lineReader;
    private final Terminal terminal;

    public AiManagementScreen(LineReader lineReader,
                              Terminal terminal) {
        this.lineReader = lineReader;
        this.terminal = terminal;
    }

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
                        promptBuilder.build()
                );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                resolveAction(
                        result.get(ACTION).getResult()
                )
        );
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
                        promptBuilder.build()
                );

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                result.get(MODEL).getResult()
        );
    }

    private AiManagementAction resolveAction(String action) {
        return switch (action) {
            case STATUS -> AiManagementAction.STATUS;
            case START -> AiManagementAction.START;
            case STOP -> AiManagementAction.STOP;
            case MODELS -> AiManagementAction.MODELS;
            case PULL -> AiManagementAction.PULL;
            case BACK -> AiManagementAction.BACK;
            default -> throw new IllegalStateException(
                    "Unknown Ollama AI action: " + action
            );
        };
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
}