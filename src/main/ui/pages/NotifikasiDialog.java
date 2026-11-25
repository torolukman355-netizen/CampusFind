package com.campusfind.ui.pages;

import com.campusfind.model.Notification;
import com.campusfind.model.NotificationType;
import com.campusfind.service.AppSession;
import com.campusfind.service.NotificationService;
import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotifikasiDialog extends JDialog {

    private final NotificationService notificationService;
    private final AppSession appSession;
    private final NavigationController controller;

    private JLabel lblHeaderCount;
    private JPanel listContainer;
    private FlatButton markAllBtn;

    public NotifikasiDialog(JFrame parent,
                            NotificationService notificationService,
                            AppSession appSession,
                            NavigationController controller) {
        super(parent, "Pemberitahuan & Notifikasi", true);
        this.notificationService = notificationService;
        this.appSession = appSession;
        this.controller = controller;

        setSize(680, 520);
        setLocationRelativeTo(parent);
        initView();
        loadNotifikasi();
    }

    public NotifikasiDialog(JFrame parent,
                            NotificationService notificationService,
                            AppSession appSession) {
        this(parent, notificationService, appSession, null);
    }

    private void initView() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(Theme.PAGE_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout(8, 4));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Pemberitahuan Saya");
        title.setFont(Fonts.sans(20f, Font.BOLD));
        title.setForeground(Theme.TEXT_PRIMARY);

        lblHeaderCount = new JLabel();
        lblHeaderCount.setFont(Fonts.sans(12.5f, Font.BOLD));

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(lblHeaderCount, BorderLayout.EAST);
        content.add(headerPanel, BorderLayout.NORTH);

        // List Scroll Container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        content.add(scrollPane, BorderLayout.CENTER);

        // Footer Section
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        markAllBtn = new FlatButton("TANDAI SEMUA SUDAH DIBACA");
        markAllBtn.addActionListener(e -> {
            notificationService.markAllAsRead(appSession.getNomorIdentitas());
            if (controller != null) {
                controller.updateNotificationCount();
            }
            loadNotifikasi();
        });

        FlatButton tutupBtn = new FlatButton("TUTUP");
        tutupBtn.addActionListener(e -> dispose());

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(markAllBtn);
        rightBtns.add(tutupBtn);

        footer.add(rightBtns, BorderLayout.EAST);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void loadNotifikasi() {
        listContainer.removeAll();
        String nomorIdentitas = appSession.getNomorIdentitas();
        List<Notification> list = notificationService.findByPenerima(nomorIdentitas);
        int unreadCount = notificationService.countBelumDibaca(nomorIdentitas);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Update header counter
        if (unreadCount > 0) {
            lblHeaderCount.setText(unreadCount + " belum dibaca");
            lblHeaderCount.setForeground(new Color(185, 28, 28)); // Red accent
            markAllBtn.setEnabled(true);
        } else {
            lblHeaderCount.setText("Semua sudah dibaca");
            lblHeaderCount.setForeground(Theme.TEXT_MUTED);
            markAllBtn.setEnabled(false);
        }

        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 0, 0));
            JLabel empty = new JLabel("Belum ada pemberitahuan untuk akun Anda.");
            empty.setFont(Fonts.sans(13f, Font.PLAIN));
            empty.setForeground(Theme.TEXT_MUTED);
            emptyPanel.add(empty);
            listContainer.add(emptyPanel);
        } else {
            for (Notification n : list) {
                boolean isUnread = !n.isDibaca();
                Color cardBg = isUnread ? new Color(255, 252, 248) : Theme.CARD_BACKGROUND;
                Color cardBorder = isUnread ? new Color(220, 160, 120) : Theme.CARD_BORDER;

                RoundedPanel card = new RoundedPanel(new BorderLayout(0, 8), cardBg, cardBorder, Theme.CARD_RADIUS);
                card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
                card.setMaximumSize(new Dimension(620, 130));

                // Top line: Dot (if unread) + Title + Tag + Time
                JPanel top = new JPanel(new BorderLayout(8, 0));
                top.setOpaque(false);

                JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                leftTop.setOpaque(false);

                if (isUnread) {
                    JLabel dot = new JLabel("● ");
                    dot.setFont(new Font("SansSerif", Font.BOLD, 14));
                    dot.setForeground(new Color(220, 38, 38));
                    leftTop.add(dot);
                }

                JLabel judul = new JLabel(n.getJudul());
                judul.setFont(Fonts.sans(13.5f, isUnread ? Font.BOLD : Font.PLAIN));
                judul.setForeground(Theme.TEXT_PRIMARY);
                leftTop.add(judul);

                // Type Badge
                JLabel typeBadge = new JLabel(" [" + n.getTipe().getLabel() + "] ");
                typeBadge.setFont(Fonts.sans(11f, Font.PLAIN));
                typeBadge.setForeground(Theme.TEXT_SECONDARY);
                leftTop.add(typeBadge);

                JLabel waktu = new JLabel(n.getDibuatPada().format(dtf));
                waktu.setFont(Fonts.sans(11f, Font.PLAIN));
                waktu.setForeground(Theme.TEXT_MUTED);

                top.add(leftTop, BorderLayout.WEST);
                top.add(waktu, BorderLayout.EAST);

                // Center message
                JLabel isi = new JLabel("<html><body style='width: 480px; font-family: sans-serif; font-size: 12px; color: #2A2420;'>" +
                        n.getIsi() + "</body></html>");

                // Bottom action line: deep link button & "Tandai Dibaca" button
                JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                actionRow.setOpaque(false);

                // Deep-link to claim if related to claim
                NotificationType tipe = n.getTipe();
                if (controller != null && (tipe == NotificationType.KLAIM_DITERIMA
                        || tipe == NotificationType.KLAIM_DISETUJUI
                        || tipe == NotificationType.KLAIM_DITOLAK)) {
                    JButton lihatKlaimBtn = createLinkButton("Lihat Klaim Saya");
                    lihatKlaimBtn.addActionListener(e -> {
                        dispose();
                        controller.showKlaimSaya();
                    });
                    actionRow.add(lihatKlaimBtn);
                } else if (controller != null && tipe == NotificationType.KANDIDAT_COCOK) {
                    JButton lihatKandidatBtn = createLinkButton("Lihat Kandidat Cocok");
                    lihatKandidatBtn.addActionListener(e -> {
                        dispose();
                        String ref = n.getReferensiId();
                        String hilangId = (ref != null && ref.contains(":")) ? ref.split(":")[0] : ref;
                        controller.showKandidatCocokById(hilangId);
                    });
                    actionRow.add(lihatKandidatBtn);
                }

                if (isUnread) {
                    JButton tandaiBtn = createLinkButton("Tandai Sudah Dibaca");
                    tandaiBtn.addActionListener(e -> {
                        notificationService.markAsRead(n.getId(), appSession.getNomorIdentitas());
                        if (controller != null) {
                            controller.updateNotificationCount();
                        }
                        loadNotifikasi();
                    });
                    actionRow.add(tandaiBtn);
                }

                card.add(top, BorderLayout.NORTH);
                card.add(isi, BorderLayout.CENTER);
                if (actionRow.getComponentCount() > 0) {
                    card.add(actionRow, BorderLayout.SOUTH);
                }

                listContainer.add(card);
                listContainer.add(Box.createVerticalStrut(10));
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Fonts.sans(11.5f, Font.BOLD));
        btn.setForeground(Theme.PILL_BUTTON_BG);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return btn;
    }
}
