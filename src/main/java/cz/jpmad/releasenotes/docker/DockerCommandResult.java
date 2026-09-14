package cz.jpmad.releasenotes.docker;

public record DockerCommandResult(
        int exitCode,
        String output
) {
    public boolean successful() {
        return exitCode == 0;
    }
}
