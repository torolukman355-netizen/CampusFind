package com.campusfind.ui.components;

import com.campusfind.model.HakAkses;
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

        centerLinksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 20));
        centerLinksPanel.setOpaque(false);
        rebuildCenterLinks();

        add(centerLinksPanel, BorderLayout.CENTER);

        rightActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 17));
        rightActionsPanel.setOpaque(false);

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

        userBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PILL_BUTTON_BG);
                g2.fillOval(0, 0, getWidth(), getHeight());
                UserIcon icon = new UserIcon(16, Color.WHITE);
                icon.paintIcon(this, g2, 7, 7);

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

        updateUserTooltip();

        add(rightActionsPanel, BorderLayout.EAST);
    }

    /**
     * Dipanggil setelah login/logout agar menu mengikuti role pengguna.
     */
    public void refreshMenu() {
        rebuildCenterLinks();
        updateUserTooltip();
    }

    private void rebuildCenterLinks() {
        centerLinksPanel.removeAll();

        centerLinksPanel.add(createNavLink("Galeri Barang", () -> controller.navigateToTemuan()));
        if (bolehAtauBelumLogin(HakAkses.BUAT_LAPORAN)) {
            centerLinksPanel.add(createNavLink("Lapor Barang", () -> controller.navigateToLapor()));
        }
        if (bolehAtauBelumLogin(HakAkses.AJUKAN_KLAIM)) {
            centerLinksPanel.add(createNavLink("Klaim Barang", () -> controller.navigateToKlaim(null)));
        }
        centerLinksPanel.add(createNavLink("Pos Satpam", () -> controller.navigateToPos()));
        centerLinksPanel.add(createNavLink("Bantuan", () -> controller.navigateToBantuan()));

        centerLinksPanel.revalidate();
        centerLinksPanel.repaint();
    }

    private void updateUserTooltip() {
        if (userBtn == null) {
            return;
        }
        userBtn.setToolTipText(controller.isLoggedIn()
                ? "Akun: " + controller.getNamaSesi() + " (" + controller.getRoleSesi().getLabel() + ")"
                : "Akun Pengguna (belum login)");
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
        JPopupMenu popup = newPopup();

        popup.add(createMenuItem("Galeri Barang", () -> controller.navigateToTemuan()));
        if (bolehAtauBelumLogin(HakAkses.BUAT_LAPORAN)) {
            popup.add(createMenuItem("Lapor Barang", () -> controller.navigateToLapor()));
        }
        if (bolehAtauBelumLogin(HakAkses.AJUKAN_KLAIM)) {
            popup.add(createMenuItem("Klaim Barang", () -> controller.navigateToKlaim(null)));
        }
        popup.add(createMenuItem("Pos Satpam", () -> controller.navigateToPos()));
        popup.add(createMenuItem("Bantuan", () -> controller.navigateToBantuan()));

        popup.addSeparator();
        if (bolehAtauBelumLogin(HakAkses.LIHAT_KLAIM_SENDIRI)) {
            popup.add(createMenuItem("Klaim Saya", () -> controller.showKlaimSaya()));
        }
        String notifText = "Notifikasi (" + unreadNotificationsCount + ")";
        popup.add(createMenuItem(notifText, () -> controller.showNotifikasi()));

        if (bolehAtauBelumLogin(HakAkses.VERIFIKASI_LAPORAN)) {
            popup.add(createMenuItem("Dashboard Petugas", () -> controller.navigateToPetugas()));
        }
        if (bolehAtauBelumLogin(HakAkses.LIHAT_STATISTIK)) {
            popup.add(createMenuItem("Dashboard Admin", () -> controller.navigateToAdmin()));
        }

        popup.addSeparator();
        popup.add(createMenuItem(
                controller.isLoggedIn() ? "Ganti Akun / Logout" : "Masuk",
                controller.isLoggedIn() ? controller::logout : controller::bukaLogin));

        showAlignedPopup(popup, source);
    }

    private void showUserMenu(JButton source) {
        JPopupMenu menu = newPopup();

        if (controller.isLoggedIn()) {
            menu.add(createMenuItem("Akun: " + controller.getNamaSesi()
                    + " (" + controller.getRoleSesi().getLabel() + ")", () -> {
            }, false));
            menu.addSeparator();
        }

        if (bolehAtauBelumLogin(HakAkses.LIHAT_KLAIM_SENDIRI)) {
            menu.add(createMenuItem("Klaim Saya", () -> controller.showKlaimSaya()));
        }

        String notifText = "Notifikasi (" + unreadNotificationsCount + " belum dibaca)";
        menu.add(createMenuItem(notifText, () -> controller.showNotifikasi()));

        boolean adaMenuRole = bolehAtauBelumLogin(HakAkses.VERIFIKASI_LAPORAN)
                || bolehAtauBelumLogin(HakAkses.LIHAT_STATISTIK);
        if (adaMenuRole) {
            menu.addSeparator();
        }
        if (bolehAtauBelumLogin(HakAkses.VERIFIKASI_LAPORAN)) {
            menu.add(createMenuItem("Dashboard Petugas", () -> controller.navigateToPetugas()));
        }
        if (bolehAtauBelumLogin(HakAkses.LIHAT_STATISTIK)) {
            menu.add(createMenuItem("Dashboard Admin", () -> controller.navigateToAdmin()));
        }

        menu.addSeparator();
        menu.add(createMenuItem(
                controller.isLoggedIn() ? "Ganti Akun / Logout" : "Masuk",
                controller.isLoggedIn() ? controller::logout : controller::bukaLogin));

        showAlignedPopup(menu, source);
    }

    /**
     * Menu ditampilkan bila pengguna belum login (agar dialog login muncul)
     * atau bila role-nya memang memiliki hak akses tersebut.
     */
    private boolean bolehAtauBelumLogin(HakAkses hakAkses) {
        return !controller.isLoggedIn() || controller.boleh(hakAkses);
    }

    private JPopupMenu newPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Theme.INPUT_BACKGROUND);
        popup.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Theme.INPUT_BORDER, 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 6, 8, 6)
        ));
        return popup;
    }

    private void showAlignedPopup(JPopupMenu popup, JButton source) {
        Dimension pref = popup.getPreferredSize();
        int popupWidth = Math.max(210, pref.width);
        popup.setPreferredSize(new Dimension(popupWidth, pref.height));

        int x = source.getWidth() - popupWidth;
        int y = source.getHeight() + 4;

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
        return createMenuItem(text, action, true);
    }

    private JMenuItem createMenuItem(String text, Runnable action, boolean enabled) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(Fonts.sans(13.5f, Font.PLAIN));
        item.setForeground(enabled ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
        item.setBackground(Theme.INPUT_BACKGROUND);
        item.setHorizontalAlignment(SwingConstants.CENTER);
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(9, 20, 9, 20));
        item.setEnabled(enabled);
        if (enabled) {
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.addActionListener(e -> action.run());
        }
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
        g.setColor(Theme.NAVBAR_BORDER);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }
}