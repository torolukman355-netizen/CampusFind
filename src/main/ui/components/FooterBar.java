package com.campusfind.ui.components;

import com.campusfind.ui.pages.NavigationController;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FooterBar extends JPanel {

    private final NavigationController controller;
    private JPanel leftBrandPanel;
    private JPanel rightLinksPanel;
    private boolean isStacked = false;

    public FooterBar(NavigationController controller) {
        super(new BorderLayout());
        this.controller = controller;
        setOpaque(false);
        setAlignmentX(Component.CENTER_ALIGNMENT);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 0, 28, 0));

        initComponents();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateResponsive(getWidth());
            }
        });
    }

    private void initComponents() {
        leftBrandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftBrandPanel.setOpaque(false);

        JLabel brand = new JLabel("CampusFind");
        brand.setFont(Fonts.sans(14f, Font.BOLD));
        brand.setForeground(Theme.TEXT_PRIMARY);

        JLabel desc = new JLabel(" — Layanan Rekonsiliasi & Kehilangan Kampus");
        desc.setFont(Fonts.sans(11.5f, Font.PLAIN));
        desc.setForeground(Theme.TEXT_MUTED);

        leftBrandPanel.add(brand);
        leftBrandPanel.add(desc);

        rightLinksPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightLinksPanel.setOpaque(false);

        rightLinksPanel.add(createFooterLink("Panduan Restitusi", () -> controller.navigateToBantuan()));
        rightLinksPanel.add(createSpacing(24));
        rightLinksPanel.add(createFooterLink("Daftar Pos Jaga", () -> controller.navigateToPos()));
        rightLinksPanel.add(createSpacing(24));
        rightLinksPanel.add(createFooterLink("Privasi", () -> controller.showPrivasiDialog()));
        rightLinksPanel.add(createSpacing(24));
        rightLinksPanel.add(createFooterLink("Kontak Darurat", () -> controller.showKontakDaruratDialog()));

        add(leftBrandPanel, BorderLayout.WEST);
        add(rightLinksPanel, BorderLayout.EAST);
    }

    public void updateResponsive(int width) {
        if (width <= 0) return;
        boolean shouldStack = width < 760;

        if (shouldStack != isStacked) {
            isStacked = shouldStack;
            removeAll();

            if (isStacked) {
                setLayout(new java.awt.GridLayout(2, 1, 0, 10));
                leftBrandPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                rightLinksPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 4));
                add(leftBrandPanel);
                add(rightLinksPanel);
            } else {
                setLayout(new BorderLayout());
                leftBrandPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                rightLinksPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                add(leftBrandPanel, BorderLayout.WEST);
                add(rightLinksPanel, BorderLayout.EAST);
            }

            revalidate();
            repaint();
        }
    }

    private JLabel createFooterLink(String text, Runnable action) {
        JLabel link = new JLabel(text);
        link.setFont(Fonts.sans(11.5f, Font.PLAIN));
        link.setForeground(Theme.TEXT_SECONDARY);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                link.setForeground(Theme.TEXT_PRIMARY);
                link.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                link.setForeground(Theme.TEXT_SECONDARY);
                link.setText(text);
            }
        });
        return link;
    }

    private JPanel createSpacing(int width) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(width, 1));
        return p;
    }
}
