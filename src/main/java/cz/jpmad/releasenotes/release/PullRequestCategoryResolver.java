package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.github.GitHubLabel;
import cz.jpmad.releasenotes.github.GitHubPullRequest;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PullRequestCategoryResolver {

    private static final Map<String, ChangeCategory> LABEL_CATEGORIES = Map.ofEntries(
            Map.entry("feature", ChangeCategory.FEATURES),
            Map.entry("enhancement", ChangeCategory.FEATURES),
            Map.entry("bug", ChangeCategory.BUG_FIXES),
            Map.entry("fix", ChangeCategory.BUG_FIXES),
            Map.entry("documentation", ChangeCategory.DOCUMENTATION),
            Map.entry("docs", ChangeCategory.DOCUMENTATION),
            Map.entry("refactoring", ChangeCategory.REFACTORING),
            Map.entry("refactor", ChangeCategory.REFACTORING),
            Map.entry("tests", ChangeCategory.TESTS),
            Map.entry("test", ChangeCategory.TESTS),
            Map.entry("performance", ChangeCategory.PERFORMANCE),
            Map.entry("dependencies", ChangeCategory.BUILD_SYSTEM),
            Map.entry("build", ChangeCategory.BUILD_SYSTEM),
            Map.entry("ci", ChangeCategory.CI_CD)
    );

    private static final List<ChangeCategory> CATEGORY_PRIORITY = List.of(
            ChangeCategory.FEATURES,
            ChangeCategory.BUG_FIXES,
            ChangeCategory.PERFORMANCE,
            ChangeCategory.DOCUMENTATION,
            ChangeCategory.REFACTORING,
            ChangeCategory.BUILD_SYSTEM,
            ChangeCategory.CI_CD,
            ChangeCategory.TESTS,
            ChangeCategory.INTERNAL_CHANGES
    );

    public ChangeCategory resolve(GitHubPullRequest pullRequest, Set<ChangeCategory> commitCategories) {
        Set<ChangeCategory> labelCategories = resolveLabelCategories(pullRequest.labels());

        for (ChangeCategory category : CATEGORY_PRIORITY) {
            if (labelCategories.contains(category)) {
                return category;
            }
        }

        for (ChangeCategory category : CATEGORY_PRIORITY) {
            if (commitCategories.contains(category)) {
                return category;
            }
        }

        return ChangeCategory.OTHER_CHANGES;
    }

    private Set<ChangeCategory> resolveLabelCategories(List<GitHubLabel> labels) {
        Set<ChangeCategory> categories = EnumSet.noneOf(ChangeCategory.class);

        for (GitHubLabel label : labels) {
            if (label.name() == null) {
                continue;
            }

            ChangeCategory category = LABEL_CATEGORIES.get(label.name().toLowerCase(Locale.ROOT));

            if (category != null) {
                categories.add(category);
            }
        }

        return categories;
    }
}