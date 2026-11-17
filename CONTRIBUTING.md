# 🤝 Contributing to Release Notes Generator

Welcome, and thank you for taking the time to contribute.

Release Notes Generator favors focused, secure, well-tested, and maintainable changes that follow the existing project structure. The application is local-first and supports both a command-line interface and an interactive terminal interface.

For installation, development setup, build instructions, and an overview of the project, see the [README](README.md) and the documentation in the [`docs`](docs) directory.

---

## 📌 How to Contribute

1. Fork the repository.
2. Create a branch from the current `master` branch.
3. Make a focused change.
4. Add or update tests and documentation.
5. Run the complete build locally.
6. Submit a pull request.

Example branch names:

```text
feature/release-preview
fix/ollama-timeout-message
docs/github-configuration
refactor/ai-client-construction
```

Keep each pull request limited to one logical change. If a proposal would substantially alter the architecture, data flow, security model, CLI contract, or supported integrations, open an issue before implementing it.

In the pull request description, include:

- what changed;
- why the change is needed;
- how the change was tested;
- any compatibility impact on the CLI or TUI;
- any security or privacy impact;
- any new network request, stored data, permission, dependency, or external service;
- terminal output or screenshots for visible interface changes.

---

## 🧭 Contribution Rules

### Code quality

- Target Java 21 and preserve compatibility with the versions declared in `pom.xml`.
- Keep changes small, localized, and consistent with the existing codebase.
- Prefer clear domain names and explicit behavior over clever abstractions.
- Keep classes and methods focused on a single responsibility.
- Preserve package boundaries between CLI, TUI, generation, Git, GitHub, Ollama, Docker, configuration, release, and output concerns.
- Avoid unrelated refactoring, reformatting, or dependency upgrades in the same pull request.
- Avoid new runtime dependencies unless they provide clear value and are justified in the pull request.
- Preserve existing public behavior unless the change intentionally modifies it.
- Handle interruptions correctly by restoring the thread interruption flag.
- Do not silently ignore failures that should be visible to the user.

### Lombok, Jackson, and records

- Use Lombok only where it removes mechanical boilerplate without hiding important behavior.
- `@RequiredArgsConstructor` is appropriate for straightforward dependency constructors with `final` fields.
- Keep explicit constructors when construction performs validation, normalization, or other meaningful work.
- Prefer Java records for immutable data carriers.
- Preserve defensive copies of mutable collections, for example with `List.copyOf`.
- Add Jackson annotations only when the JSON contract differs from Java naming or requires explicit configuration.
- Reuse the centrally configured `ObjectMapper`; do not create inconsistent mappers throughout the application.

### CLI compatibility

The Picocli interface is a public contract. When changing commands or options:

- preserve existing command names and option semantics unless a breaking change is intentional;
- provide useful descriptions and actionable error messages;
- use standard Picocli exit codes;
- send generated Markdown to standard output;
- send progress, diagnostics, and errors to standard error;
- keep commands usable independently of the TUI;
- update command tests, help text, README, and documentation.

Do not make the CLI depend on an interactive terminal. Scripts and CI environments must continue to work.

### Terminal interface

The TUI is part of the first-class user experience. When changing it:

- keep keyboard navigation consistent across screens;
- preserve `Esc` navigation back to the previous or main screen where applicable;
- ensure command output remains visible until the user chooses to continue;
- avoid terminal flickering and unnecessary screen clearing;
- route Picocli output through the configured terminal adapter;
- test narrow terminals and both light and dark terminal themes when changing colors;
- keep all actions available through the non-interactive CLI as well.

Visible TUI changes should include screenshots or a short recording in the pull request when practical.

### Git processing

- Execute Git commands as argument lists; do not construct shell command strings from user input.
- Treat repository paths, references, tags, commit messages, remote URLs, and command output as untrusted input.
- Keep Git operations read-only unless a feature explicitly documents otherwise.
- Preserve support for repositories without tags, without a GitHub remote, or without matching commits.
- Keep platform compatibility in mind, especially Windows and Linux path handling.
- Do not expose confidential repository metadata in unnecessary logs.

