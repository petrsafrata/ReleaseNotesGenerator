package cz.jpmad.releasenotes.tui;

import cz.jpmad.releasenotes.config.ApplicationVersion;
import org.jline.utils.AttributedString;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TuiHeaderTest {

    private final TuiHeader tuiHeader = new TuiHeader();

    @Test
    void containsApplicationInformation() {
        List<AttributedString> header = tuiHeader.create();

        String plainText = String.join(
                System.lineSeparator(),
                header.stream()
                        .map(AttributedString::toString)
                        .toList()
        );

        assertTrue(plainText.contains("Welcome to"));
        assertTrue(plainText.contains("RELEASE NOTES GENERATOR"));
        assertTrue(plainText.contains("Version " + ApplicationVersion.get()));
        assertTrue(plainText.contains("Git  •  GitHub  •  Local AI"));
    }

    @Test
    void createsMutableHeaderForConsolePrompt() {
        List<AttributedString> header = tuiHeader.create();
        assertDoesNotThrow(() -> header.add(new AttributedString("Additional screen content")));
    }
}