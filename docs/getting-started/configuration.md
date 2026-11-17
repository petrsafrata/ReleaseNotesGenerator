# Configuration and usage

Release Notes Generator is configured through command-line options and interactive menus. It does not require a configuration file.

## Interactive terminal interface

Start the JAR without arguments:

```powershell
java -jar release-notes.jar
```

The main menu provides:

- **Generate release notes** — select the range, integrations, language, model, and output destination
- **Manage Ollama AI** — inspect the service, start or stop it, list models, and download a model
- **Command mode** — run the same commands available from the standard CLI
- **Exit** — close the application

Use the arrow keys and ++enter++ to select an item. Press ++esc++ to cancel the current screen and return to the previous menu.

!!! tip "Command mode"
    Use `generate --help`, `ai --help`, or `help` inside command mode to inspect available commands without leaving the interactive application.

## Generate from the CLI

The minimal command is:

```powershell
java -jar release-notes.jar generate
```

By default, the application generates changes from the tag preceding `HEAD` to `HEAD` and prints Markdown to the terminal.

## Generation options

| Option | Default | Description |
| --- | --- | --- |
| `--from <reference>` | Previous tag | Starting Git reference |
| `--to <reference>` | `HEAD` | Target Git reference |
| `-o`, `--output <path>` | Terminal | Markdown output file |
| `--github` | Disabled | Enrich changes through GitHub |
| `--ai` | Disabled | Improve wording through Ollama |
| `--model <model>` | `gemma3:4b` | Ollama model |
| `--language <language>` | `en` | AI output language |
| `--ollama-url <URL>` | `http://localhost:11434` | Ollama API base URL |

Display the authoritative options for the installed version with:

```powershell
java -jar release-notes.jar generate --help
```

## Release range

An explicit range can use tags, branches, commit hashes, or other references understood by Git:

```powershell
java -jar release-notes.jar generate `
    --from v0.1.0 `
    --to v0.2.0
```

When `--from` is omitted, the application searches for the tag preceding the target reference.

!!! warning "Review the selected range"
    A valid but unintended range can produce technically correct release notes for the wrong set of commits. Review `--from` and `--to` before publication.

## Output

Write Markdown directly to a file:

```powershell
java -jar release-notes.jar generate `
    --from v0.1.0 `
    --to v0.2.0 `
    --output RELEASE-NOTES.md
```

Without `--output`, generated Markdown is written to standard output. Progress and diagnostics are written separately to standard error, so scripts can redirect Markdown safely.

```powershell
java -jar release-notes.jar generate 1> RELEASE-NOTES.md
```

## Optional integrations

GitHub enrichment:

```powershell
java -jar release-notes.jar generate --github
```

Local AI improvement:

```powershell
java -jar release-notes.jar generate `
    --ai `
    --model gemma3:4b `
    --language en
```

Combined generation:

```powershell
java -jar release-notes.jar generate `
    --github `
    --ai `
    --model gemma3:4b `
    --language en `
    --output RELEASE-NOTES.md
```

GitHub enrichment runs before AI improvement, so the model receives pull request titles where associations are available.

## Categorization

Conventional commit prefixes are used where possible:

| Prefix | Category |
| --- | --- |
| `feat:` | Features |
| `fix:` | Bug fixes |
| `perf:` | Performance |
| `docs:` | Documentation |
| `refactor:` | Refactoring |
| `build:` | Build system |
| `ci:` | CI/CD |
| `test:` | Tests |

GitHub pull request labels can override commit-derived categories. Unrecognized changes are preserved in a fallback category rather than discarded.

## Exit codes

| Exit code | Meaning |
| --- | --- |
| `0` | Command completed successfully |
| Non-zero | Invalid input or execution failure |

Automation should check the process exit code instead of relying only on terminal text.
