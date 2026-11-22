package com.campusfind.ui.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public class UserIcon implements Icon {

    private final int size;
    private final Color color;

    public UserIcon(int size, Color color) {
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
        g2.setStroke(new BasicStroke(1.3f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Head circle
        float headRadius = 2.8f * s;
        g2.drawOval(Math.round(8 * s - headRadius), Math.round(5 * s - headRadius), Math.round(2 * headRadius), Math.round(2 * headRadius));

        // Shoulders arc
        Path2D shoulders = new Path2D.Float();
        shoulders.moveTo(3.5f * s, 14 * s);
        shoulders.curveTo(3.5f * s, 10.5f * s, 5.5f * s, 9.5f * s, 8 * s, 9.5f * s);
        shoulders.curveTo(10.5f * s, 9.5f * s, 12.5f * s, 10.5f * s, 12.5f * s, 14 * s);
        g2.draw(shoulders);

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
