package com.campusfind.ui.components;

import com.campusfind.ui.icons.ChevronDownIcon;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

public class StyledComboBox<E> extends JComboBox<E> {

    private boolean hasError = false;

    public StyledComboBox() {
        super();
        init();
    }

    public StyledComboBox(E[] items) {
        super(items);
        init();
    }

    private void init() {
        setFont(Fonts.sans(13f, Font.PLAIN));
        setForeground(Theme.TEXT_PRIMARY);
        setBackground(Theme.INPUT_BACKGROUND);
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(getPreferredSize().width, Theme.INPUT_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.INPUT_HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(Fonts.sans(13f, Font.PLAIN));
                if (isSelected) {
                    c.setBackground(Theme.CARD_BORDER);
                    c.setForeground(Theme.TEXT_PRIMARY);
                } else {
                    c.setBackground(Theme.INPUT_BACKGROUND);
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
                return c;
            }
        });

        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton(new ChevronDownIcon(14, Theme.TEXT_SECONDARY));
                btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 12));
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                btn.setOpaque(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return btn;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
                // Handled in component paint
            }

            @Override
            protected Insets getInsets() {
                return new Insets(0, 12, 0, 32);
            }
        });
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        repaint();
    }

    public boolean isHasError() {
        return hasError;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(Theme.INPUT_BACKGROUND);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);

        // Draw border
        if (hasError) {
            g2.setColor(Theme.ERROR_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        } else if (hasFocus()) {
            g2.setColor(Theme.INPUT_FOCUS_BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        } else {
            g2.setColor(Theme.INPUT_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
