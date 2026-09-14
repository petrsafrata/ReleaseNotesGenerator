package cz.jpmad.releasenotes.docker;

@FunctionalInterface
public interface DockerCommandRunner {

    DockerCommandResult execute(String... arguments);
}