### GitHub integration

GitHub enrichment is optional and disabled by default. Changes to it must follow these rules:

- read authentication only from the `GITHUB_TOKEN` environment variable;
- never store, print, log, or include the token in generated release notes;
- send the token only in the HTTP `Authorization` header;
- communicate with GitHub over HTTPS;
- request only the repository and pull request data required for enrichment;
- treat GitHub API responses as untrusted input;
- handle missing remotes, invalid tokens, authorization errors, rate limits, and incomplete responses clearly;
- keep GitHub-free generation fully functional.

> [!IMPORTANT]
> Any change affecting token handling, GitHub API requests, repository identification, or transmitted data must include a short security and privacy explanation in the pull request.

### Ollama and Docker integration

Ollama enrichment is optional and disabled by default. Changes to the integration must:

- preserve `http://localhost:11434` as the documented local default unless a deliberate compatibility change is approved;
- validate model names, endpoint values, requests, and responses;
- use explicit connection and request timeouts;
- provide actionable errors for unavailable Ollama, missing models, timeouts, and malformed responses;
- preserve deterministic generation settings where applicable;
- validate AI output before applying it to release changes;
- never send the GitHub token or other credentials to Ollama;
- clearly document any newly transmitted release data.

Docker-related changes must preserve the existing named container and persistent model volume unless a migration is provided. Commands should be idempotent: starting an already running service or stopping an already stopped service must remain safe.

> [!WARNING]
> A custom Ollama URL may point to a remote service. Do not add new data to AI requests without considering whether it may disclose private repository information.

### Security

- Never commit or log secrets, tokens, credentials, or authorization headers.
- Never include secrets in exception messages, generated Markdown, test fixtures, screenshots, or documentation examples.
- Use structured process arguments instead of invoking commands through a shell.
- Validate data received from Git, GitHub, Ollama, Docker, configuration, and user input.
- Use safe HTTP timeouts and preserve TLS validation for HTTPS endpoints.
- Avoid unsafe deserialization and overly permissive Jackson configuration.
- Do not introduce telemetry, analytics, advertising, or developer-controlled data collection without prior discussion and explicit documentation.
- Do not weaken Log4j2, dependency, or build security settings without justification.
- Review all new runtime dependencies for maintenance status, licensing, and known security concerns.

