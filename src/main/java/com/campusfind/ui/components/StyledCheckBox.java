package com.campusfind.ui.components;

import com.campusfind.ui.icons.CheckIcon;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JCheckBox;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class StyledCheckBox extends JCheckBox {

    private static final int BOX_SIZE = 14;
    private static final int BOX_RADIUS = 3;
    private boolean hasError = false;

    public StyledCheckBox(String text) {
        super("<html><body style='width: 320px; font-family: sans-serif;'>" + text + "</body></html>");
        setFont(Fonts.sans(12f, Font.PLAIN));
        setForeground(Theme.TEXT_PRIMARY);
        setOpaque(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setIconTextGap(10);

        // Custom icon for unchecked state
        setIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Theme.INPUT_BACKGROUND);
                g2.fillRoundRect(x, y + 2, BOX_SIZE, BOX_SIZE, BOX_RADIUS, BOX_RADIUS);

                if (hasError) {
                    g2.setColor(Theme.ERROR_COLOR);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(Theme.INPUT_BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                }
                g2.drawRoundRect(x, y + 2, BOX_SIZE, BOX_SIZE, BOX_RADIUS, BOX_RADIUS);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return BOX_SIZE;
            }

            @Override
            public int getIconHeight() {
                return BOX_SIZE + 4;
            }
        });

        // Custom icon for checked state
        setSelectedIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Theme.BUTTON_DARK_BG);
                g2.fillRoundRect(x, y + 2, BOX_SIZE, BOX_SIZE, BOX_RADIUS, BOX_RADIUS);

                CheckIcon check = new CheckIcon(12, Color.WHITE);
                check.paintIcon(c, g2, x + 1, y + 3);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return BOX_SIZE;
            }

            @Override
            public int getIconHeight() {
                return BOX_SIZE + 4;
            }
        });
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        repaint();
    }
}
