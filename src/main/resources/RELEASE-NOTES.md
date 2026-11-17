# Release Notes

## [0.1.0]

### Added

- Release note generation between two Git references.
- Automatic previous-tag resolution when the starting reference is omitted.
- Commit categorization into features, bug fixes, performance, documentation, refactoring, build, CI/CD, tests, internal changes, and other changes.
- Markdown output to the terminal or a selected file.
- Optional GitHub enrichment with pull request titles, numbers, links, authors, and labels.
- Pull request label-based change categorization.
- Optional AI-assisted summaries and improved change descriptions through Ollama.
- Structured AI response validation that preserves the original set of release changes.
- Configurable AI model, output language, and Ollama API endpoint.
- Docker-based Ollama start, status, stop, model listing, and model download commands.
- Persistent Docker volume for downloaded Ollama models.
- Interactive terminal interface with guided release generation and Ollama management.
- Keyboard navigation, command mode, progress reporting, and `Esc` navigation.
- Script-friendly Picocli command-line interface.
- Actionable error messages for Git, GitHub, Docker, Ollama, missing models, invalid responses, and timeouts.
- Log4j2 diagnostic logging.
- Executable JAR distribution for Java 21.

### Documentation

- Added installation and configuration guides.
- Added GitHub and Ollama integration documentation.
- Added architecture, development, and troubleshooting documentation.
- Added project privacy policy and contribution guidelines.
- Added MkDocs Material documentation website configuration.

### Security

- GitHub tokens are read only from the `GITHUB_TOKEN` environment variable.
- GitHub tokens are not stored by the application or sent to Ollama.
- GitHub and Ollama integrations are disabled unless explicitly enabled by the user.
- Git commands are executed as structured process arguments instead of shell command strings.
- AI responses are validated before being applied to release changes.
- Missing, duplicated, or invented AI change identifiers are rejected.
- HTTP requests use explicit connection and operation timeouts.
- Interrupted operations restore the thread interruption flag.
- The application includes no analytics, telemetry, advertising, user accounts, or developer-controlled backend.
