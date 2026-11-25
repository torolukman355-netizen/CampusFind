package com.campusfind.ui.pages;

import com.campusfind.model.Role;
import com.campusfind.model.User;
import com.campusfind.service.AppSession;
import com.campusfind.service.AuthService;
import com.campusfind.ui.theme.Fonts;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Halaman Masuk / Login CampusFind dengan desain Split-Screen modern.
 * Panel kiri: Brand CampusFind dark navy dengan 4 fitur utama.
 * Panel kanan: Kartu form masuk putih dengan tombol role cepat (Mahasiswa, Petugas, Admin).
 */
public class LoginPage extends JPanel {

    private final NavigationController controller;
    private final AuthService authService;
    private final AppSession appSession;

    private CardLayout rightCardLayout;
    private JPanel rightCardContainer;

    // Login Form Components
    private CustomInputField emailField;
    private CustomPasswordField passwordField;
    private JLabel errorLabel;
    private JButton masukButton;

    // Register Form Components (Khusus Mahasiswa)
    private CustomInputField regNamaField;
    private CustomInputField regNimField;
    private CustomInputField regEmailField;
    private CustomPasswordField regPasswordField;
    private CustomPasswordField regConfirmPasswordField;
    private JLabel regErrorLabel;
    private JButton regDaftarButton;

    // Color Palette
    private static final Color NAVY_BG = new Color(0x0B, 0x13, 0x2B);
    private static final Color NAVY_CIRCLE_TOP = new Color(0x1E, 0x1B, 0x4B, 190);
    private static final Color NAVY_CIRCLE_BOTTOM = new Color(0x15, 0x1E, 0x33, 230);
    private static final Color CARD_NAVY_BG = new Color(0x18, 0x22, 0x38);
    private static final Color CARD_NAVY_BORDER = new Color(0x27, 0x35, 0x4F);

    private static final Color RIGHT_PANEL_BG = new Color(0xF1, 0xF5, 0xF9);
    private static final Color WHITE_CARD_BG = Color.WHITE;
    private static final Color WHITE_CARD_BORDER = new Color(0xE2, 0xE8, 0xF0);

    private static final Color PRIMARY_INDIGO = new Color(0x4F, 0x46, 0xE5);
    private static final Color PRIMARY_INDIGO_HOVER = new Color(0x43, 0x38, 0xCA);
    private static final Color PRIMARY_INDIGO_PRESSED = new Color(0x37, 0x30, 0xA3);

