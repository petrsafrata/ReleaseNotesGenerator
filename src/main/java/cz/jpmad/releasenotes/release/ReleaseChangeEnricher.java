package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.github.GitHubPullRequestAssociation;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

public class ReleaseChangeEnricher {

    private final PullRequestCategoryResolver categoryResolver;

    public ReleaseChangeEnricher() {
        this(new PullRequestCategoryResolver());
    }

    ReleaseChangeEnricher(PullRequestCategoryResolver categoryResolver) {
        this.categoryResolver = categoryResolver;
    }

    public List<ReleaseChange> enrich(List<ReleaseChange> changes, List<GitHubPullRequestAssociation> associations) {
        Map<String, ReleaseChange> changesByCommit = mapChangesByCommit(changes);
        Map<String, List<GitHubPullRequestAssociation>> associationsByCommit = mapAssociationsByCommit(associations);

        Set<Integer> includedPullRequests = new HashSet<>();
        List<ReleaseChange> enrichedChanges = new ArrayList<>();

        for (ReleaseChange change : changes) {
            String commitHash = change.sourceCommit().hash();
            List<GitHubPullRequestAssociation> commitAssociations = associationsByCommit.get(commitHash);

            if (commitAssociations == null || commitAssociations.isEmpty()) {
                enrichedChanges.add(change);
                continue;
            }

            for (GitHubPullRequestAssociation association : commitAssociations) {
                int pullRequestNumber = association.pullRequest().number();

                if (!includedPullRequests.add(pullRequestNumber)) {
                    continue;
                }

                Set<ChangeCategory> commitCategories = resolveCommitCategories(association, changesByCommit);
                ChangeCategory category = categoryResolver.resolve(association.pullRequest(), commitCategories);

                enrichedChanges.add(new ReleaseChange(
                        category,
                        association.pullRequest().title(),
                        change.sourceCommit(),
                        Optional.of(association.pullRequest())
                ));
            }
        }

        return List.copyOf(enrichedChanges);
    }

    private Map<String, ReleaseChange> mapChangesByCommit(List<ReleaseChange> changes) {
        Map<String, ReleaseChange> result = new HashMap<>();

        for (ReleaseChange change : changes) {
            result.put(change.sourceCommit().hash(), change);
        }

        return result;
    }

    private Map<String, List<GitHubPullRequestAssociation>> mapAssociationsByCommit(List<GitHubPullRequestAssociation> associations) {
        Map<String, List<GitHubPullRequestAssociation>> result = new HashMap<>();

        for (GitHubPullRequestAssociation association : associations) {
            for (String commitHash : association.commitHashes()) {
                result.computeIfAbsent(commitHash, ignored -> new ArrayList<>()).add(association);
            }
        }

        return result;
    }

    private Set<ChangeCategory> resolveCommitCategories(GitHubPullRequestAssociation association,
                                                        Map<String, ReleaseChange> changesByCommit) {
        Set<ChangeCategory> categories = EnumSet.noneOf(ChangeCategory.class);

        for (String commitHash : association.commitHashes()) {
            ReleaseChange change = changesByCommit.get(commitHash);

            if (change != null) {
                categories.add(change.category());
            }
        }

        return categories;
    }
}