# Privacy Policy

**Effective date:** October 28, 2026  
**Last updated:** October 28, 2026

## Summary

Release Notes Generator is a local-first command-line application. It does not include analytics, advertising, telemetry, user accounts, or a backend operated by the project author.

By default, the application processes Git metadata locally. Data is sent outside the application only when you explicitly enable an optional integration, such as GitHub or a remotely hosted Ollama endpoint.

## Data processed by the application

Depending on the selected features, Release Notes Generator may process:

- local repository paths and configured Git remote URLs;
- Git references and tags;
- commit hashes, messages, author names, and commit dates;
- GitHub repository owner and repository name;
- pull request numbers, titles, states, URLs, authors, and labels;
- generated release-note descriptions and summaries;
- the selected language, Ollama model, and Ollama endpoint;
- diagnostic information, including error messages and application events.

As part of its normal release-note generation workflow, the application does not intentionally read or transmit source-file contents, file diffs, access tokens, or Git credentials.

## Local processing

Without optional integrations, the application reads Git metadata from the repository in the current working directory, categorizes the detected changes, and generates Markdown locally.

The project author does not receive this information.

## GitHub integration

GitHub enrichment is disabled by default. It is activated only when you enable GitHub integration in the terminal interface or use the `--github` command-line option.

When enabled, the application:

- reads the `origin` Git remote to identify the GitHub repository;
- reads an access token from the `GITHUB_TOKEN` environment variable;
- sends the repository identity and relevant commit identifiers to the GitHub API;
- retrieves repository information and pull request metadata associated with those commits;
- uses pull request titles, authors, URLs, and labels to enrich the generated release notes.

The GitHub token is used only to authorize API requests. The application does not intentionally include the token in generated release notes or diagnostic logs. The token is not saved by the application; however, the shell or operating system is responsible for the lifetime and protection of the environment variable.

GitHub processes API requests according to [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement) and applicable terms.

## Ollama integration

AI-assisted improvement is disabled by default. It is activated only when you enable AI processing in the terminal interface or use the `--ai` command-line option.

When enabled, the application sends structured release data to the configured Ollama API. This data may contain:

- the target release version;
- the requested output language;
- change identifiers and categories;
- commit messages or pull request descriptions used to describe the changes.

The default endpoint is `http://localhost:11434`, which normally keeps AI processing on the local computer. Ollama models managed by the application run in a local Docker container by default.

If you configure `--ollama-url` to point to another computer, server, proxy, or hosted service, release data is transmitted to that endpoint. Its operator may process, log, or retain the data according to their own policies. Review and trust the endpoint before sending data from a private or confidential repository.

The application does not send the `GITHUB_TOKEN` to Ollama.

## Data storage and retention

Release Notes Generator does not maintain a user database, cloud account, user profile, or developer-controlled storage service.

Data may nevertheless be stored in the following locations:

- generated release notes are displayed in the terminal or written to a path selected by the user;
- diagnostic logs may be written according to the active Log4j2 configuration;
- downloaded Ollama models are stored in the Docker volume `release-notes-ollama-data`;
- GitHub, a custom Ollama endpoint, Docker, reverse proxies, or operating-system services may keep their own logs or cached data.

Generated files and logs may contain repository names, remote URLs, commit messages, pull request metadata, local paths, or error details. The application does not automatically upload these files or delete them. Users are responsible for reviewing, protecting, retaining, and deleting them when appropriate.

## Data collection by the project author

The project author does not automatically receive data processed by the application.

Information is received only when a user voluntarily provides it—for example, in a GitHub issue, discussion, or diagnostic attachment.

Before sharing diagnostics, remove or redact:

- access tokens, credentials, and authorization headers;
- private repository names and URLs;
- confidential commit messages or pull request content;
- personal information;
- organization-specific or local filesystem paths.

## Security

Release Notes Generator is designed to minimize data exposure, but no software can guarantee absolute security. Users should:

- use a fine-grained GitHub token with the minimum permissions required;
- keep tokens only in temporary environment variables where practical;
- never commit tokens or credentials to the repository;
- use HTTPS for remote Ollama endpoints;
- avoid sending confidential repository metadata to untrusted endpoints;
- restrict access to generated files, logs, and Docker volumes;
- keep Java, Docker, Ollama, the application, and its dependencies up to date;
- review generated release notes before publishing them.

If you believe you have found a security issue, do not include secrets or exploitable details in a public issue. Open an issue requesting a private contact method.

## Third-party services

Optional integrations are provided by third parties and are governed by their own privacy policies and terms. This policy describes how Release Notes Generator uses those integrations but does not control how GitHub, Ollama deployments, model providers, hosting providers, proxies, or infrastructure operators process data.

## Changes to this policy

This Privacy Policy may be updated when application functionality, integrations, or data flows change. The effective date and last-updated date at the top of this document will be revised when changes are made. Material changes will be documented in the project repository or release notes.

## Questions and privacy requests

For questions about this Privacy Policy or the application's data handling, [open a GitHub issue](https://github.com/petrsafrata/ReleaseNotesGenerator/issues/new).

Do not include access tokens, credentials, private repository data, personal information, or other sensitive content in a public issue.
