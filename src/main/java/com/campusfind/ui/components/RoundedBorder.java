package com.campusfind.ui.components;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

public class RoundedBorder extends AbstractBorder {

    private final Color color;
    private final int radius;
    private final float thickness;

    public RoundedBorder(Color color, int radius, float thickness) {
        this.color = color;
        this.radius = radius;
        this.thickness = thickness;
    }

    public RoundedBorder(Color color, int radius) {
        this(color, radius, 1.0f);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        float pad = thickness / 2.0f;
        g2.drawRoundRect(
                Math.round(x + pad),
                Math.round(y + pad),
                Math.round(width - thickness),
                Math.round(height - thickness),
                radius, radius
        );
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int pad = Math.round(thickness) + 4;
        return new Insets(pad, pad, pad, pad);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        int pad = Math.round(thickness) + 4;
        insets.left = insets.top = insets.right = insets.bottom = pad;
        return insets;
    }
}
