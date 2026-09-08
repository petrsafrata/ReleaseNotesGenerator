package cz.jpmad.releasenotes.git;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class GitLogParser {

    private static final String FIELD_SEPARATOR = "\u001f";
    private static final String RECORD_SEPARATOR = "\u001e";

    public List<GitCommit> parse(String output) {
        if (output == null || output.isBlank()) {
            return List.of();
        }

        List<GitCommit> commits = new ArrayList<>();
        String[] records = output.split(RECORD_SEPARATOR);

        for (String record : records) {
            if (record.isBlank()) {
                continue;
            }
            commits.add(parseRecord(record.strip()));
        }
        return List.copyOf(commits);
    }

    private GitCommit parseRecord(String record) {
        String[] fields = record.split(FIELD_SEPARATOR, -1);

        if (fields.length != 5) {
            throw new GitCommandException("Unable to parse Git log output.");
        }
        return new GitCommit(fields[0], fields[1], fields[2], OffsetDateTime.parse(fields[3]), fields[4]);
    }
}