If you discover a vulnerability, do not publish credentials, private repository data, or immediately exploitable details in a public issue. [Open an issue](https://github.com/petrsafrata/ReleaseNotesGenerator/issues/new) requesting a private contact method.

### Privacy

Update [`PRIVACY.md`](PRIVACY.md) when a change affects:

- Git metadata processed by the application;
- GitHub API communication or token handling;
- data sent to Ollama or another endpoint;
- generated files, diagnostic logs, caching, or retention;
- Docker volumes or other persistent storage;
- analytics, telemetry, or any developer-controlled data collection.

> [!IMPORTANT]
> Release Notes Generator is local-first. A contribution must not introduce hidden network communication or data collection.

### Dependencies and third-party assets

Before adding a dependency, model-related component, icon, image, font, or other third-party asset:

- confirm that its license is compatible with Apache License 2.0 and redistribution in this project;
- add complete Maven license metadata where applicable;
- preserve any legally required copyright, license, or attribution notice;
- explain why the new dependency or asset is necessary;
- avoid replacing a small implementation with a large dependency;
- verify that SVG files contain no scripts, event handlers, external resources, or `foreignObject` elements;
- do not imply endorsement by GitHub, Ollama, Docker, or another trademark owner.

Do not manually edit generated dependency notices. If distributed dependencies change, regenerate and review the project's third-party license report as part of the release process.

### Documentation

Update `README.md` and the relevant pages in `docs` when changing:

- installation or build requirements;
- user-visible CLI or TUI behavior;
- commands, options, defaults, or exit behavior;
- GitHub token requirements;
- Ollama, Docker, or model configuration;
- generated output or supported workflows;
- security, privacy, or data handling;
- architecture or contributor setup.

Documentation must build successfully with MkDocs in strict mode.

---

## 🧪 Testing

### Automated tests

Run the complete Maven verification before submitting a pull request.

On Linux or macOS:

```bash
./mvnw -B clean verify
```

On Windows PowerShell:

```powershell
.\mvnw.cmd -B clean verify
```

Tests should be deterministic and must not require a real GitHub token, running Ollama service, Docker daemon, or internet connection unless they are explicitly marked and documented as integration tests.

When fixing a bug, add a regression test that fails before the fix and passes afterward whenever practical.

### Documentation build

Install the documentation dependencies and build with strict validation:

```bash
python -m pip install -r requirements-docs.txt
mkdocs build --strict
```

### Manual verification

Automated tests do not replace all real-world checks. Perform focused manual verification when changing integration boundaries or terminal behavior.

Depending on the change, verify:

- local generation without optional integrations;
- generation with GitHub only;
- generation with Ollama only;
- generation with both GitHub and Ollama;
- Markdown written to the terminal and to an output file;
- CLI help, version output, invalid input, and non-zero failure exit codes;
- TUI navigation, `Esc`, output visibility, and command mode;
- Ollama start, status, stop, model listing, and model download behavior;
- clear failure behavior when GitHub, Ollama, Docker, Git, a model, or a token is unavailable.

Do not perform expensive AI generation after every small internal refactor. Run it when AI request construction, response parsing, validation, networking, defaults, or the end-to-end generation flow changes.

---

## ✅ Before Submitting a Pull Request

- [ ] The change is focused and contains no unrelated refactoring.
- [ ] `mvnw clean verify` completes successfully.
- [ ] New behavior and bug fixes are covered by tests.
- [ ] Relevant manual checks were completed.
- [ ] CLI output, help, and exit behavior remain correct.
- [ ] TUI navigation and output remain usable.
- [ ] No secrets, tokens, credentials, private data, or debug code were committed.
- [ ] No unnecessary runtime dependency or network request was introduced.
- [ ] Error messages are actionable and do not disclose sensitive data.
- [ ] README and documentation were updated where required.
- [ ] MkDocs builds successfully with `--strict` when documentation changed.
- [ ] `PRIVACY.md` was updated if data handling changed.
- [ ] Third-party licenses and required attribution were reviewed.
- [ ] The shaded executable JAR can be built when packaging changed.

---

## 🐛 Reporting Bugs

[Open a GitHub issue](https://github.com/petrsafrata/ReleaseNotesGenerator/issues/new) and include:

- operating system and version;
- Java version;
- Release Notes Generator version;
- Git, Docker, and Ollama versions when relevant;
- whether the CLI or TUI was used;
- the exact command or workflow;
- steps to reproduce the problem;
- expected behavior;
- actual behavior and exit code;
- relevant sanitized logs or terminal output;
- screenshots for terminal-interface issues when useful.

> [!NOTE]
> Use a small public test repository as a reproduction example whenever possible.

Do not include:

- GitHub tokens or authorization headers;
- credentials or environment-variable dumps;
- private repository names, URLs, commits, or pull request content;
- confidential generated release notes;
- personal information;
- sensitive local filesystem paths.

---

## 💡 Requesting Features

Before implementing a large feature, [open a GitHub issue](https://github.com/petrsafrata/ReleaseNotesGenerator/issues/new) describing:

- the problem to solve;
- the proposed user experience;
- expected CLI and TUI behavior;
- alternative solutions considered;
- security, privacy, compatibility, and maintenance implications.

Discussion before implementation helps avoid duplicated work and incompatible design decisions.

---

## 📜 License

This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

Unless explicitly stated otherwise, contributions submitted to this project are provided under the same license.

By submitting a contribution, you confirm that you have the right to provide it under the Apache License 2.0.

---

Thank you for contributing to Release Notes Generator!
