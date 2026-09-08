package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitTagProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseRangeResolverTest {

    @Test
    void resolvesFirstReleaseWithoutPreviousTag() {
        GitTagProvider tagProvider = new StubTagProvider(Optional.of("v0.0.1"), Optional.empty());
        ReleaseRange range = new ReleaseRangeResolver(tagProvider).resolve(null, "HEAD");

        assertTrue(range.initialRelease());
        assertTrue(range.from().isEmpty());
        assertEquals("v0.0.1", range.to());
    }

    @Test
    void resolvesChangesBetweenPreviousAndCurrentTag() {
        GitTagProvider tagProvider = new StubTagProvider(Optional.of("v0.0.2"), Optional.of("v0.0.1"));
        ReleaseRange range = new ReleaseRangeResolver(tagProvider).resolve(null, "HEAD");

        assertEquals(Optional.of("v0.0.1"), range.from());
        assertEquals("v0.0.2", range.to());
    }

    @Test
    void usesExplicitFromReference() {
        GitTagProvider tagProvider = new StubTagProvider(Optional.of("v0.0.2"), Optional.of("v0.0.1"));
        ReleaseRange range = new ReleaseRangeResolver(tagProvider).resolve("custom-reference", "HEAD");

        assertEquals(Optional.of("custom-reference"), range.from());
    }

    @Test
    void failsWhenTargetDoesNotHaveTag() {
        GitTagProvider tagProvider = new StubTagProvider(Optional.empty(), Optional.empty());

        ReleaseRangeException exception = assertThrows(ReleaseRangeException.class,
                () -> new ReleaseRangeResolver(tagProvider).resolve(null, "HEAD")
        );

        assertEquals("Target reference 'HEAD' does not have a Git tag.", exception.getMessage());
    }

    private record StubTagProvider(Optional<String> exactTag, Optional<String> previousTag) implements GitTagProvider {

        @Override
        public Optional<String> findExactTag(String reference) {
            return exactTag;
        }

        @Override
        public Optional<String> findPreviousTag(String reference) {
            return previousTag;
        }
    }
}