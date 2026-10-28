package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitTagProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ReleaseRangeResolver {

    @NonNull
    private final GitTagProvider gitTagProvider;

    public ReleaseRange resolve(String from, String to) {
        String targetReference = resolveTargetReference(to);

        String targetTag = gitTagProvider.findExactTag(targetReference)
                .orElseThrow(() -> new ReleaseRangeException(
                        "Target reference '%s' does not have a Git tag."
                                .formatted(targetReference)
                ));

        Optional<String> previousTag = resolvePreviousTag(from, targetTag);

        return new ReleaseRange(previousTag, targetTag);
    }

    private String resolveTargetReference(String to) {
        if (to == null || to.isBlank()) {
            return "HEAD";
        }

        return to;
    }

    private Optional<String> resolvePreviousTag(String from, String targetTag) {
        if (from != null && !from.isBlank()) {
            return Optional.of(from);
        }

        return gitTagProvider.findPreviousTag(targetTag);
    }
}
