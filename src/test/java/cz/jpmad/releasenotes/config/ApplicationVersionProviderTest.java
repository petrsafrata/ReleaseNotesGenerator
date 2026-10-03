package cz.jpmad.releasenotes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ApplicationVersionProviderTest {

    private final ApplicationVersionProvider provider = new ApplicationVersionProvider();

    @Test
    void providesPicocliApplicationVersion() {
        assertArrayEquals(new String[]{"Release Notes Generator " + ApplicationVersion.get()},
                provider.getVersion()
        );
    }
}