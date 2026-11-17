# GitHub integration

GitHub enrichment associates commits with pull requests and adds user-facing metadata to generated release notes.

## What it adds

- Pull request title
- Pull request number and link
- Author and profile link
- Label-based release category
- One release change for related pull request commits where applicable

The repository is resolved from the local Git remote named `origin`.

## Token requirements

The application reads a token from the `GITHUB_TOKEN` environment variable. Use a fine-grained token limited to the required repository with read-only repository content and pull request access.

!!! warning "Protect the token"
    Never place a token in source code, command arguments, committed scripts, screenshots, or documentation. Revoke it immediately if it is exposed.

## Set a temporary token

The following examples keep the token only in the current shell and its child processes.

=== "Windows PowerShell"

    ```powershell
    $secureToken = Read-Host "Fine-grained GitHub token" -AsSecureString
    $env:GITHUB_TOKEN = [System.Net.NetworkCredential]::new("", $secureToken).Password
    ```

    Verify its presence without printing it:

    ```powershell
    Test-Path Env:GITHUB_TOKEN
    ```

    Remove it when finished:

    ```powershell
    Remove-Item Env:GITHUB_TOKEN
    ```

=== "Bash"

    ```bash
    read -rsp "Fine-grained GitHub token: " GITHUB_TOKEN
    echo
    export GITHUB_TOKEN
    ```

    Remove it when finished:

    ```bash
    unset GITHUB_TOKEN
    ```

Closing the shell also removes a process-scoped token.

## Generate enriched notes

```powershell
java -jar release-notes.jar generate `
    --github `
    --from v0.1.0 `
    --to v0.2.0 `
    --output RELEASE-NOTES.md
```

## Repository remote

Inspect configured remotes:

```powershell
git remote -v
```

A supported GitHub URL must be configured as `origin`, for example:

```powershell
git remote set-url origin https://github.com/OWNER/REPOSITORY.git
```

HTTPS and common GitHub SSH remote formats are supported.

## Label categorization

Recognized labels include:

| Labels | Category |
| --- | --- |
| `feature`, `enhancement` | Features |
| `bug`, `fix` | Bug fixes |
| `performance` | Performance |
| `documentation`, `docs` | Documentation |
| `refactoring`, `refactor` | Refactoring |
| `dependencies`, `build` | Build system |
| `ci` | CI/CD |
| `tests`, `test` | Tests |

When no supported label is present, categories inferred from associated commits are used as a fallback.

!!! info "GitHub is optional"
    Local generation does not call GitHub. The GitHub API is used only when `--github` is selected.

## Data sent to GitHub

The integration sends API requests containing repository and commit identifiers. GitHub processes those requests under GitHub's own policies. See the [privacy policy](../legal/privacy.md) for the complete data-flow summary.
