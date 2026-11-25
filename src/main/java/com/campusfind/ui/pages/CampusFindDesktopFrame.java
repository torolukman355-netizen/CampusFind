package com.campusfind.ui.pages;

import com.campusfind.model.HakAkses;
import com.campusfind.model.Notification;
import com.campusfind.model.User;
import com.campusfind.model.UserAccount;
import com.campusfind.model.UserAccount.RoleType;
import com.campusfind.repository.FileRepositoryProvider;
import com.campusfind.repository.RepositoryProvider;
import com.campusfind.service.AdminService;
import com.campusfind.service.AppSession;
import com.campusfind.service.CandidateMatchingStrategy;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatNotifier;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingEngine;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PetugasService;
import com.campusfind.service.QRCodeService;
import com.campusfind.service.TokenOverlapStrategy;
import com.campusfind.service.WeightedAttributeStrategy;
import com.campusfind.ui.components.ModernSwingKit;
import com.campusfind.ui.theme.Fonts;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.Clock;
import java.util.List;

/**
 * =============================================================================
 * CampusFind v2.0 — Aplikasi Desktop Java Enterprise (Multi-Role Unified Shell)
 * =============================================================================
 * Menghadirkan pengalaman aplikasi desktop modern bergaya SaaS dengan:
 * - Dark Slate Sidebar Navigation & Active Role Switching / Authentication (RBAC)
 * - Role Petugas (Security & Cleaning Service — Bintang, Jepin, Fakhry)
 * - Role Mahasiswa (Pitya & Nadia)
 * - Role Admin (Lukman — Statistik, Grafik Batang, Manajemen Akun, Master Pos)
 * - Arsitektur DAO RepositoryProvider (SQL-Ready tanpa dependensi driver SQL)
 * =============================================================================
 */
public class CampusFindDesktopFrame extends JFrame {

    private final RepositoryProvider repos;
    private final AdminService adminService;
    private final NotificationService notificationService;
    private final PetugasService petugasService;

    private UserAccount activeAccount;
    private User activeDomainUser;

    private CardLayout contentLayout;
    private JPanel contentArea;

    private PetugasPage petugasPage;
    private MahasiswaDesktopPage mahasiswaPage;
    private AdminDesktopPage adminPage;

    // Sidebar & Topbar UI elements
    private JButton btnNavPetugas, btnNavMahasiswa, btnNavAdmin;
    private JLabel lblAvatar, lblUserName, lblUserRole;
    private JLabel lblTopTitle, lblTopSubtitle;
    private JButton btnNotifBell;
    private String currentCard = "PETUGAS";

    public CampusFindDesktopFrame() {
        this(new FileRepositoryProvider());
    }

