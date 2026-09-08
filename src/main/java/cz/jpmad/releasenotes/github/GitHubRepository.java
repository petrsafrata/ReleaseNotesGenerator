package cz.jpmad.releasenotes.github;

public record  GitHubRepository(
        String owner,
        String name
) {
    public String fullName() {
        return owner + "/" + name;
    }
}
