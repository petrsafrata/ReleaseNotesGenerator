# Installation

Release Notes Generator can be downloaded as an executable JAR or built locally from source.

## Requirements

The core application requires:

- Java 21 or newer
- Git available on `PATH`
- A local Git repository

Optional functionality requires:

- Docker Desktop or Docker Engine for the managed Ollama service
- A GitHub token for GitHub pull request enrichment

## Install from GitHub Releases

1. Open the [GitHub Releases](https://github.com/petrsafrata/ReleaseNotesGenerator/releases) page.
2. Download `release-notes.jar` and `release-notes.jar.sha256` from the latest release.
3. Store both files in a permanent directory, for example `D:\Tools\release-notes`.
4. Verify the checksum.
5. Run the version command.

=== "Windows PowerShell"

    ```powershell
    Set-Location D:\Tools\release-notes

    $expected = (Get-Content .\release-notes.jar.sha256).Split()[0]
    $actual = (Get-FileHash .\release-notes.jar -Algorithm SHA256).Hash

    if ($actual -ne $expected) {
        throw "Checksum verification failed."
    }

    java -jar .\release-notes.jar --version
    ```

=== "Linux"

    ```bash
    sha256sum --check release-notes.jar.sha256
    java -jar release-notes.jar --version
    ```

=== "macOS"

    ```bash
    shasum -a 256 release-notes.jar
    java -jar release-notes.jar --version
    ```

!!! warning "Verify downloaded artifacts"
    Do not run a downloaded JAR when its checksum does not match the checksum published with the release.

## Run the application

Open the Git repository for which release notes should be generated, then start the application:

```powershell
Set-Location D:\Java_Projects\MyRepository
java -jar D:\Tools\release-notes\release-notes.jar
```

Running the JAR without arguments opens the interactive terminal interface.

When Java is not on `PATH`, invoke the selected JDK directly:

```powershell
& "C:\Path\To\jdk-21\bin\java.exe" `
    -jar D:\Tools\release-notes\release-notes.jar
```

!!! note
    Git commands run in the current working directory. The location of `release-notes.jar` does not determine the target repository.

## Build from source

Clone and build the project with the included Maven Wrapper.

=== "Windows PowerShell"

    ```powershell
    git clone https://github.com/petrsafrata/ReleaseNotesGenerator.git
    Set-Location .\ReleaseNotesGenerator
    .\mvnw.cmd clean verify
    ```

=== "Linux or macOS"

    ```bash
    git clone https://github.com/petrsafrata/ReleaseNotesGenerator.git
    cd ReleaseNotesGenerator
    chmod +x mvnw
    ./mvnw clean verify
    ```

The executable shaded JAR is created at:

```text
target/release-notes.jar
```

## Update

Manual installations do not update automatically.

1. Download the JAR and checksum from the newer release.
2. Verify the checksum.
3. Replace the previous JAR.
4. Run `--version` to confirm the installed version.

Ollama models are stored separately in a Docker volume and are not removed when the application JAR is replaced.

## Uninstall

Delete `release-notes.jar` and any application log files you no longer need.

The managed Ollama container and model volume are separate Docker resources. Remove them manually only when their downloaded models are no longer required.
