package cz.jpmad.releasenotes.git;

import java.util.Optional;

public interface GitTagProvider {

    Optional<String> findExactTag(String reference);

    Optional<String> findPreviousTag(String reference);
}
