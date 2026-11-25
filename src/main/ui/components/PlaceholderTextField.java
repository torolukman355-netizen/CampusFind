package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.Icon;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class PlaceholderTextField extends JTextField {

    private String placeholder;
    private Icon trailingIcon;
    private boolean hasError = false;

    public PlaceholderTextField(String placeholder, Icon trailingIcon) {
        this.placeholder = placeholder;
        this.trailingIcon = trailingIcon;
        init();
    }

    public PlaceholderTextField(String placeholder) {
        this(placeholder, null);
    }

    public PlaceholderTextField() {
        this("", null);
    }

    private void init() {
        setFont(Fonts.sans(13f, Font.PLAIN));
        setForeground(Theme.TEXT_PRIMARY);
        setBackground(Theme.INPUT_BACKGROUND);
        setOpaque(false);
        setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(getPreferredSize().width, Theme.INPUT_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.INPUT_HEIGHT));

        int rightPad = (trailingIcon != null) ? trailingIcon.getIconWidth() + 18 : 12;
        setMargin(new Insets(8, 12, 8, rightPad));

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

    public void setTrailingIcon(Icon trailingIcon) {
        this.trailingIcon = trailingIcon;
        int rightPad = (trailingIcon != null) ? trailingIcon.getIconWidth() + 18 : 12;
        setMargin(new Insets(8, 12, 8, rightPad));
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

        // Draw trailing icon if present
        if (trailingIcon != null) {
            int iconX = getWidth() - trailingIcon.getIconWidth() - 12;
            int iconY = (getHeight() - trailingIcon.getIconHeight()) / 2;
            trailingIcon.paintIcon(this, g2, iconX, iconY);
        }

        g2.dispose();
        super.paintComponent(g);

        // Draw placeholder if text is empty and not focused
        if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
            Graphics2D gPlaceholder = (Graphics2D) g.create();
            gPlaceholder.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gPlaceholder.setColor(Theme.TEXT_PLACEHOLDER);
            gPlaceholder.setFont(getFont());
            Insets insets = getInsets();
            int y = (getHeight() - gPlaceholder.getFontMetrics().getHeight()) / 2 + gPlaceholder.getFontMetrics().getAscent();
            gPlaceholder.drawString(placeholder, insets.left, y);
            gPlaceholder.dispose();
        }
    }
}
