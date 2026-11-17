# Development

## Requirements

- JDK 21
- Git
- Maven Wrapper included in the repository
- Docker for real Ollama checks
- A process-scoped GitHub token for real GitHub checks
- Python 3.11 or newer for documentation builds

## Build and test

=== "Windows PowerShell"

    ```powershell
    .\mvnw.cmd clean verify
    ```

=== "Linux or macOS"

    ```bash
    chmod +x mvnw
    ./mvnw clean verify
    ```

The executable JAR is created at `target/release-notes.jar`.

## Run the packaged application

```powershell
java -jar target\release-notes.jar --version
java -jar target\release-notes.jar --help
java -jar target\release-notes.jar
```

## Project structure

```text
src/
├── main/
│   ├── java/
│   │   └── cz/jpmad/
│   └── resources/
└── test/
    └── java/
        └── cz/jpmad/
```

Production packages separate AI, CLI, configuration, Docker, generation, Git, GitHub, output, release-domain, and TUI responsibilities.

## Test strategy

The JUnit 5 and Mockito test suite covers:

- Git output parsing and range resolution
- Commit and pull request categorization
- GitHub response parsing and enrichment
- Ollama request and response handling
- AI mapping and response validation
- Markdown rendering
- Docker command orchestration
- CLI command behavior
- Generation service orchestration

Automated tests must not require a personal token, Docker daemon, GitHub API, or Ollama server.

## Manual regression matrix

Run real integrations at release milestones or after related functional changes:

| Scenario | GitHub | AI |
| --- | --- | --- |
| Local generation | No | No |
| GitHub enrichment | Yes | No |
| Local AI | No | Yes |
| Complete generation | Yes | Yes |

Also verify:

- Output file writing
- Missing token error
- Stopped Ollama error
- Missing model error
- TUI navigation and ++esc++ behavior
- Ollama management screens

!!! tip
    Small refactorings normally require the automated suite. A full real AI generation is most useful after integration changes and before a release.

## Build the documentation

Create a virtual environment and install the pinned documentation dependency:

=== "Windows PowerShell"

    ```powershell
    py -3.13 -m venv .venv
    .\.venv\Scripts\Activate.ps1
    python -m pip install --requirement requirements-docs.txt
    mkdocs build --strict --clean
    ```

=== "Linux or macOS"

    ```bash
    python3 -m venv .venv
    source .venv/bin/activate
    python -m pip install --requirement requirements-docs.txt
    mkdocs build --strict --clean
    ```

Preview changes locally:

```powershell
mkdocs serve
```

## Add a generation setting

When introducing a new setting:

1. Add the Picocli option.
2. Add it to the generation request.
3. Pass it through the service layer.
4. Expose it in the TUI where applicable.
5. Update command and workflow tests.
6. Update the documentation.

## Release preparation

Before release:

```powershell
git status
git diff --check
.\mvnw.cmd clean verify
java -jar target\release-notes.jar --version
```

Ensure `VERSION`, the Maven project version, and application version output agree. A final release must not retain a `-SNAPSHOT` version.

See the repository [contributing guidelines](https://github.com/petrsafrata/ReleaseNotesGenerator/blob/master/CONTRIBUTING.md) for code and pull request expectations.