    public CampusFindDesktopFrame(RepositoryProvider repos) {
        super("CampusFind v2.0 — Smart Lost & Found Desktop Application");
        this.repos = repos;

        Clock clock = Clock.systemDefaultZone();
        this.notificationService = new NotificationService(repos.notifications(), clock);
        QRCodeService qrCodeService = new QRCodeService();
        ItemService itemService = new ItemService(repos.items(), repos.pos(), clock);
        KlaimService klaimService = new KlaimService(
                repos.klaim(), repos.items(), repos.pos(),
                this.notificationService, new TokenOverlapStrategy(), clock
        );
        this.petugasService = new PetugasService(repos.items(), repos.pos(), this.notificationService, qrCodeService);

        CandidateMatchingStrategy candidateStrategy = new WeightedAttributeStrategy();
        KandidatService kandidatService = new KandidatService(repos.items(), candidateStrategy);
        MatchingEngine matchingEngine = new MatchingEngine(candidateStrategy, this.notificationService);
        itemService.addItemCreatedListener(new KandidatNotifier(repos.items(), candidateStrategy, this.notificationService, repos.notifications()));

        this.adminService = new AdminService(repos.users(), repos.items(), repos.pos());
        this.petugasService.pastikanDataAwalLengkap(repos.klaim());

        // Default login akun Bintang Pamungkas (Security)
        this.activeAccount = repos.users().findById("USR-001")
                .orElseGet(() -> adminService.getAllDemoAccounts().get(0));
        this.activeDomainUser = activeAccount.toDomainUser();

        AppSession appSession = new AppSession();
        appSession.setIdentitas(activeAccount.getNomorIdentitas(), activeAccount.getNama());

        // Bangun 3 modul halaman utama
        this.petugasPage = new PetugasPage(
                itemService, klaimService, repos.klaim(),
                kandidatService, petugasService, qrCodeService, appSession
        );
        this.mahasiswaPage = new MahasiswaDesktopPage(
                repos.items(), repos.klaim(), klaimService,
                petugasService, matchingEngine, activeDomainUser, this::syncAllViews
        );
        this.adminPage = new AdminDesktopPage(
                adminService, repos.items(), repos.pos(),
                petugasService, activeDomainUser, this::syncAllViews
        );

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 840);
        setMinimumSize(new Dimension(1120, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initSidebar();
        initMainShell();
        applyActiveAccountUI();
    }

    private void initSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 14));
        sidebar.setPreferredSize(new Dimension(265, 0));
        sidebar.setBackground(ModernSwingKit.SLATE_900);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, ModernSwingKit.SLATE_800),
                new EmptyBorder(20, 16, 18, 16)
        ));

        // Top Brand
        JPanel brandBox = new JPanel(new BorderLayout(10, 0));
        brandBox.setOpaque(false);
        brandBox.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 25)),
                new EmptyBorder(0, 0, 16, 0)
        ));

        JLabel logoIcon = new JLabel("CF", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ModernSwingKit.BLUE_600);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoIcon.setPreferredSize(new Dimension(40, 40));
        logoIcon.setFont(Fonts.sans(15f, Font.BOLD));
        logoIcon.setForeground(Color.WHITE);

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);
        JLabel lblBrand = new JLabel("CampusFind v2.0");
        lblBrand.setFont(Fonts.sans(16f, Font.BOLD));
        lblBrand.setForeground(Color.WHITE);
        JLabel lblTag = new JLabel("Smart Lost & Found Java App");
        lblTag.setFont(Fonts.sans(11f, Font.PLAIN));
        lblTag.setForeground(ModernSwingKit.SLATE_400);
        brandText.add(lblBrand);
        brandText.add(lblTag);

        brandBox.add(logoIcon, BorderLayout.WEST);
        brandBox.add(brandText, BorderLayout.CENTER);

        // Navigation Buttons
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        JLabel lblMenu = new JLabel("  WORKSPACES (ROLE)");
        lblMenu.setFont(Fonts.sans(10.5f, Font.BOLD));
        lblMenu.setForeground(ModernSwingKit.SLATE_500);
        navPanel.add(lblMenu);
        navPanel.add(Box.createVerticalStrut(10));

        btnNavPetugas = createNavButton("🛡  Petugas (Security & CS)", "PETUGAS");
        btnNavMahasiswa = createNavButton("🎓  Portal Mahasiswa", "MAHASISWA");
        btnNavAdmin = createNavButton("📊  Panel Admin & Statistik", "ADMIN");

        navPanel.add(btnNavPetugas);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnNavMahasiswa);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(btnNavAdmin);

        // Footer (SQL Status + User Card + Switch Account)
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 25)),
                new EmptyBorder(14, 0, 0, 0)
        ));

        JLabel lblSqlReady = new JLabel("● " + repos.getBackendType());
        lblSqlReady.setFont(Fonts.sans(10.5f, Font.PLAIN));
        lblSqlReady.setForeground(new Color(52, 211, 153));
        lblSqlReady.setBorder(new EmptyBorder(0, 4, 10, 0));

        JPanel userCard = new JPanel(new BorderLayout(10, 0));
        userCard.setBackground(ModernSwingKit.SLATE_800);
        userCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        lblAvatar = new JLabel("B", SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(36, 36));
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(ModernSwingKit.BLUE_600);
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setFont(Fonts.sans(14f, Font.BOLD));

        JPanel userMeta = new JPanel();
        userMeta.setLayout(new BoxLayout(userMeta, BoxLayout.Y_AXIS));
        userMeta.setOpaque(false);
        lblUserName = new JLabel();
        lblUserName.setFont(Fonts.sans(12.5f, Font.BOLD));
        lblUserName.setForeground(Color.WHITE);
        lblUserRole = new JLabel();
        lblUserRole.setFont(Fonts.sans(11f, Font.PLAIN));
        lblUserRole.setForeground(new Color(147, 197, 253));
        userMeta.add(lblUserName);
        userMeta.add(lblUserRole);

        userCard.add(lblAvatar, BorderLayout.WEST);
        userCard.add(userMeta, BorderLayout.CENTER);

        JButton btnSwitch = ModernSwingKit.createButton("⇄ Ganti Akun / Login", ModernSwingKit.SLATE_700, Color.WHITE);
        btnSwitch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnSwitch.addActionListener(e -> dialogSwitchOrLogin());

        footer.add(lblSqlReady);
        footer.add(userCard);
        footer.add(Box.createVerticalStrut(10));
        footer.add(btnSwitch);

        sidebar.add(brandBox, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createNavButton(String text, String cardKey) {
        JButton btn = new JButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(Fonts.sans(13f, Font.BOLD));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(ModernSwingKit.SLATE_900);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBorder(new EmptyBorder(10, 12, 10, 12));
        btn.addActionListener(e -> navigateToCard(cardKey));
        return btn;
    }

    private void initMainShell() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(ModernSwingKit.PAGE_BG);

        // Top Bar
        JPanel topbar = new JPanel(new BorderLayout(12, 0));
        topbar.setBackground(Color.WHITE);
        topbar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, ModernSwingKit.SLATE_200),
                new EmptyBorder(12, 22, 12, 22)
        ));

        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
        topLeft.setOpaque(false);
        lblTopTitle = new JLabel("CampusFind — Antrean Kerja Petugas");
        lblTopTitle.setFont(Fonts.sans(17f, Font.BOLD));
        lblTopTitle.setForeground(ModernSwingKit.SLATE_900);
        lblTopSubtitle = new JLabel();
        lblTopSubtitle.setFont(Fonts.sans(12f, Font.PLAIN));
        lblTopSubtitle.setForeground(ModernSwingKit.SLATE_500);
        topLeft.add(lblTopTitle);
        topLeft.add(lblTopSubtitle);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        topRight.setOpaque(false);

        btnNotifBell = ModernSwingKit.createButton("🔔 Notifikasi (0)", ModernSwingKit.SLATE_100, ModernSwingKit.SLATE_800);
        btnNotifBell.addActionListener(e -> dialogNotifikasi());

        JButton btnSyncAll = ModernSwingKit.createButton("🔄 Sinkronkan Semua", ModernSwingKit.SLATE_100, ModernSwingKit.SLATE_800);
        btnSyncAll.addActionListener(e -> syncAllViews());

        topRight.add(btnNotifBell);
        topRight.add(btnSyncAll);

        topbar.add(topLeft, BorderLayout.WEST);
        topbar.add(topRight, BorderLayout.EAST);

        // Content Card Area
        contentLayout = new CardLayout();
        contentArea = new JPanel(contentLayout);
        contentArea.setOpaque(false);
        contentArea.add(petugasPage, "PETUGAS");
        contentArea.add(mahasiswaPage, "MAHASISWA");
        contentArea.add(adminPage, "ADMIN");

        mainContainer.add(topbar, BorderLayout.NORTH);
        mainContainer.add(contentArea, BorderLayout.CENTER);
        add(mainContainer, BorderLayout.CENTER);
    }

    private void navigateToCard(String cardKey) {
        // Kontrol Hak Akses (RBAC Guard)
        if ("ADMIN".equals(cardKey) && !activeDomainUser.punyaAkses(HakAkses.MANAJEMEN_PENGGUNA)) {
            JOptionPane.showMessageDialog(this,
                    "Akses Ditolak (US-022): Panel Admin hanya dapat diakses oleh role Admin (Lukman).\n" +
                            "Silakan pindah akun ke Admin lewat tombol 'Ganti Akun / Login'.",
                    "Otorisasi RBAC", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("PETUGAS".equals(cardKey)
                && activeAccount.getRole() == RoleType.MAHASISWA) {
            JOptionPane.showMessageDialog(this,
                    "Akses Ditolak: Mahasiswa tidak dapat mengakses antrean kerja Petugas Pos.",
                    "Otorisasi RBAC", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentCard = cardKey;
        contentLayout.show(contentArea, cardKey);
        syncAllViews();
        updateNavHighlights();
    }

    private void updateNavHighlights() {
        styleNav(btnNavPetugas, "PETUGAS".equals(currentCard));
        styleNav(btnNavMahasiswa, "MAHASISWA".equals(currentCard));
        styleNav(btnNavAdmin, "ADMIN".equals(currentCard));

        if ("PETUGAS".equals(currentCard)) {
            lblTopTitle.setText("Workspace Petugas — Security & Cleaning Service");
        } else if ("MAHASISWA".equals(currentCard)) {
            lblTopTitle.setText("Portal Mahasiswa — Pelaporan, Pencocokan & Klaim");
        } else {
            lblTopTitle.setText("Panel Admin — Statistik, Grafik, Akun & Master Data");
        }
        lblTopSubtitle.setText(activeDomainUser.tampilDashboard());
    }

    private void styleNav(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(new Color(37, 99, 235, 65));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 3, 0, 0, new Color(96, 165, 250)),
                    new EmptyBorder(10, 9, 10, 12)
            ));
        } else {
            btn.setBackground(ModernSwingKit.SLATE_900);
            btn.setForeground(new Color(203, 213, 225));
            btn.setBorder(new EmptyBorder(10, 12, 10, 12));
        }
    }

    private void applyActiveAccountUI() {
        activeDomainUser = activeAccount.toDomainUser();
        lblAvatar.setText(activeAccount.getNama().substring(0, 1).toUpperCase());
        lblUserName.setText(activeAccount.getNama());
        lblUserRole.setText(activeAccount.getRole().getLabel());

        if (activeAccount.getRole() == RoleType.MAHASISWA) {
            mahasiswaPage.setActiveUser(activeDomainUser);
            currentCard = "MAHASISWA";
        } else if (activeAccount.getRole() == RoleType.ADMIN) {
            adminPage.setActiveAdmin(activeDomainUser);
            currentCard = "ADMIN";
        } else {
            petugasPage.setActiveRoleIndex(activeAccount.getRole() == RoleType.CLEANING_SERVICE ? 1 : 0);
            currentCard = "PETUGAS";
        }

        contentLayout.show(contentArea, currentCard);
        syncAllViews();
        updateNavHighlights();
    }

    public void syncAllViews() {
        petugasPage.refreshAllQueues();
        mahasiswaPage.refreshData();
        adminPage.refreshData();

        int unread = notificationService.countBelumDibaca(activeAccount.getNomorIdentitas());
        btnNotifBell.setText("🔔 Notifikasi (" + unread + ")");
        btnNotifBell.setForeground(unread > 0 ? ModernSwingKit.ROSE_600 : ModernSwingKit.SLATE_800);
    }

    private void dialogSwitchOrLogin() {
        List<UserAccount> accounts = adminService.getAllDemoAccounts();
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setPreferredSize(new Dimension(480, 420));

        JPanel demoGrid = new JPanel(new GridLayout(0, 1, 6, 6));
        demoGrid.setBorder(BorderFactory.createTitledBorder("Pilih Cepat Akun Demo Kelompok 4 (1-Klik)"));
        for (UserAccount acc : accounts) {
            String statusTxt = acc.isAktif() ? acc.getRole().getLabel() : "NONAKTIF (Ditolak Login)";
            JButton b = ModernSwingKit.createButton(
                    acc.getNama() + "  •  " + statusTxt + " (" + acc.getEmail() + ")",
                    acc.isAktif() ? ModernSwingKit.SLATE_100 : new Color(254, 226, 226),
                    ModernSwingKit.SLATE_900
            );
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.addActionListener(e -> {
                if (!acc.isAktif()) {
                    JOptionPane.showMessageDialog(this, "Akun ini berstatus Nonaktif dan ditolak oleh sistem (US-022).",
                            "Login Ditolak", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                activeAccount = acc;
                applyActiveAccountUI();
                java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(b);
                if (w != null) w.dispose();
            });
            demoGrid.add(b);
        }

        JPanel manualPanel = new JPanel(new GridLayout(3, 2, 8, 6));
        manualPanel.setBorder(BorderFactory.createTitledBorder("Atau Login Manual Email & Kata Sandi"));
        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JButton btnLogin = ModernSwingKit.createButton("Masuk dengan Kredensial", ModernSwingKit.BLUE_600, Color.WHITE);

        manualPanel.add(new JLabel("Email:"));
        manualPanel.add(txtEmail);
        manualPanel.add(new JLabel("Kata Sandi:"));
        manualPanel.add(txtPass);
        manualPanel.add(new JLabel());
        manualPanel.add(btnLogin);

        btnLogin.addActionListener(e -> {
            try {
                UserAccount auth = adminService.authenticate(txtEmail.getText(), new String(txtPass.getPassword()));
                activeAccount = auth;
                applyActiveAccountUI();
                java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(btnLogin);
                if (w != null) w.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Autentikasi Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });

        container.add(new JScrollPane(demoGrid), BorderLayout.CENTER);
        container.add(manualPanel, BorderLayout.SOUTH);

        JOptionPane.showOptionDialog(this, container, "Autentikasi & Ganti Akun CampusFind v2.0",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{"Tutup"}, "Tutup");
    }

    private void dialogNotifikasi() {
        List<Notification> list = notificationService.findByPenerima(activeAccount.getNomorIdentitas());
        StringBuilder sb = new StringBuilder();
        if (list.isEmpty()) {
            sb.append("Belum ada notifikasi in-app untuk ").append(activeAccount.getNama()).append(".");
        } else {
            for (Notification n : list) {
                sb.append(n.isDibaca() ? "[Dibaca] " : "[BARU] ")
                  .append(n.getJudul()).append("\n")
                  .append(n.getIsi()).append("\n")
                  .append("Waktu: ").append(n.getDibuatPada()).append("\n")
                  .append("--------------------------------------------------\n");
            }
        }
        JTextArea area = new JTextArea(sb.toString(), 14, 42);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        int res = JOptionPane.showOptionDialog(this, new JScrollPane(area),
                "Notifikasi In-App (" + activeAccount.getNama() + ")",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"✔ Tandai Semua Sudah Dibaca", "Tutup"}, "✔ Tandai Semua Sudah Dibaca");
        if (res == JOptionPane.YES_OPTION) {
            notificationService.markAllAsRead(activeAccount.getNomorIdentitas());
            syncAllViews();
        }
    }
}
