package com.campusfind.ui.components;

import com.campusfind.ui.icons.HamburgerIcon;
import com.campusfind.ui.icons.UserIcon;
import com.campusfind.ui.pages.NavigationController;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavBar extends JPanel {

    private final NavigationController controller;
    private int unreadNotificationsCount = 0;

    private JPanel leftBrandPanel;
    private JPanel centerLinksPanel;
    private JPanel rightActionsPanel;

    private JButton hamburgerBtn;
    private JButton userBtn;

    public NavBar(NavigationController controller) {
        super(new BorderLayout());
        this.controller = controller;
        setPreferredSize(new Dimension(Theme.WINDOW_DEFAULT.width, Theme.NAVBAR_HEIGHT));
        setBackground(Theme.NAVBAR_BACKGROUND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 32, 0, 32));

        initComponents();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateResponsiveNav(getWidth());
            }
        });
    }

    private void initComponents() {
        // 1. Left: Brand Logo "CampusFind."
        leftBrandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        leftBrandPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("CampusFind");
        logoLabel.setFont(Fonts.serif(22f, Font.BOLD));
        logoLabel.setForeground(Theme.TEXT_PRIMARY);
        logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.navigateToTemuan();
            }
        });

        JLabel dotLabel = new JLabel(".");
        dotLabel.setFont(Fonts.serif(22f, Font.BOLD));
        dotLabel.setForeground(Theme.NUMBER_CIRCLE_BG);

        leftBrandPanel.add(logoLabel);
        leftBrandPanel.add(dotLabel);
        add(leftBrandPanel, BorderLayout.WEST);

        // 2. Center: Regular Navigation Links (visible when >= BREAKPOINT_NAV_COLLAPSE)
        centerLinksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 20));
        centerLinksPanel.setOpaque(false);

        centerLinksPanel.add(createNavLink("Barang Temuan", () -> controller.navigateToTemuan()));
        centerLinksPanel.add(createNavLink("Lapor Barang", () -> controller.navigateToLapor()));
        centerLinksPanel.add(createNavLink("Klaim Barang", () -> controller.navigateToKlaim(null)));
        centerLinksPanel.add(createNavLink("Pos Satpam", () -> controller.navigateToPos()));
        centerLinksPanel.add(createNavLink("Bantuan", () -> controller.navigateToBantuan()));

        add(centerLinksPanel, BorderLayout.CENTER);

        // 3. Right: Hamburger Button (for small screen) + User Profile Button
        rightActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 17));
        rightActionsPanel.setOpaque(false);

        // Hamburger Button
        hamburgerBtn = new JButton(new HamburgerIcon(22, Theme.TEXT_PRIMARY));
        hamburgerBtn.setPreferredSize(new Dimension(36, 32));
        hamburgerBtn.setContentAreaFilled(false);
        hamburgerBtn.setBorderPainted(false);
        hamburgerBtn.setFocusPainted(false);
        hamburgerBtn.setOpaque(false);
        hamburgerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hamburgerBtn.setToolTipText("Menu Navigasi");
        hamburgerBtn.setVisible(false);
        hamburgerBtn.addActionListener(e -> showHamburgerMenu(hamburgerBtn));
        rightActionsPanel.add(hamburgerBtn);

        // User Profile Button (30x30 circle with white user icon)
        userBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PILL_BUTTON_BG);
                g2.fillOval(0, 0, getWidth(), getHeight());
                UserIcon icon = new UserIcon(16, Color.WHITE);
                icon.paintIcon(this, g2, 7, 7);

                // Indicator titik badge notifikasi belum dibaca
                if (unreadNotificationsCount > 0) {
                    g2.setColor(new Color(220, 38, 38));
                    g2.fillOval(getWidth() - 9, 1, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.drawOval(getWidth() - 9, 1, 8, 8);
                }
                g2.dispose();
            }
        };
        userBtn.setPreferredSize(new Dimension(30, 30));
        userBtn.setContentAreaFilled(false);
        userBtn.setBorderPainted(false);
        userBtn.setFocusPainted(false);
        userBtn.setOpaque(false);
        userBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userBtn.setToolTipText("Akun Pengguna");
        userBtn.addActionListener(e -> showUserMenu(userBtn));
        rightActionsPanel.add(userBtn);

        add(rightActionsPanel, BorderLayout.EAST);
    }

    private void updateResponsiveNav(int width) {
        boolean isNarrow = width < Theme.BREAKPOINT_NAV_COLLAPSE;
        centerLinksPanel.setVisible(!isNarrow);
        hamburgerBtn.setVisible(isNarrow);

        int pad = (width < Theme.BREAKPOINT_COMPACT) ? 16 : 32;
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, pad, 0, pad));
        revalidate();
        repaint();
    }

    private void showHamburgerMenu(JButton source) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Theme.INPUT_BACKGROUND);
        popup.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Theme.INPUT_BORDER, 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 6, 8, 6)
        ));

        popup.add(createMenuItem("Barang Temuan", () -> controller.navigateToTemuan()));
        popup.add(createMenuItem("Lapor Barang", () -> controller.navigateToLapor()));
        popup.add(createMenuItem("Klaim Barang", () -> controller.navigateToKlaim(null)));
        popup.add(createMenuItem("Pos Satpam", () -> controller.navigateToPos()));
        popup.add(createMenuItem("Bantuan", () -> controller.navigateToBantuan()));

        popup.addSeparator();
        popup.add(createMenuItem("Klaim Saya", () -> controller.showKlaimSaya()));
        String notifText = "Notifikasi (" + unreadNotificationsCount + ")";
        popup.add(createMenuItem(notifText, () -> controller.showNotifikasi()));

        showAlignedPopup(popup, source);
    }

    private void showUserMenu(JButton source) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Theme.INPUT_BACKGROUND);
        menu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Theme.INPUT_BORDER, 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 6, 8, 6)
        ));

        JMenuItem klaimItem = createMenuItem("Klaim Saya", () -> controller.showKlaimSaya());
        menu.add(klaimItem);

        String notifText = "Notifikasi (" + unreadNotificationsCount + " belum dibaca)";
        JMenuItem notifItem = createMenuItem(notifText, () -> controller.showNotifikasi());
        menu.add(notifItem);

        showAlignedPopup(menu, source);
    }

    private void showAlignedPopup(JPopupMenu popup, JButton source) {
        Dimension pref = popup.getPreferredSize();
        int popupWidth = Math.max(200, pref.width);
        popup.setPreferredSize(new Dimension(popupWidth, pref.height));

        // Align right edge of popup with right edge of button
        int x = source.getWidth() - popupWidth;
        int y = source.getHeight() + 4;

        // Ensure popup does not overflow window boundary
        Window window = SwingUtilities.getWindowAncestor(source);
        if (window != null && source.isShowing()) {
            Point sourceLocation = source.getLocationOnScreen();
            Point windowLocation = window.getLocationOnScreen();
            int screenRight = windowLocation.x + window.getWidth();

            if (sourceLocation.x + x + popupWidth > screenRight - 10) {
                x = (screenRight - 10) - (sourceLocation.x + popupWidth);
            }
            if (sourceLocation.x + x < windowLocation.x + 10) {
                x = (windowLocation.x + 10) - sourceLocation.x;
            }
        }

        popup.show(source, x, y);
    }

    private JMenuItem createMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Fonts.sans(13.5f, Font.PLAIN));
        item.setForeground(Theme.TEXT_PRIMARY);
        item.setBackground(Theme.INPUT_BACKGROUND);
        item.setHorizontalAlignment(SwingConstants.CENTER); // Centered text
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(9, 20, 9, 20));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addActionListener(e -> action.run());
        return item;
    }

    public void setUnreadNotificationsCount(int count) {
        this.unreadNotificationsCount = count;
        if (userBtn != null) {
            userBtn.repaint();
        }
        repaint();
    }

    private JLabel createNavLink(String text, Runnable action) {
        JLabel link = new JLabel(text);
        link.setFont(Fonts.sans(13f, Font.PLAIN));
        link.setForeground(Theme.NAVBAR_LINK);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                link.setForeground(Theme.NAVBAR_LINK_HOVER);
                link.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                link.setForeground(Theme.NAVBAR_LINK);
                link.setText(text);
            }
        });
        return link;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Bottom border line 1px #DAD6D0
        g.setColor(Theme.NAVBAR_BORDER);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }
}
