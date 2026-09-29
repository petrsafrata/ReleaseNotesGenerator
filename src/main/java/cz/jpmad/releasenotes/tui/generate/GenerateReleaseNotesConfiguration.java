package cz.jpmad.releasenotes.tui.generate;

import java.util.ArrayList;
import java.util.List;

public record GenerateReleaseNotesConfiguration(
        String from,
        String to,
        boolean githubEnabled,
        boolean aiEnabled,
        String model,
        String language,
        String ollamaUrl,
        String output
) {

    public List<String> toArguments() {
        List<String> arguments = new ArrayList<>();

        arguments.add("generate");

        if (from != null && !from.isBlank()) {
            arguments.add("--from");
            arguments.add(from);
        }

        if (to != null && !to.isBlank() && !"HEAD".equals(to)) {
            arguments.add("--to");
            arguments.add(to);
        }

        if (githubEnabled) {
            arguments.add("--github");
        }

        if (aiEnabled) {
            arguments.add("--ai");
            arguments.add("--model");
            arguments.add(model);
            arguments.add("--language");
            arguments.add(language);
            arguments.add("--ollama-url");
            arguments.add(ollamaUrl);
        }

        if (output != null && !output.isBlank()) {
            arguments.add("--output");
            arguments.add(output);
        }

        return List.copyOf(arguments);
    }

    public String rangeDescription() {
        String resolvedFrom = from == null || from.isBlank()
                ? "previous tag"
                : from;

        String resolvedTo = to == null || to.isBlank()
                ? "HEAD"
                : to;

        return resolvedFrom + " -> " + resolvedTo;
    }

    public String outputDescription() {
        if (output == null || output.isBlank()) {
            return "Terminal";
        }

        return output;
    }
}