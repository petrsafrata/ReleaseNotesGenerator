package cz.jpmad.releasenotes.release;

public enum ChangeCategory {

    FEATURES("Features"),
    BUG_FIXES("Bug Fixes"),
    DOCUMENTATION("Documentation"),
    REFACTORING("Refactoring"),
    TESTS("Tests"),
    INTERNAL_CHANGES("Internal Changes"),
    PERFORMANCE("Performance"),
    BUILD_SYSTEM("Build System"),
    CI_CD("CI/CD"),
    OTHER_CHANGES("Other Changes");

    private final String heading;

    ChangeCategory(String heading) {
        this.heading = heading;
    }

    public String heading() {
        return heading;
    }
}
