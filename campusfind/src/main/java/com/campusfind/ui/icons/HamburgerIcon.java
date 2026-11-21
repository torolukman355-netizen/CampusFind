package com.campusfind.ui.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class HamburgerIcon implements Icon {

    private final int size;
    private final Color color;

    public HamburgerIcon(int size, Color color) {
        this.size = size;
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.translate(x, y);

        float s = size / 20.0f;
        g2.setStroke(new BasicStroke(2.0f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // 3 horizontal bars
        int left = Math.round(3 * s);
        int right = Math.round(17 * s);
        int y1 = Math.round(5 * s);
        int y2 = Math.round(10 * s);
        int y3 = Math.round(15 * s);

        g2.drawLine(left, y1, right, y1);
        g2.drawLine(left, y2, right, y2);
        g2.drawLine(left, y3, right, y3);

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
