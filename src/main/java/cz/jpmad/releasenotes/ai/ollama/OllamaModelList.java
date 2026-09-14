package cz.jpmad.releasenotes.ai.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaModelList(
        List<OllamaModel> models
) {
    public OllamaModelList {
        models = models == null ? List.of() : List.copyOf(models);
    }
}