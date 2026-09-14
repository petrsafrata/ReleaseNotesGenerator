package cz.jpmad.releasenotes.docker;

public class DockerException extends RuntimeException {
    public DockerException(String message) {
        super(message);
    }

    public DockerException(String message, Throwable cause) {
        super(message, cause);
    }
}
