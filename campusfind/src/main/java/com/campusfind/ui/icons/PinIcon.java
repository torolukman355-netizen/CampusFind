package com.campusfind.ui.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public class PinIcon implements Icon {

    private final int size;
    private final Color color;

    public PinIcon(int size, Color color) {
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

        // Location pin outline
        Path2D pin = new Path2D.Float();
        pin.moveTo(8 * s, 14.5f * s);
        pin.curveTo(4.5f * s, 10 * s, 3.5f * s, 7.5f * s, 3.5f * s, 5.5f * s);
        pin.curveTo(3.5f * s, 3 * s, 5.5f * s, 1.5f * s, 8 * s, 1.5f * s);
        pin.curveTo(10.5f * s, 1.5f * s, 12.5f * s, 3 * s, 12.5f * s, 5.5f * s);
        pin.curveTo(12.5f * s, 7.5f * s, 11.5f * s, 10 * s, 8 * s, 14.5f * s);
        pin.closePath();
        g2.draw(pin);

        // Inner circle
        float r = 1.8f * s;
        g2.drawOval(Math.round(8 * s - r), Math.round(5.5f * s - r), Math.round(2 * r), Math.round(2 * r));

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
