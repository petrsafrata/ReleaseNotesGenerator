package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.github.GitHubPullRequest;

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
            appendChange(markdown, change);
        }

        markdown.append("\n");
    }

    private void appendChange(StringBuilder markdown, ReleaseChange change) {
        markdown.append("- ").append(change.description());

        if (change.pullRequest().isPresent()) {
            appendPullRequest(markdown, change.pullRequest().orElseThrow());
        } else {
            appendCommit(markdown, change);
        }

        markdown.append("\n");
    }

    private void appendPullRequest(StringBuilder markdown, GitHubPullRequest pullRequest) {
        markdown.append(" ([#")
                .append(pullRequest.number())
                .append("](")
                .append(pullRequest.htmlUrl())
                .append("))");

        if (pullRequest.user() != null && pullRequest.user().login() != null) {
            markdown.append(" by ");

            if (pullRequest.user().htmlUrl() == null) {
                markdown.append("@")
                        .append(pullRequest.user().login());
                return;
            }

            markdown.append("[@")
                    .append(pullRequest.user().login())
                    .append("](")
                    .append(pullRequest.user().htmlUrl())
                    .append(")");
        }
    }

    private void appendCommit(StringBuilder markdown, ReleaseChange change
    ) {
        markdown.append(" (`")
                .append(change.sourceCommit().shortHash())
                .append("`)");
    }
}