package cz.jpmad.releasenotes.release;

import cz.jpmad.releasenotes.git.GitCommit;

public record ReleaseChange(
        ChangeCategory category,
        String description,
        GitCommit sourceCommit
) {
}
