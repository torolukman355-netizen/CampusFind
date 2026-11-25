package com.campusfind.ui.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public class ShieldIcon implements Icon {

    private final int size;
    private final Color color;

    public ShieldIcon(int size, Color color) {
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

        Path2D shield = new Path2D.Float();
        shield.moveTo(8 * s, 1.5f * s);
        shield.lineTo(13.5f * s, 3.5f * s);
        shield.curveTo(13.5f * s, 9 * s, 11 * s, 12.5f * s, 8 * s, 14.5f * s);
        shield.curveTo(5 * s, 12.5f * s, 2.5f * s, 9 * s, 2.5f * s, 3.5f * s);
        shield.closePath();
        g2.draw(shield);

        // Inner tick or crest
        Path2D check = new Path2D.Float();
        check.moveTo(5.5f * s, 8 * s);
        check.lineTo(7.5f * s, 10 * s);
        check.lineTo(11 * s, 6 * s);
        g2.draw(check);

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
