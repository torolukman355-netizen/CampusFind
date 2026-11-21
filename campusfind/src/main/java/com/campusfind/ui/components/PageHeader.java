package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Font;

/**
 * Reusable header component containing Eyebrow, Title, and Subtitle.
 * All elements are strictly centered and adapt responsively to container width.
 */
public class PageHeader extends JPanel {

    private final JLabel eyebrowLabel;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final String rawSubtitle;
    private final int maxWrapWidth;
    private int lastWidth = -1;

    public PageHeader(String eyebrow, String title, String subtitleHtmlOrText, int maxWrapWidth) {
        this.rawSubtitle = subtitleHtmlOrText;
        this.maxWrapWidth = maxWrapWidth;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        // Top spacing 32px
        add(Box.createVerticalStrut(32));

        // 1. Eyebrow
        eyebrowLabel = new JLabel(eyebrow);
        eyebrowLabel.setFont(Fonts.trackedSans(11f, Font.BOLD, 0.14));
        eyebrowLabel.setForeground(Theme.EYEBROW_TEXT);
        eyebrowLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        eyebrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(eyebrowLabel);

        add(Box.createVerticalStrut(14));

        // 2. Title (serif)
        titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.serif(46f, Font.BOLD));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel);

        add(Box.createVerticalStrut(10));

        // 3. Subtitle (multiline centered)
        subtitleLabel = new JLabel();
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(subtitleLabel);

        updateWidth(Theme.MAX_CONTENT_WIDTH);
    }

    public PageHeader(String eyebrow, String title, String subtitleHtmlOrText) {
        this(eyebrow, title, subtitleHtmlOrText, 660);
    }

    public void updateWidth(int contentWidth) {
        if (contentWidth <= 0 || contentWidth == lastWidth) return;
        lastWidth = contentWidth;

        // Font scaling: 46pt -> 34pt (< 650) -> 28pt (< 480)
        if (contentWidth < Theme.BREAKPOINT_TINY) {
            titleLabel.setFont(Fonts.serif(28f, Font.BOLD));
        } else if (contentWidth < Theme.BREAKPOINT_COMPACT) {
            titleLabel.setFont(Fonts.serif(34f, Font.BOLD));
        } else {
            titleLabel.setFont(Fonts.serif(46f, Font.BOLD));
        }

        // Subtitle dynamic width calculation (max maxWrapWidth, at least 220px)
        // In Swing HTML 3.2, <center><table width="..."><tr><td align="center">...</td></tr></table></center>
        // ensures the text block is mathematically centered in the container, avoiding left-offset bugs of <div>
        int availableWrap = Math.min(maxWrapWidth, Math.max(220, contentWidth - 24));
        String formattedHtml = String.format(
                "<html><body style='margin:0;padding:0;'><center><table width='%d' cellpadding='0' cellspacing='0'><tr><td align='center' style='font-family:sans-serif;font-size:14.5px;color:#5E554C;line-height:1.45;text-align:center;'>%s</td></tr></table></center></body></html>",
                availableWrap,
                rawSubtitle
        );
        subtitleLabel.setText(formattedHtml);

        revalidate();
    }
}
