package com.campusfind.ui.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

public class CloudUploadIcon implements Icon {

    private final int size;
    private final Color color;

    public CloudUploadIcon(int size, Color color) {
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

        // Cloud outline
        Path2D cloud = new Path2D.Float();
        cloud.moveTo(4 * s, 11 * s);
        cloud.curveTo(2 * s, 11 * s, 1.5f * s, 9 * s, 3 * s, 7.5f * s);
        cloud.curveTo(2.5f * s, 5 * s, 5 * s, 3.5f * s, 7.5f * s, 4.5f * s);
        cloud.curveTo(8.5f * s, 2.5f * s, 12 * s, 2.5f * s, 13 * s, 5 * s);
        cloud.curveTo(15 * s, 5.5f * s, 15.5f * s, 8 * s, 14.5f * s, 9.5f * s);
        cloud.curveTo(15 * s, 11 * s, 13.5f * s, 11.5f * s, 12 * s, 11.5f * s);
        g2.draw(cloud);

        // Upload arrow (stem + head)
        g2.drawLine(Math.round(8 * s), Math.round(7 * s), Math.round(8 * s), Math.round(13 * s));
        g2.drawLine(Math.round(8 * s), Math.round(7 * s), Math.round(6 * s), Math.round(9 * s));
        g2.drawLine(Math.round(8 * s), Math.round(7 * s), Math.round(10 * s), Math.round(9 * s));

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
