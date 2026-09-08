package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitCommit;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeCategorizer {

    private static final Pattern CONVENTIONAL_COMMIT_PATTERN =
            Pattern.compile(
                    "^(feat|fix|docs|refactor|test|chore|perf|build|ci)"
                            + "(?:\\([^)]*\\))?"
                            + "!?:\\s+"
                            + "(.+)$"
            );

    public List<ReleaseChange> categorize(List<GitCommit> commits) {
        return commits.stream()
                .map(this::categorize)
                .toList();
    }

    private ReleaseChange categorize(GitCommit commit) {
        Matcher matcher = CONVENTIONAL_COMMIT_PATTERN.matcher(commit.message());

        if (!matcher.matches()) {
            return new ReleaseChange(ChangeCategory.OTHER_CHANGES, commit.message(), commit);
        }

        String type = matcher.group(1);
        String description = matcher.group(2);

        return new ReleaseChange(resolveCategory(type), description, commit
        );
    }

    private ChangeCategory resolveCategory(String type) {
        return switch (type) {
            case "feat" -> ChangeCategory.FEATURES;
            case "fix" -> ChangeCategory.BUG_FIXES;
            case "docs" -> ChangeCategory.DOCUMENTATION;
            case "refactor" -> ChangeCategory.REFACTORING;
            case "test" -> ChangeCategory.TESTS;
            case "chore" -> ChangeCategory.INTERNAL_CHANGES;
            case "perf" -> ChangeCategory.PERFORMANCE;
            case "build" -> ChangeCategory.BUILD_SYSTEM;
            case "ci" -> ChangeCategory.CI_CD;
            default -> ChangeCategory.OTHER_CHANGES;
        };
    }
}
