package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FlatButton extends JButton {

    private int radius = 4;

    public FlatButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public FlatButton(String text) {
        this(text, null);
    }

    private void init() {
        setFont(Fonts.trackedSans(11.5f, Font.BOLD, 0.12));
        setForeground(java.awt.Color.WHITE);
        setBackground(Theme.BUTTON_DARK_BG);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        // Enter key triggers action
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && isEnabled()) {
                    doClick();
                }
            }
        });
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!isEnabled()) {
            g2.setColor(Theme.BUTTON_DARK_DISABLED);
        } else if (getModel().isPressed()) {
            g2.setColor(Theme.BUTTON_DARK_PRESSED);
        } else if (getModel().isRollover()) {
            g2.setColor(Theme.BUTTON_DARK_HOVER);
        } else {
            g2.setColor(Theme.BUTTON_DARK_BG);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();

        super.paintComponent(g);
    }
}
