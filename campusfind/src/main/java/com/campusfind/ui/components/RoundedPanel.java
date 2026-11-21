package com.campusfind.ui.components;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {

    private Color backgroundColor;
    private Color borderColor;
    private int radius;
    private float strokeWidth = 1.0f;

    public RoundedPanel(LayoutManager layout, Color backgroundColor, Color borderColor, int radius) {
        super(layout);
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.radius = radius;
        setOpaque(false);
    }

    public RoundedPanel(Color backgroundColor, Color borderColor, int radius) {
        this(null, backgroundColor, borderColor, radius);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        if (backgroundColor != null) {
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, width, height, radius, radius);
        }

        // Border
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(strokeWidth));
            float offset = strokeWidth / 2.0f;
            g2.drawRoundRect(
                    Math.round(offset),
                    Math.round(offset),
                    Math.round(width - strokeWidth),
                    Math.round(height - strokeWidth),
                    radius, radius
            );
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
