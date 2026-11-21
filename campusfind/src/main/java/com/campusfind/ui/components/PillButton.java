package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PillButton extends JButton {

    public PillButton(String text) {
        super(text);
        setFont(Fonts.sans(12f, Font.BOLD));
        setForeground(Color.WHITE);
        setBackground(Theme.PILL_BUTTON_BG);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(164, 30));
        setMargin(new Insets(4, 16, 4, 16));

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && isEnabled()) {
                    doClick();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int height = getHeight();
        int width = getWidth();

        if (!isEnabled()) {
            g2.setColor(Theme.BUTTON_DARK_DISABLED);
        } else if (getModel().isPressed()) {
            g2.setColor(Theme.BUTTON_DARK_PRESSED);
        } else if (getModel().isRollover()) {
            g2.setColor(Theme.BUTTON_DARK_HOVER);
        } else {
            g2.setColor(Theme.PILL_BUTTON_BG);
        }

        // Pill shape: corner radius is height
        g2.fillRoundRect(0, 0, width, height, height, height);
        g2.dispose();

        super.paintComponent(g);
    }
}
