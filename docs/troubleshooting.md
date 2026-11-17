# Troubleshooting

## The TUI does not open

The terminal may have been detected as non-interactive. Run the application directly in Windows Terminal, PowerShell, Command Prompt, or another full terminal emulator.

In CI jobs and redirected shells, use explicit CLI commands instead.

```powershell
java -jar release-notes.jar generate --help
```

## Git is unavailable

Verify the installation:

```powershell
git --version
```

Install Git and ensure its executable is available on `PATH`.

## The current directory is not a repository

Generation targets the current working directory.

```powershell
Set-Location D:\Java_Projects\MyRepository
git status
java -jar D:\Tools\release-notes\release-notes.jar generate
```

## The previous tag cannot be resolved

List available tags:

```powershell
git tag --list
```

Specify both references explicitly:

```powershell
java -jar release-notes.jar generate `
    --from <starting-reference> `
    --to <target-reference>
```

!!! note
    A repository needs at least two suitable release points for automatic previous-tag resolution to represent a normal release range.

## GitHub token is not configured

Set a process-scoped token before using `--github`:

```powershell
$secureToken = Read-Host "Fine-grained GitHub token" -AsSecureString
$env:GITHUB_TOKEN = [System.Net.NetworkCredential]::new("", $secureToken).Password
```

See [GitHub integration](integrations/github.md) for token and permission guidance.

## GitHub rejects the request

Check that:

- The token has not expired
- The token can access the selected repository
- Required read-only permissions are enabled
- Organization policies allow the token
- `origin` points to the expected GitHub repository

```powershell
git remote -v
```

## Docker is unavailable

Verify that Docker Desktop or Docker Engine is running:

```powershell
docker version
```

Then retry:

```powershell
java -jar release-notes.jar ai start
```

## Port 11434 is already in use

Another Ollama instance or application may already use the default port.

```powershell
Get-NetTCPConnection -LocalPort 11434 -ErrorAction SilentlyContinue
```

Stop the conflicting service or point the application to an existing Ollama API with `--ollama-url`.

## Ollama cannot be reached

```powershell
java -jar release-notes.jar ai status
java -jar release-notes.jar ai start
```

Confirm that the configured URL is reachable and that local firewall rules permit the connection.

## The model is not installed

List installed models and download the selected one:

```powershell
java -jar release-notes.jar ai models
java -jar release-notes.jar ai pull gemma3:4b
```

## AI generation is slow

The first request loads the model into memory. Larger models also require more CPU, RAM, GPU memory, and time.

Try:

- A smaller or more heavily quantized model
- A smaller release range
- Closing memory-intensive applications
- Configuring Ollama to use available GPU acceleration

## AI output is rejected

The model returned JSON that did not match the required release changes or schema. Retry the operation or choose a model with stronger structured-output support.

!!! info "Safe fallback"
    Release notes can still be generated without `--ai`. Local categorization and optional GitHub enrichment are independent of AI processing.

## The output file was not created

Check that:

- The parent directory exists
- The current user can write to it
- The path does not point to a directory
- Another application is not locking the file

Try an absolute path when a relative path is ambiguous.

## Report a problem

Include:

- Application version
- Java version
- Operating system
- Command with secrets removed
- Exit code
- Concise error output

!!! danger "Never share credentials"
    Remove GitHub tokens, authorization headers, private repository URLs, and confidential release content before sharing logs or screenshots.
