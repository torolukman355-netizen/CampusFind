package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JTextArea;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextArea extends JTextArea {

    private String placeholder;
    private boolean hasError = false;

    public PlaceholderTextArea(String placeholder) {
        this.placeholder = placeholder;
        init();
    }

    public PlaceholderTextArea() {
        this("");
    }

    private void init() {
        setFont(Fonts.sans(13f, Font.PLAIN));
        setForeground(Theme.TEXT_PRIMARY);
        setBackground(Theme.INPUT_BACKGROUND);
        setLineWrap(true);
        setWrapStyleWord(true);
        setMargin(new Insets(12, 12, 12, 12));
        setOpaque(false);

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        repaint();
    }

    public boolean isHasError() {
        return hasError;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(Theme.INPUT_BACKGROUND);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);

        // Draw border
        if (hasError) {
            g2.setColor(Theme.ERROR_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        } else if (hasFocus()) {
            g2.setColor(Theme.INPUT_FOCUS_BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        } else {
            g2.setColor(Theme.INPUT_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        }

        g2.dispose();
        super.paintComponent(g);

        // Placeholder
        if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
            Graphics2D gPlaceholder = (Graphics2D) g.create();
            gPlaceholder.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gPlaceholder.setColor(Theme.TEXT_PLACEHOLDER);
            gPlaceholder.setFont(getFont());
            Insets insets = getInsets();
            int x = insets.left;
            int y = insets.top + gPlaceholder.getFontMetrics().getAscent();

            // Wrap placeholder if too long
            int maxWidth = getWidth() - insets.left - insets.right;
            if (maxWidth > 50) {
                String[] words = placeholder.split(" ");
                StringBuilder currentLine = new StringBuilder();
                int lineHeight = gPlaceholder.getFontMetrics().getHeight();

                for (String word : words) {
                    if (gPlaceholder.getFontMetrics().stringWidth(currentLine + " " + word) > maxWidth) {
                        gPlaceholder.drawString(currentLine.toString(), x, y);
                        y += lineHeight;
                        currentLine = new StringBuilder(word);
                    } else {
                        if (currentLine.length() > 0) currentLine.append(" ");
                        currentLine.append(word);
                    }
                }
                if (currentLine.length() > 0) {
                    gPlaceholder.drawString(currentLine.toString(), x, y);
                }
            } else {
                gPlaceholder.drawString(placeholder, x, y);
            }

            gPlaceholder.dispose();
        }
    }
}
