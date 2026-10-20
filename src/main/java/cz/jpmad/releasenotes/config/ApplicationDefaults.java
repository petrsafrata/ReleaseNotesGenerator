package cz.jpmad.releasenotes.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ApplicationDefaults {

    public static final String TARGET_REFERENCE = "HEAD";
    public static final String OLLAMA_URL = "http://localhost:11434";
    public static final String OLLAMA_MODEL = "gemma3:4b";
    public static final String AI_LANGUAGE = "en";
}