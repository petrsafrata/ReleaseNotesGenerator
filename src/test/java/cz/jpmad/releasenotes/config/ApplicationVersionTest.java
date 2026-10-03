package cz.jpmad.releasenotes.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationVersionTest {

    @Test
    void loadsApplicationVersionFromClasspath() throws IOException {
        ClassLoader classLoader = ApplicationVersionTest.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream("VERSION")) {
            assertNotNull(
                    inputStream,
                    "VERSION must be included in the application resources."
            );

            String expectedVersion = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).strip();
            assertFalse(expectedVersion.isBlank());
            assertEquals(expectedVersion, ApplicationVersion.get());
        }
    }

    @Test
    void doesNotUseDevelopmentFallbackInPackagedBuild() {
        assertFalse(ApplicationVersion.get().isBlank());
        assertFalse("development".equals(ApplicationVersion.get())
        );
    }
}