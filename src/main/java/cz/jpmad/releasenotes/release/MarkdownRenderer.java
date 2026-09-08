package cz.jpmad.releasenotes.release;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MarkdownRenderer {

    public String render(ReleaseRange range, List<ReleaseChange> changes) {
        StringBuilder markdown = new StringBuilder();

        markdown.append("## ")
                .append(range.to())
                .append("\n\n");

        if (changes.isEmpty()) {
            markdown.append("No changes found.\n");
            return markdown.toString();
        }

        Map<ChangeCategory, List<ReleaseChange>> groupedChanges = groupByCategory(changes);

        for (ChangeCategory category : ChangeCategory.values()) {
            List<ReleaseChange> categoryChanges = groupedChanges.get(category);

            if (categoryChanges == null || categoryChanges.isEmpty()) {
                continue;
            }

            appendCategory(markdown, category, categoryChanges);
        }

        return markdown.toString();
    }

    private Map<ChangeCategory, List<ReleaseChange>> groupByCategory(List<ReleaseChange> changes) {
        return changes.stream()
                .collect(Collectors.groupingBy(
                        ReleaseChange::category,
                        () -> new EnumMap<>(ChangeCategory.class),
                        Collectors.toList()
                ));
    }

    private void appendCategory(StringBuilder markdown, ChangeCategory category, List<ReleaseChange> changes) {
        markdown.append("### ")
                .append(category.heading())
                .append("\n\n");

        for (ReleaseChange change : changes) {
            markdown.append("- ")
                    .append(change.description())
                    .append(" (`")
                    .append(change.sourceCommit().shortHash())
                    .append("`)\n");
        }

        markdown.append("\n");
    }
}
