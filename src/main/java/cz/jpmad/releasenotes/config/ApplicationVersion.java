package cz.jpmad.releasenotes.config;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Log4j2
@UtilityClass
public class ApplicationVersion {

    private static final String VERSION_RESOURCE = "VERSION";
    private static final String DEVELOPMENT_VERSION = "development";

    private static final String VERSION = loadVersion();

    public static String get() {
        return VERSION;
    }

    private static String loadVersion() {
        ClassLoader classLoader = ApplicationVersion.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(VERSION_RESOURCE)) {
            if (inputStream == null) {
                log.warn("Application version resource was not found.");
                return DEVELOPMENT_VERSION;
            }

            String version = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).strip();
            if (version.isBlank()) {
                log.warn("Application version resource is empty.");
                return DEVELOPMENT_VERSION;
            }
            return version;
        } catch (IOException exception) {
            log.warn("Unable to read the application version.", exception);
            return DEVELOPMENT_VERSION;
        }
    }
}
