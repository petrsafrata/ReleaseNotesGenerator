package cz.jpmad.releasenotes.git;

import java.time.OffsetDateTime;

public record GitCommit(
        String hash,
        String shortHash,
        String author,
        OffsetDateTime date,
        String message
) {
}
