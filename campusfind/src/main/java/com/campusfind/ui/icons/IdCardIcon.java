package com.campusfind.ui.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class IdCardIcon implements Icon {

    private final int size;
    private final Color color;

    public IdCardIcon(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.translate(x, y);

        float s = size / 16.0f;
        g2.setStroke(new BasicStroke(1.2f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Outer card rectangle
        g2.drawRoundRect(Math.round(1 * s), Math.round(2 * s), Math.round(14 * s), Math.round(11 * s), Math.round(2 * s), Math.round(2 * s));

        // Photo box or user silhouette inside card
        g2.drawRoundRect(Math.round(3 * s), Math.round(4.5f * s), Math.round(4 * s), Math.round(4 * s), 1, 1);

        // Lines on the right
        g2.drawLine(Math.round(9 * s), Math.round(5.5f * s), Math.round(13 * s), Math.round(5.5f * s));
        g2.drawLine(Math.round(9 * s), Math.round(8 * s), Math.round(13 * s), Math.round(8 * s));

        // Bottom lanyard clip / slot
        g2.drawLine(Math.round(5 * s), Math.round(10.5f * s), Math.round(11 * s), Math.round(10.5f * s));

        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
