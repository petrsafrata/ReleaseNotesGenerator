# Ollama AI integration

Ollama can rewrite release descriptions consistently and add a concise release summary. AI processing is optional and disabled by default.

## Managed Docker service

Release Notes Generator manages these Docker resources:

| Resource | Default |
| --- | --- |
| Container | `release-notes-ollama` |
| Image | `ollama/ollama` |
| Port | `11434:11434` |
| Volume | `release-notes-ollama-data` |
| API URL | `http://localhost:11434` |

The named volume preserves downloaded models when the container is stopped.

## Start and inspect Ollama

Ensure Docker Desktop or Docker Engine is running.

```powershell
java -jar release-notes.jar ai start
java -jar release-notes.jar ai status
```

The start command creates the managed container if it does not exist and starts an existing stopped container otherwise.

## Download a model

The default model is `gemma3:4b`:

```powershell
java -jar release-notes.jar ai pull gemma3:4b
```

List installed models:

```powershell
java -jar release-notes.jar ai models
```

!!! note
    Model downloads can take several minutes and consume multiple gigabytes of disk space. Required resources depend on the model and quantization.

## Generate with AI

```powershell
java -jar release-notes.jar generate `
    --ai `
    --model gemma3:4b `
    --language en
```

Use GitHub and AI together:

```powershell
java -jar release-notes.jar generate `
    --github `
    --ai `
    --model gemma3:4b `
    --language en `
    --output RELEASE-NOTES.md
```

## Structured response validation

The application sends structured release data and requires a structured response. A valid response must contain:

- A non-empty summary
- Exactly one description for every input change
- The original stable identifier for each change
- No duplicate or invented identifiers

!!! info "Changes cannot disappear silently"
    Invalid model output is rejected before rendering. The application does not publish a partial response when the model omits or invents changes.

## Use another Ollama endpoint

```powershell
java -jar release-notes.jar generate `
    --ai `
    --ollama-url http://localhost:11434
```

The same option is available for model commands:

```powershell
java -jar release-notes.jar ai models `
    --ollama-url http://localhost:11434
```

!!! warning "Remote endpoints"
    The default endpoint normally keeps processing on the local computer. A custom URL can transmit release data to another machine. Review its transport security, access controls, and retention policy before use.

## Stop Ollama

```powershell
java -jar release-notes.jar ai stop
```

Stopping the container does not remove downloaded models.

## Performance

Generation time depends on model size, quantization, CPU, memory, GPU availability, and the number of release changes. The first request is commonly slower because the model must be loaded into memory.
