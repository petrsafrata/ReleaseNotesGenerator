package cz.jpmad.releasenotes.tui;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class MainMenuScreen {

    private static final String MAIN_MENU = "main-menu";

    private static final String GENERATE = "generate";
    private static final String AI = "ai";
    private static final String COMMAND_MODE = "command-mode";
    private static final String EXIT = "exit";

    @NonNull
    private final TuiHeader tuiHeader;

    public MainMenuAction show(LineReader lineReader, Terminal terminal) throws IOException {
        ConsolePrompt consolePrompt = createConsolePrompt(lineReader, terminal);
        PromptBuilder promptBuilder = consolePrompt.getPromptBuilder();

        promptBuilder.createListPrompt()
                .name(MAIN_MENU)
                .message("What do you want to do?")
                .newItem(GENERATE)
                .text("Generate release notes")
                .add()
                .newItem(AI)
                .text("Manage Ollama AI")
                .add()
                .newItem(COMMAND_MODE)
                .text("Command mode")
                .add()
                .newItem(EXIT)
                .text("Exit")
                .add()
                .addPrompt();

        Map<String, PromptResultItemIF> result =
                consolePrompt.prompt(
                        tuiHeader.create(),
                        promptBuilder.build()
                );

        if (result == null || result.isEmpty()) {
            return MainMenuAction.EXIT;
        }

        return resolveAction(result.get(MAIN_MENU).getResult());
    }

    private ConsolePrompt createConsolePrompt(LineReader lineReader, Terminal terminal) {
        ConsolePrompt.UiConfig uiConfig =
                new ConsolePrompt.UiConfig(
                        ">",
                        "( )",
                        "(x)",
                        "( )"
                );

        uiConfig.setCancellableFirstPrompt(false);

        return new ConsolePrompt(lineReader, terminal, uiConfig);
    }

    private MainMenuAction resolveAction(String action) {
        return switch (action) {
            case GENERATE -> MainMenuAction.GENERATE;
            case AI -> MainMenuAction.AI;
            case COMMAND_MODE -> MainMenuAction.COMMAND_MODE;
            case EXIT -> MainMenuAction.EXIT;

            default -> throw new IllegalStateException("Unknown TUI menu item: " + action);
        };
    }
}