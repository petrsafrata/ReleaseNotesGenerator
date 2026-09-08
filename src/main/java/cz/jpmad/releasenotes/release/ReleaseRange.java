package cz.jpmad.releasenotes.release;

import java.util.Optional;

public record ReleaseRange(
        Optional<String> from,
        String to
) {
    public boolean initialRelease() {
        return from.isEmpty();
    }
}
