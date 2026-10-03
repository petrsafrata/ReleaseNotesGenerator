package cz.jpmad.releasenotes.tui;

import cz.jpmad.releasenotes.config.ApplicationVersion;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.ArrayList;
import java.util.List;

public class TuiHeader {

    private static final AttributedStyle ICON_STYLE = AttributedStyle.DEFAULT
            .bold()
            .foreground(AttributedStyle.MAGENTA);

    private static final AttributedStyle TITLE_STYLE = AttributedStyle.DEFAULT
            .bold()
            .foreground(AttributedStyle.CYAN);

    private static final AttributedStyle SUBTITLE_STYLE = AttributedStyle.DEFAULT
            .foreground(AttributedStyle.MAGENTA);

    private static final AttributedStyle MUTED_STYLE = AttributedStyle.DEFAULT
            .faint();

    public List<AttributedString> create() {
        List<AttributedString> header = new ArrayList<>();
        header.add(createLine(
                        "    ▄▄▄▄▄▄▄    ",
                        "",
                        AttributedStyle.DEFAULT
                )
        );
        header.add(createLine(
                        "    █ ▄ ▄ █    ",
                        "Welcome to",
                        AttributedStyle.DEFAULT
                )
        );
        header.add(createLine(
                        "    █  ▄  █    ",
                        "RELEASE NOTES GENERATOR",
                        TITLE_STYLE
                )
        );
        header.add(createLine(
                        "    █ ███ █    ",
                        "Git  •  GitHub  •  Local AI",
                        SUBTITLE_STYLE
                )
        );
        header.add(createLine(
                        "    ▀█▄▄▄█▀    ",
                        "Version " + ApplicationVersion.get(),
                        MUTED_STYLE
                )
        );
        header.add(createLine(
                        "      ▀ ▀      ",
                        "",
                        AttributedStyle.DEFAULT
                )
        );
        header.add(new AttributedString(""));
        return header;
    }

    public void print(Terminal terminal) {
        for (AttributedString line : create()) {
            terminal.writer().println(
                    line.toAnsi(terminal)
            );
        }

        terminal.writer().flush();
    }

    private AttributedString createLine(String icon, String text, AttributedStyle textStyle) {
        return new AttributedStringBuilder()
                .style(ICON_STYLE)
                .append(icon)
                .style(textStyle)
                .append(text)
                .style(AttributedStyle.DEFAULT)
                .toAttributedString();
    }
}