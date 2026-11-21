package com.campusfind.ui.icons;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ChevronDownIcon implements Icon {

    private final int size;
    private final Color color;

    public ChevronDownIcon(int size, Color color) {
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
        g2.setStroke(new BasicStroke(1.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.drawLine(Math.round(4 * s), Math.round(6 * s), Math.round(8 * s), Math.round(10 * s));
        g2.drawLine(Math.round(8 * s), Math.round(10 * s), Math.round(12 * s), Math.round(6 * s));

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