    private static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A);
    private static final Color TEXT_LABEL = new Color(0x33, 0x41, 0x55);
    private static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    private static final Color TEXT_NAVY_MUTED = new Color(0x94, 0xA3, 0xB8);
    private static final Color ERROR_RED = new Color(0xEF, 0x44, 0x44);

    public LoginPage(NavigationController controller, AuthService authService, AppSession appSession) {
        super(new GridLayout(1, 2));
        this.controller = controller;
        this.authService = authService;
        this.appSession = appSession;

        initComponents();
    }

    private void initComponents() {
        // -------------------------------------------------------------
        // 1. PANEL KIRI (Brand, Hero, 4 Feature Cards, Footer)
        // -------------------------------------------------------------
        JPanel leftPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Base navy background
                g2.setColor(NAVY_BG);
                g2.fillRect(0, 0, w, h);

                // Decorative top-right circle
                g2.setColor(NAVY_CIRCLE_TOP);
                int r1 = (int) (w * 0.7);
                g2.fillOval((int) (w * 0.55), (int) (-h * 0.1), r1, r1);

                // Decorative bottom-left circle
                g2.setColor(NAVY_CIRCLE_BOTTOM);
                int r2 = (int) (w * 0.65);
                g2.fillOval((int) (-w * 0.25), (int) (h * 0.65), r2, r2);

                g2.dispose();
            }
        };
        leftPanel.setBorder(new EmptyBorder(40, 56, 36, 56));
        leftPanel.setOpaque(false);

        // Header: Logo & Brand
        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandRow.setOpaque(false);

        JLabel logoIcon = new JLabel(new CampusLogoIcon(38));
        JLabel brandTitle = new JLabel("CampusFind");
        brandTitle.setFont(Fonts.sans(22f, Font.BOLD));
        brandTitle.setForeground(Color.WHITE);

        brandRow.add(logoIcon);
        brandRow.add(brandTitle);
        leftPanel.add(brandRow, BorderLayout.NORTH);

        // Center: Hero text + Feature Cards (2x2)
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        centerContainer.add(Box.createVerticalStrut(40));

        // Headline
        JLabel line1 = new JLabel("Barang hilang,");
        line1.setFont(Fonts.sans(36f, Font.BOLD));
        line1.setForeground(Color.WHITE);
        line1.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(line1);

        JLabel line2 = new JLabel("ketemu lebih cepat.");
        line2.setFont(Fonts.sans(36f, Font.BOLD));
        line2.setForeground(Color.WHITE);
        line2.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(line2);

        centerContainer.add(Box.createVerticalStrut(14));

        // Subtitle
        JLabel subtitle = new JLabel("<html><body style='width: 380px;'>"
                + "Satu sistem untuk lapor, cocokkan otomatis, lacak lewat QR Code, "
                + "dan serahkan hanya kepada pemilik sah.</body></html>");
        subtitle.setFont(Fonts.sans(14.5f, Font.PLAIN));
        subtitle.setForeground(TEXT_NAVY_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContainer.add(subtitle);

        centerContainer.add(Box.createVerticalStrut(32));

        // 4 Feature Cards (2x2 Grid)
        JPanel cardsGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(500, 160));
        cardsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardsGrid.add(createFeatureCard(new SparkleIcon(20, new Color(0xFB, 0xBF, 0x24)),
                "Smart Matching", "Skor kecocokan 0–100"));
        cardsGrid.add(createFeatureCard(new QrMiniIcon(20, new Color(0xFB, 0xBF, 0x24)),
                "QR Code unik", "Lacak barang fisik"));
        cardsGrid.add(createFeatureCard(new ShieldMiniIcon(20, new Color(0xFB, 0xBF, 0x24)),
                "Verifikasi klaim", "Bukti kepemilikan wajib"));
        cardsGrid.add(createFeatureCard(new BellMiniIcon(20, new Color(0xFB, 0xBF, 0x24)),
                "Notifikasi", "Status selalu jelas"));

        centerContainer.add(cardsGrid);
        leftPanel.add(centerContainer, BorderLayout.CENTER);

        // Footer note
        JLabel footerLabel = new JLabel("Pemrograman Berorientasi Objek Lanjutan · Kelompok 4");
        footerLabel.setFont(Fonts.sans(12f, Font.PLAIN));
        footerLabel.setForeground(new Color(0x64, 0x74, 0x8B));
        leftPanel.add(footerLabel, BorderLayout.SOUTH);

        add(leftPanel);

        // -------------------------------------------------------------
        // 2. PANEL KANAN (White Login Card in Light Background)
        // -------------------------------------------------------------
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(RIGHT_PANEL_BG);

        // Card Container
        JPanel loginCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 16;

                // Card background
                g2.setColor(WHITE_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                // Card border
                g2.setColor(WHITE_CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setOpaque(false);
        loginCard.setPreferredSize(new Dimension(440, 520));
        loginCard.setMaximumSize(new Dimension(460, 560));
        loginCard.setBorder(new EmptyBorder(36, 36, 32, 36));

        // Title & Subtitle
        JLabel cardTitle = new JLabel("Masuk");
        cardTitle.setFont(Fonts.sans(24f, Font.BOLD));
        cardTitle.setForeground(TEXT_DARK);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(cardTitle);

        loginCard.add(Box.createVerticalStrut(6));

        JLabel cardSub = new JLabel("Gunakan akun kampus Anda untuk melanjutkan.");
        cardSub.setFont(Fonts.sans(13.5f, Font.PLAIN));
        cardSub.setForeground(TEXT_MUTED);
        cardSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(cardSub);

        loginCard.add(Box.createVerticalStrut(24));

        // Email Field Section
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(Fonts.sans(13f, Font.BOLD));
        emailLabel.setForeground(TEXT_LABEL);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(emailLabel);

        loginCard.add(Box.createVerticalStrut(6));

        emailField = new CustomInputField("budi@student.kampus.ac", new EnvelopeIcon(18, new Color(0x94, 0xA3, 0xB8)));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(emailField);

        loginCard.add(Box.createVerticalStrut(4));

        // Error message label (initially hidden or shown on validation error)
        errorLabel = new JLabel(" Format email tidak valid.");
        errorLabel.setIcon(new AlertCircleIcon(13, ERROR_RED));
        errorLabel.setFont(Fonts.sans(11.5f, Font.PLAIN));
        errorLabel.setForeground(ERROR_RED);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setVisible(false);
        loginCard.add(errorLabel);

        loginCard.add(Box.createVerticalStrut(14));

        // Password Field Section
        JLabel passLabel = new JLabel("Kata sandi");
        passLabel.setFont(Fonts.sans(13f, Font.BOLD));
        passLabel.setForeground(TEXT_LABEL);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(passLabel);

        loginCard.add(Box.createVerticalStrut(6));

        passwordField = new CustomPasswordField("••••••••••••", new LockIcon(18, new Color(0x94, 0xA3, 0xB8)));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(passwordField);

        loginCard.add(Box.createVerticalStrut(20));

        // Masuk Button (Full Width, Indigo)
        masukButton = new JButton("Masuk  (Enter)", new LoginArrowIcon(18, Color.WHITE)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 8;

                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_INDIGO_PRESSED);
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_INDIGO_HOVER);
                } else {
                    g2.setColor(PRIMARY_INDIGO);
                }

                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, r, r));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        masukButton.setFont(Fonts.sans(14f, Font.BOLD));
        masukButton.setForeground(Color.WHITE);
        masukButton.setFocusPainted(false);
        masukButton.setBorderPainted(false);
        masukButton.setContentAreaFilled(false);
        masukButton.setOpaque(false);
        masukButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        masukButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        masukButton.setPreferredSize(new Dimension(368, 44));
        masukButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        masukButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        masukButton.setIconTextGap(8);

        masukButton.addActionListener(e -> executeLogin());
        loginCard.add(masukButton);

        // Enter key action
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeLogin();
                }
            }
        };
        emailField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        loginCard.add(Box.createVerticalStrut(14));

        // Toggle to register form
        JPanel regPromptRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        regPromptRow.setOpaque(false);
        regPromptRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPromptRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel belumPunyaLbl = new JLabel("Belum punya akun?");
        belumPunyaLbl.setFont(Fonts.sans(12f, Font.PLAIN));
        belumPunyaLbl.setForeground(TEXT_MUTED);

        JButton bukaDaftarBtn = new JButton("Daftar sebagai Mahasiswa →");
        bukaDaftarBtn.setFont(Fonts.sans(12f, Font.BOLD));
        bukaDaftarBtn.setForeground(PRIMARY_INDIGO);
        bukaDaftarBtn.setBorderPainted(false);
        bukaDaftarBtn.setContentAreaFilled(false);
        bukaDaftarBtn.setFocusPainted(false);
        bukaDaftarBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bukaDaftarBtn.addActionListener(e -> showRegisterCard());

        regPromptRow.add(belumPunyaLbl);
        regPromptRow.add(bukaDaftarBtn);
        loginCard.add(regPromptRow);

        loginCard.add(Box.createVerticalStrut(20));

        // Role Section Header
        JLabel roleGuide = new JLabel("SETELAH MASUK, MENU MENGIKUTI ROLE");
        roleGuide.setFont(Fonts.trackedSans(11f, Font.BOLD, 0.08));
        roleGuide.setForeground(TEXT_MUTED);
        roleGuide.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(roleGuide);

        loginCard.add(Box.createVerticalStrut(12));

        // 3 Quick Role Buttons (1 row, 3 columns)
        JPanel roleGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        roleGrid.setOpaque(false);
        roleGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        roleGrid.setPreferredSize(new Dimension(368, 64));
        roleGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Mahasiswa Button
        roleGrid.add(createRoleButton("Mahasiswa",
                new MortarboardIcon(20, new Color(0x43, 0x38, 0xCA)),
                new Color(0xEE, 0xF2, 0xFF),
                new Color(0xE0, 0xE7, 0xFF),
                new Color(0xC7, 0xD2, 0xFE),
                new Color(0x43, 0x38, 0xCA),
                () -> loginAsRole(Role.MAHASISWA, "budi@student.kampus.ac", "Budi Santoso", "123456789")
        ));

        // Petugas Button
        roleGrid.add(createRoleButton("Petugas",
                new OfficerBadgeIcon(20, new Color(0xB4, 0x53, 0x09)),
                new Color(0xFE, 0xF3, 0xC7),
                new Color(0xFD, 0xE6, 0x8A),
                new Color(0xFD, 0xE6, 0x8A),
                new Color(0xB4, 0x53, 0x09),
                () -> loginAsRole(Role.PETUGAS, "rina@kampus.ac", "Rina Wulandari", "198501012010122001")
        ));

        // Admin Button
        roleGrid.add(createRoleButton("Admin",
                new AdminShieldIcon(20, new Color(0x04, 0x78, 0x57)),
                new Color(0xEC, 0xFD, 0xF5),
                new Color(0xD1, 0xFA, 0xE5),
                new Color(0xA7, 0xF3, 0xD0),
                new Color(0x04, 0x78, 0x57),
                () -> loginAsRole(Role.ADMIN, "hendra@kampus.ac", "Hendra Admin", "admin")
        ));

        loginCard.add(roleGrid);

        // Register Card setup
        JPanel registerCard = createRegisterCard();

        rightCardLayout = new CardLayout();
        rightCardContainer = new JPanel(rightCardLayout);
        rightCardContainer.setOpaque(false);
        rightCardContainer.add(loginCard, "LOGIN");
        rightCardContainer.add(registerCard, "REGISTER");

        // Add card container to right panel centered
        rightPanel.add(rightCardContainer, new GridBagConstraints());
        add(rightPanel);
    }

    public void showLoginCard() {
        if (rightCardLayout != null && rightCardContainer != null) {
            rightCardLayout.show(rightCardContainer, "LOGIN");
            if (emailField != null) {
                emailField.requestFocusInWindow();
            }
        }
    }

    public void showRegisterCard() {
        if (rightCardLayout != null && rightCardContainer != null) {
            rightCardLayout.show(rightCardContainer, "REGISTER");
            if (regNamaField != null) {
                regNamaField.requestFocusInWindow();
            }
        }
    }

    private JPanel createRegisterCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 16;

                g2.setColor(WHITE_CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                g2.setColor(WHITE_CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(440, 580));
        card.setMaximumSize(new Dimension(460, 620));
        card.setBorder(new EmptyBorder(26, 36, 24, 36));

        // Title
        JLabel title = new JLabel("Daftar Akun Mahasiswa");
        title.setFont(Fonts.sans(22f, Font.BOLD));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);

        card.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Khusus mahasiswa untuk layanan rekonsiliasi kampus.");
        subtitle.setFont(Fonts.sans(12.5f, Font.PLAIN));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createVerticalStrut(10));

        // Notice Banner
        JPanel banner = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFE, 0xF3, 0xC7));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(new Color(0xFD, 0xE6, 0x8A));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(6, 10, 6, 10));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bannerIcon = new JLabel(new OfficerBadgeIcon(16, new Color(0xB4, 0x53, 0x09)));
        JLabel bannerText = new JLabel("<html><body style='width: 320px; font-size: 10px; color: #92400E;'>"
                + "<b>Pendaftaran mandiri khusus Mahasiswa.</b> Petugas & Admin mendapatkan akses langsung dari Administrator kampus.</body></html>");
        banner.add(bannerIcon, BorderLayout.WEST);
        banner.add(bannerText, BorderLayout.CENTER);
        card.add(banner);

        card.add(Box.createVerticalStrut(12));

        // 1. Nama Lengkap
        JLabel lblNama = new JLabel("Nama Lengkap");
        lblNama.setFont(Fonts.sans(12f, Font.BOLD));
        lblNama.setForeground(TEXT_LABEL);
        lblNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblNama);
        card.add(Box.createVerticalStrut(3));

        regNamaField = new CustomInputField("", new UserMiniIcon(16, new Color(0x94, 0xA3, 0xB8)));
        regNamaField.setAlignmentX(Component.LEFT_ALIGNMENT);
        regNamaField.setPreferredSize(new Dimension(368, 38));
        card.add(regNamaField);

        card.add(Box.createVerticalStrut(8));

        // 2. NIM
        JLabel lblNim = new JLabel("Nomor Induk Mahasiswa (NIM 9 Digit)");
        lblNim.setFont(Fonts.sans(12f, Font.BOLD));
        lblNim.setForeground(TEXT_LABEL);
        lblNim.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblNim);
        card.add(Box.createVerticalStrut(3));

        regNimField = new CustomInputField("", new IdCardMiniIcon(16, new Color(0x94, 0xA3, 0xB8)));
        regNimField.setAlignmentX(Component.LEFT_ALIGNMENT);
        regNimField.setPreferredSize(new Dimension(368, 38));
        card.add(regNimField);

        card.add(Box.createVerticalStrut(8));

        // 3. Email
        JLabel lblEmail = new JLabel("Email Mahasiswa");
        lblEmail.setFont(Fonts.sans(12f, Font.BOLD));
        lblEmail.setForeground(TEXT_LABEL);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblEmail);
        card.add(Box.createVerticalStrut(3));

        regEmailField = new CustomInputField("", new EnvelopeIcon(16, new Color(0x94, 0xA3, 0xB8)));
        regEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        regEmailField.setPreferredSize(new Dimension(368, 38));
        card.add(regEmailField);

        card.add(Box.createVerticalStrut(8));

        // 4. Kata Sandi
        JLabel lblPass = new JLabel("Kata Sandi (min. 6 karakter)");
        lblPass.setFont(Fonts.sans(12f, Font.BOLD));
        lblPass.setForeground(TEXT_LABEL);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblPass);
        card.add(Box.createVerticalStrut(3));

        regPasswordField = new CustomPasswordField("", new LockIcon(16, new Color(0x94, 0xA3, 0xB8)));
        regPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        regPasswordField.setPreferredSize(new Dimension(368, 38));
        card.add(regPasswordField);

        card.add(Box.createVerticalStrut(8));

        // 5. Konfirmasi Sandi
        JLabel lblConfirm = new JLabel("Konfirmasi Kata Sandi");
        lblConfirm.setFont(Fonts.sans(12f, Font.BOLD));
        lblConfirm.setForeground(TEXT_LABEL);
        lblConfirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblConfirm);
        card.add(Box.createVerticalStrut(3));

        regConfirmPasswordField = new CustomPasswordField("", new LockIcon(16, new Color(0x94, 0xA3, 0xB8)));
        regConfirmPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        regConfirmPasswordField.setPreferredSize(new Dimension(368, 38));
        card.add(regConfirmPasswordField);

        card.add(Box.createVerticalStrut(6));

        // Error message label
        regErrorLabel = new JLabel(" Validasi gagal");
        regErrorLabel.setIcon(new AlertCircleIcon(13, ERROR_RED));
        regErrorLabel.setFont(Fonts.sans(11f, Font.PLAIN));
        regErrorLabel.setForeground(ERROR_RED);
        regErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        regErrorLabel.setVisible(false);
        card.add(regErrorLabel);

        card.add(Box.createVerticalStrut(10));

        // Enter key action in register fields
        KeyAdapter regEnterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeRegister();
                }
            }
        };
        regNamaField.addKeyListener(regEnterListener);
        regNimField.addKeyListener(regEnterListener);
        regEmailField.addKeyListener(regEnterListener);
        regPasswordField.addKeyListener(regEnterListener);
        regConfirmPasswordField.addKeyListener(regEnterListener);

        // Tombol Daftar
        regDaftarButton = new JButton("Daftar Akun Mahasiswa") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 8;

                if (getModel().isPressed()) {
                    g2.setColor(PRIMARY_INDIGO_PRESSED);
                } else if (getModel().isRollover()) {
                    g2.setColor(PRIMARY_INDIGO_HOVER);
                } else {
                    g2.setColor(PRIMARY_INDIGO);
                }

                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, r, r));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        regDaftarButton.setFont(Fonts.sans(13.5f, Font.BOLD));
        regDaftarButton.setForeground(Color.WHITE);
        regDaftarButton.setFocusPainted(false);
        regDaftarButton.setBorderPainted(false);
        regDaftarButton.setContentAreaFilled(false);
        regDaftarButton.setOpaque(false);
        regDaftarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regDaftarButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        regDaftarButton.setPreferredSize(new Dimension(368, 40));
        regDaftarButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        regDaftarButton.addActionListener(e -> executeRegister());
        card.add(regDaftarButton);

        card.add(Box.createVerticalStrut(10));

        // Back to login row
        JPanel loginPromptRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        loginPromptRow.setOpaque(false);
        loginPromptRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginPromptRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel sudahPunyaLbl = new JLabel("Sudah punya akun?");
        sudahPunyaLbl.setFont(Fonts.sans(12f, Font.PLAIN));
        sudahPunyaLbl.setForeground(TEXT_MUTED);

        JButton bukaLoginBtn = new JButton("Masuk di sini →");
        bukaLoginBtn.setFont(Fonts.sans(12f, Font.BOLD));
        bukaLoginBtn.setForeground(PRIMARY_INDIGO);
        bukaLoginBtn.setBorderPainted(false);
        bukaLoginBtn.setContentAreaFilled(false);
        bukaLoginBtn.setFocusPainted(false);
        bukaLoginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bukaLoginBtn.addActionListener(e -> showLoginCard());

        loginPromptRow.add(sudahPunyaLbl);
        loginPromptRow.add(bukaLoginBtn);
        card.add(loginPromptRow);

        return card;
    }

    private void resetRegErrors() {
        if (regNamaField != null) regNamaField.setError(false);
        if (regNimField != null) regNimField.setError(false);
        if (regEmailField != null) regEmailField.setError(false);
        if (regPasswordField != null) regPasswordField.setError(false);
        if (regConfirmPasswordField != null) regConfirmPasswordField.setError(false);
        if (regErrorLabel != null) regErrorLabel.setVisible(false);
    }

    private void showRegError(JComponent comp, String message) {
        if (comp instanceof CustomInputField field) {
            field.setError(true);
            field.requestFocusInWindow();
        } else if (comp instanceof CustomPasswordField pass) {
            pass.setError(true);
            pass.requestFocusInWindow();
        }
        if (regErrorLabel != null) {
            regErrorLabel.setText(" " + message);
            regErrorLabel.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void executeRegister() {
        String nama = regNamaField.getText() == null ? "" : regNamaField.getText().trim();
        String nim = regNimField.getText() == null ? "" : regNimField.getText().trim();
        String email = regEmailField.getText() == null ? "" : regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirmPassword = new String(regConfirmPasswordField.getPassword());

        resetRegErrors();

        if (nama.isBlank()) {
            showRegError(regNamaField, "Nama lengkap wajib diisi.");
            return;
        }

        if (nim.isBlank()) {
            showRegError(regNimField, "NIM wajib diisi.");
            return;
        }
        if (!nim.matches("\\d+")) {
            showRegError(regNimField, "NIM hanya boleh berisi angka.");
            return;
        }
        if (nim.length() != 9) {
            showRegError(regNimField, "NIM Mahasiswa harus tepat 9 digit angka.");
            return;
        }

        if (email.isBlank() || !email.contains("@") || !email.contains(".") || email.length() < 5) {
            showRegError(regEmailField, "Format email tidak valid (contoh: nama@student.kampus.ac).");
            return;
        }

        if (password.length() < 6) {
            showRegError(regPasswordField, "Kata sandi minimal 6 karakter.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showRegError(regConfirmPasswordField, "Konfirmasi kata sandi tidak cocok.");
            return;
        }

        try {
            User mhs = authService.daftarMahasiswa(nama, nim, email, password);
            if (controller != null) {
                controller.updateNotificationCount();
                controller.setNavBarVisible(true);
                Runnable pending = controller.consumePendingLoginAction();
                if (pending != null) {
                    pending.run();
                } else {
                    controller.navigateToTemuan();
                }
            }
        } catch (Exception ex) {
            showRegError(regEmailField, ex.getMessage());
        }
    }

    private void executeLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        boolean validFormat = email.contains("@") && email.contains(".") && email.length() >= 5;

        if (!validFormat) {
            emailField.setError(true);
            errorLabel.setText(" Format email tidak valid.");
            errorLabel.setVisible(true);
            emailField.requestFocusInWindow();
            revalidate();
            repaint();
            return;
        }

        emailField.setError(false);
        errorLabel.setVisible(false);

        String password = new String(passwordField.getPassword());
        try {
            User user = authService.loginWithEmail(email, password);
            controller.updateNotificationCount();
            controller.setNavBarVisible(true);

            Runnable pending = controller.consumePendingLoginAction();
            if (pending != null) {
                pending.run();
            } else if (user.getRole() == Role.ADMIN) {
                controller.navigateToAdmin();
            } else if (user.getRole() == Role.PETUGAS) {
                controller.navigateToPetugas();
            } else {
                controller.navigateToTemuan();
            }
        } catch (Exception ex) {
            emailField.setError(true);
            errorLabel.setText(" " + ex.getMessage());
            errorLabel.setVisible(true);
            revalidate();
            repaint();
        }
    }

    private void loginAsRole(Role role, String email, String nama, String id) {
        emailField.setText(email);
        emailField.setError(false);
        errorLabel.setVisible(false);
        passwordField.setText("••••••••••••");

        try {
            authService.login(id, nama, role);
            controller.updateNotificationCount();
            controller.setNavBarVisible(true);

            // Execute pending action if any, otherwise route to role dashboard
            Runnable pending = controller.consumePendingLoginAction();
            if (pending != null) {
                pending.run();
            } else if (role == Role.ADMIN) {
                controller.navigateToAdmin();
            } else if (role == Role.PETUGAS) {
                controller.navigateToPetugas();
            } else {
                controller.navigateToTemuan();
            }
        } catch (Exception ex) {
            emailField.setError(true);
            errorLabel.setText(" " + ex.getMessage());
            errorLabel.setVisible(true);
            revalidate();
            repaint();
        }
    }

    private JPanel createFeatureCard(Icon icon, String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 12;

                g2.setColor(CARD_NAVY_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                g2.setColor(CARD_NAVY_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel iconLabel = new JLabel(icon);
        card.add(iconLabel, BorderLayout.WEST);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Fonts.sans(13.5f, Font.BOLD));
        titleLbl.setForeground(Color.WHITE);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(Fonts.sans(11.5f, Font.PLAIN));
        subLbl.setForeground(TEXT_NAVY_MUTED);

        textCol.add(titleLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(subLbl);

        card.add(textCol, BorderLayout.CENTER);
        return card;
    }

    private JButton createRoleButton(String roleName, Icon icon, Color bg, Color hoverBg, Color border, Color textColor, Runnable onClick) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int r = 10;

                g2.setColor(getModel().isRollover() || getModel().isPressed() ? hoverBg : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 8, 8, 8));

        JLabel icLbl = new JLabel(icon);
        icLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txtLbl = new JLabel(roleName);
        txtLbl.setFont(Fonts.sans(12.5f, Font.BOLD));
        txtLbl.setForeground(textColor);
        txtLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.add(icLbl);
        btn.add(Box.createVerticalStrut(5));
        btn.add(txtLbl);

        btn.addActionListener(e -> onClick.run());
        return btn;
    }

    // -----------------------------------------------------------------
    // Custom Input Fields with Inside Vector Icons
    // -----------------------------------------------------------------
    private static class CustomInputField extends JTextField {
        private final Icon leadingIcon;
        private boolean error = false;
        private boolean focused = false;

        public CustomInputField(String text, Icon leadingIcon) {
            super(text);
            this.leadingIcon = leadingIcon;
            init();
        }

        private void init() {
            setFont(Fonts.sans(13.5f, Font.PLAIN));
            setForeground(TEXT_DARK);
            setOpaque(false);
            setPreferredSize(new Dimension(368, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setMargin(new Insets(8, 40, 8, 12));

            addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        public void setError(boolean error) {
            this.error = error;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 8;

            // Background
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

            // Leading Icon
            if (leadingIcon != null) {
                int iconY = (h - leadingIcon.getIconHeight()) / 2;
                leadingIcon.paintIcon(this, g2, 14, iconY);
            }

            // Border
            if (error) {
                g2.setColor(ERROR_RED);
                g2.setStroke(new BasicStroke(1.5f));
            } else if (focused) {
                g2.setColor(PRIMARY_INDIGO);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(0xCB, 0xD5, 0xE1));
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CustomPasswordField extends JPasswordField {
        private final Icon leadingIcon;
        private boolean error = false;
        private boolean focused = false;

        public CustomPasswordField(String text, Icon leadingIcon) {
            super(text);
            this.leadingIcon = leadingIcon;
            init();
        }

        private void init() {
            setFont(Fonts.sans(13.5f, Font.PLAIN));
            setForeground(TEXT_DARK);
            setOpaque(false);
            setPreferredSize(new Dimension(368, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setMargin(new Insets(8, 40, 8, 12));

            addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        public void setError(boolean error) {
            this.error = error;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 8;

            // Background
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

            // Leading Icon
            if (leadingIcon != null) {
                int iconY = (h - leadingIcon.getIconHeight()) / 2;
                leadingIcon.paintIcon(this, g2, 14, iconY);
            }

            // Border
            if (error) {
                g2.setColor(ERROR_RED);
                g2.setStroke(new BasicStroke(1.5f));
            } else if (focused) {
                g2.setColor(PRIMARY_INDIGO);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(new Color(0xCB, 0xD5, 0xE1));
                g2.setStroke(new BasicStroke(1.0f));
            }
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, r, r));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // -----------------------------------------------------------------
    // Vector Icons (Pixel-perfect matching mockups)
    // -----------------------------------------------------------------
    private static class CampusLogoIcon implements Icon {
        private final int size;

        public CampusLogoIcon(int size) {
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);

            // Rounded badge
            g2.setColor(PRIMARY_INDIGO);
            g2.fillRoundRect(0, 0, size, size, 10, 10);

            // Search Pin inside (white)
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Magnifying glass circle
            int d = (int) (size * 0.42);
            int cx = (int) (size * 0.22);
            int cy = (int) (size * 0.22);
            g2.drawOval(cx, cy, d, d);

            // Pin dot inside glass
            g2.fillOval(cx + d / 2 - 2, cy + d / 2 - 2, 4, 4);

            // Handle
            int hx1 = cx + (int) (d * 0.85);
            int hy1 = cy + (int) (d * 0.85);
            int hx2 = (int) (size * 0.78);
            int hy2 = (int) (size * 0.78);
            g2.drawLine(hx1, hy1, hx2, hy2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class EnvelopeIcon implements Icon {
        private final int size;
        private final Color color;

        public EnvelopeIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int w = size;
            int h = (int) (size * 0.75);
            int top = (size - h) / 2;

            g2.drawRoundRect(1, top, w - 2, h, 3, 3);
            g2.drawLine(2, top + 1, w / 2, top + h / 2);
            g2.drawLine(w - 2, top + 1, w / 2, top + h / 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class LockIcon implements Icon {
        private final int size;
        private final Color color;

        public LockIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Shackle
            int sw = size - 6;
            int sh = size / 2;
            g2.drawArc(4, 1, sw - 2, sh, 0, 180);

            // Body
            int by = size / 2 - 1;
            int bh = size / 2;
            g2.fillRoundRect(2, by, size - 4, bh, 3, 3);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class LoginArrowIcon implements Icon {
        private final int size;
        private final Color color;

        public LoginArrowIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Door / bracket
            g2.drawLine(size - 3, 3, size - 3, size - 3);
            g2.drawLine(size - 7, 3, size - 3, 3);
            g2.drawLine(size - 7, size - 3, size - 3, size - 3);

            // Arrow shaft
            g2.drawLine(2, size / 2, size - 6, size / 2);
            // Arrow head
            g2.drawLine(size - 9, size / 2 - 4, size - 6, size / 2);
            g2.drawLine(size - 9, size / 2 + 4, size - 6, size / 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class AlertCircleIcon implements Icon {
        private final int size;
        private final Color color;

        public AlertCircleIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);

            g2.fillOval(0, 0, size, size);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(size / 2, 3, size / 2, size - 5);
            g2.fillOval(size / 2 - 1, size - 3, 2, 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class SparkleIcon implements Icon {
        private final int size;
        private final Color color;

        public SparkleIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Wand wand line
            g2.drawLine(3, size - 3, size - 6, 6);

            // Sparkles
            g2.drawLine(size - 3, 4, size - 3, 4);
            g2.drawLine(size - 7, 2, size - 7, 2);
            g2.drawLine(size - 2, 8, size - 2, 8);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class QrMiniIcon implements Icon {
        private final int size;
        private final Color color;

        public QrMiniIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);

            int b = size / 2 - 2;
            g2.drawRect(1, 1, b, b);
            g2.fillRect(3, 3, b - 4, b - 4);

            g2.drawRect(size / 2 + 1, 1, b, b);
            g2.fillRect(size / 2 + 3, 3, b - 4, b - 4);

            g2.drawRect(1, size / 2 + 1, b, b);
            g2.fillRect(3, size / 2 + 3, b - 4, b - 4);

            g2.fillRect(size / 2 + 3, size / 2 + 3, b - 2, b - 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class ShieldMiniIcon implements Icon {
        private final int size;
        private final Color color;

        public ShieldMiniIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int[] px = {2, size - 2, size - 2, size / 2, 2};
            int[] py = {3, 3, size / 2, size - 2, size / 2};
            g2.drawPolygon(px, py, 5);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class BellMiniIcon implements Icon {
        private final int size;
        private final Color color;

        public BellMiniIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Bell outline
            g2.drawArc(4, 3, size - 8, size - 6, 0, 180);
            g2.drawLine(4, size / 2, 2, size - 4);
            g2.drawLine(size - 4, size / 2, size - 2, size - 4);
            g2.drawLine(2, size - 4, size - 2, size - 4);

            // Clapper
            g2.drawLine(size / 2 - 1, size - 2, size / 2 + 1, size - 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class MortarboardIcon implements Icon {
        private final int size;
        private final Color color;

        public MortarboardIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Diamond cap
            int[] px = {size / 2, size - 1, size / 2, 1};
            int[] py = {2, 7, 12, 7};
            g2.fillPolygon(px, py, 4);

            // Cap base
            g2.drawArc(4, 9, size - 8, 8, 180, 180);

            // Tassel
            g2.drawLine(size - 3, 8, size - 3, 14);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class OfficerBadgeIcon implements Icon {
        private final int size;
        private final Color color;

        public OfficerBadgeIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Head
            g2.fillOval(size / 2 - 4, 1, 8, 8);

            // Shoulders & tie/badge
            g2.drawArc(2, 10, size - 4, 10, 0, 180);
            g2.drawLine(size / 2, 10, size / 2, 15);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class AdminShieldIcon implements Icon {
        private final int size;
        private final Color color;

        public AdminShieldIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Head
            g2.fillOval(size / 2 - 4, 1, 8, 8);

            // Body
            g2.drawArc(2, 10, size - 4, 10, 0, 180);

            // Small gear / badge
            g2.fillOval(size - 6, size - 6, 5, 5);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class UserMiniIcon implements Icon {
        private final int size;
        private final Color color;

        public UserMiniIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Head
            int r = size / 3;
            g2.drawOval(size / 2 - r / 2, 2, r, r);
            // Shoulders arc
            g2.drawArc(2, size / 2, size - 4, size / 2 + 2, 0, 180);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }

    private static class IdCardMiniIcon implements Icon {
        private final int size;
        private final Color color;

        public IdCardMiniIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Card outline
            g2.drawRoundRect(1, 2, size - 2, size - 4, 3, 3);
            // Photo rectangle
            g2.drawRect(3, 5, 4, 4);
            // Text lines
            g2.drawLine(9, 6, size - 4, 6);
            g2.drawLine(9, 9, size - 4, 9);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }
        @Override
        public int getIconHeight() { return size; }
    }
}
