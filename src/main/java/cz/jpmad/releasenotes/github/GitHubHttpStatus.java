package cz.jpmad.releasenotes.github;

public enum GitHubHttpStatus {

    OK(200, "OK"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    CONFLICT(409, "Conflict"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    UNKNOWN(-1, "Unknown");

    private final int code;
    private final String label;

    GitHubHttpStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static GitHubHttpStatus fromCode(int code) {
        for (GitHubHttpStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }

        return UNKNOWN;
    }
}