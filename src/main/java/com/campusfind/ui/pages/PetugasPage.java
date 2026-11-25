package com.campusfind.ui.pages;

import com.campusfind.model.Admin;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.CleaningService;
import com.campusfind.model.Item;
import com.campusfind.model.JenisBukti;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KandidatCocokItem;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Laporan;
import com.campusfind.model.Petugas;
import com.campusfind.model.Security;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.User;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingEngine;
import com.campusfind.service.PetugasService;
import com.campusfind.service.QRCodeService;
import com.campusfind.ui.theme.Fonts;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * CampusFind - Dashboard & Antrean Kerja Petugas (Executive Professional Edition)
 * =============================================================================
 * Dikembangkan untuk Role Petugas: Bintang, Jepin, dan Fakhri.
 * Mengimplementasikan 8 Fitur Utama Role Petugas:
 * 1. Dashboard & Antrean Kerja (US-010, US-028)
 * 2. Verifikasi Laporan Masuk (US-011, US-028)
 * 3. Manajemen Lokasi Penyimpanan Fisik Temuan (US-013)
 * 4. Konfirmasi Hasil Smart Matching (US-015)
 * 5. Manajemen & Pelacakan QR Code (US-017, US-018, US-029)
 * 6. Pemeriksaan & Keputusan Klaim Mahasiswa (US-020, US-028)
 * 7. Pencatatan Serah Terima Barang & Penguncian Data (US-021, US-029)
 * 8. Kontrol Transisi Status Berurutan & Logging Audit (US-012)
 *
 * Menerapkan Arsitektur OOP Bersih & Polimorfisme Item.tampilkanDetail() (US-007, US-008).
 * =============================================================================
 */
public class PetugasPage extends JPanel {

    // Palet Warna UI Profesional
    private static final Color COLOR_PAGE_BG = new Color(248, 250, 252);     // Slate 50
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);  // Slate 200
    private static final Color COLOR_PRIMARY = new Color(30, 41, 59);         // Slate 800
    private static final Color COLOR_HEADER_DARK = new Color(15, 23, 42);     // Slate 900
    private static final Color COLOR_ACCENT_BLUE = new Color(37, 99, 235);    // Blue 600
    private static final Color COLOR_ACCENT_AMBER = new Color(217, 119, 6);   // Amber 600
    private static final Color COLOR_ACCENT_PURPLE = new Color(124, 58, 237); // Purple 600
    private static final Color COLOR_ACCENT_GREEN = new Color(5, 150, 105);   // Emerald 600
    private static final Color COLOR_ACCENT_RED = new Color(220, 38, 38);     // Red 600

    private final ItemService itemService;
    private final KlaimService klaimService;
    private final KlaimRepository klaimRepository;
    private final KandidatService kandidatService;
    private final PetugasService petugasService;
    private final QRCodeService qrCodeService;
    private final AppSession appSession;

    private final List<MatchPair> daftarKandidatCocok = new ArrayList<>();

    // KPI Counters
    private final JLabel lblCountVerifikasi = new JLabel("0");
    private final JLabel lblCountKlaim = new JLabel("0");
    private final JLabel lblCountMatching = new JLabel("0");
    private final JLabel lblCountSerahTerima = new JLabel("0");
    private final JLabel lblStatusWorker = new JLabel("Smart Matching: Siap");
    private final JProgressBar progressWorker = new JProgressBar();

    // Tables & Models
    private JTabbedPane tabbedPane;
    private JTable tblVerifikasi, tblKlaim, tblMatching, tblInventaris, tblSerahTerima, tblAuditLog;
    private DefaultTableModel modelVerifikasi, modelKlaim, modelMatching, modelInventaris, modelSerahTerima, modelAuditLog;

    // Aktor Aktif & v2.0 Components
    private JComboBox<String> cmbAktorAktif;
    private JLabel lblPetugasInfo;
    private JLabel lblStatistikAudit;
    private final MatchingEngine matchingEngine = new MatchingEngine();

    // Right Detail Panel
    private JTextArea txtDetailItem;
    private JPanel actionButtonPanel;
    private JLabel lblDetailSubtitle;

    // Action Buttons
    private JButton btnTerimaLaporan, btnTolakLaporan, btnSerahKePosCS;
    private JButton btnKirimOTP, btnVerifikasiOTP, btnUnlockPerangkat, btnSetujuiKlaim, btnTolakKlaim;
    private JButton btnKonfirmasiCocok;
    private JButton btnUpdateLokasi, btnLihatSimpanQR, btnLihatPrivasiPublik, btnArsipkanBarang;
    private JButton btnSerahTerima;

    public static class MatchPair {
        final Item hilang;
        final Item temuan;
        final int skor;

        public MatchPair(Item hilang, Item temuan, int skor) {
            this.hilang = hilang;
            this.temuan = temuan;
            this.skor = skor;
        }

        public Item getHilang() { return hilang; }
        public Item getTemuan() { return temuan; }
        public int getSkor() { return skor; }
    }

    public PetugasPage(ItemService itemService,
                       KlaimService klaimService,
                       KlaimRepository klaimRepository,
                       KandidatService kandidatService,
                       PetugasService petugasService,
                       QRCodeService qrCodeService,
                       AppSession appSession) {
        this.itemService = itemService;
        this.klaimService = klaimService;
        this.klaimRepository = klaimRepository;
        this.kandidatService = kandidatService;
        this.petugasService = petugasService;
        this.qrCodeService = qrCodeService;
        this.appSession = appSession;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(COLOR_PAGE_BG);

        // Pastikan ada data simulasi jika sistem masih kosong
        if (this.petugasService != null) {
            this.petugasService.pastikanDataAwalLengkap(this.klaimRepository);
        }

        initHeaderAndKPI();
        initCenterTabs();
        initRightDetailPanel();

        refreshAllQueues();
    }

    public void setActiveRoleIndex(int idx) {
        if (cmbAktorAktif != null && idx >= 0 && idx < cmbAktorAktif.getItemCount()) {
            cmbAktorAktif.setSelectedIndex(idx);
            perbaruiBannerAktor();
        }
    }

    private User getCurrentActor() {
        if (cmbAktorAktif != null) {
            int idx = cmbAktorAktif.getSelectedIndex();
            if (idx == 1) {
                return new CleaningService("Jevine Jeje (CS)", "CS-2026-01");
            } else if (idx == 2) {
                return new Admin("Lukman Toro (Admin)", "ADM-2026-01");
            }
        }
        if (appSession != null && appSession.isLoggedIn()) {
            return new Security(appSession.getNama(), appSession.getNomorIdentitas());
        }
        return new Security("Bintang Pamungkas", "NIP-251402082");
    }

    private Petugas getCurrentPetugas() {
        User aktor = getCurrentActor();
        if (aktor instanceof Petugas p) {
            return p;
        }
        return new Security(aktor.getNama(), aktor.getNomorIdentitas());
    }

    // =========================================================================
    // 1. Header & KPI Cards (US-010, US-028)
    // =========================================================================
    private void initHeaderAndKPI() {
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Header Banner Atas (2 Baris Rapi Tanpa Overlap)
        JPanel headerCard = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_HEADER_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        headerCard.setOpaque(false);
        headerCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Baris 1: Judul Kiri & Tombol Kontrol Kanan
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblEyebrow = new JLabel("CAMPUSFIND v2.0  •  ANTREAN OPERASIONAL PETUGAS");
        lblEyebrow.setFont(Fonts.sans(10.5f, Font.BOLD));
        lblEyebrow.setForeground(new Color(148, 163, 184));

        JLabel lblTitle = new JLabel("Pusat Kendali Verifikasi, Klaim & QR");
        lblTitle.setFont(Fonts.sans(17f, Font.BOLD));
        lblTitle.setForeground(Color.WHITE);

        titleBox.add(lblEyebrow);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblTitle);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        rightControls.setOpaque(false);

        lblStatusWorker.setFont(Fonts.sans(11.5f, Font.ITALIC));
        lblStatusWorker.setForeground(new Color(203, 213, 225));

        progressWorker.setPreferredSize(new Dimension(70, 12));
        progressWorker.setVisible(false);

        JButton btnLaporBaru = createModernButton("+ Lapor Temuan", COLOR_ACCENT_BLUE, Color.WHITE);
        btnLaporBaru.addActionListener(e -> dialogLaporTemuanLapangan());

        JButton btnScanMatch = createModernButton("★ Pindai Matching", new Color(139, 92, 246), Color.WHITE);
        btnScanMatch.addActionListener(e -> jalankanSmartMatchingSemua());

        JButton btnCariQR = createModernButton("[QR] Lacak QR", new Color(51, 65, 85), Color.WHITE);
        btnCariQR.addActionListener(e -> dialogLacakBarangQR());

        JButton btnRefresh = createModernButton("↻ Segarkan", new Color(71, 85, 105), Color.WHITE);
        btnRefresh.addActionListener(e -> {
            refreshAllQueues();
            JOptionPane.showMessageDialog(this, "Seluruh antrean data berhasil disegarkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        });

        rightControls.add(lblStatusWorker);
        rightControls.add(progressWorker);
        rightControls.add(btnLaporBaru);
        rightControls.add(btnScanMatch);
        rightControls.add(btnCariQR);
        rightControls.add(btnRefresh);

        row1.add(titleBox, BorderLayout.WEST);
        row1.add(rightControls, BorderLayout.EAST);

        // Baris 2: Aktor Switcher & Info Petugas
        JPanel row2 = new JPanel(new BorderLayout(12, 0));
        row2.setOpaque(false);

        JPanel actorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actorPanel.setOpaque(false);
        JLabel lblAktorTitle = new JLabel("Role Aktif (RBAC):");
        lblAktorTitle.setFont(Fonts.sans(12f, Font.BOLD));
        lblAktorTitle.setForeground(new Color(148, 163, 184));

        cmbAktorAktif = new JComboBox<>(new String[]{
                "Bintang Pamungkas — Security (Verifikator)",
                "Jevine Jeje — Cleaning Service (Pelapor Lapangan)",
                "Lukman Toro — Admin (Audit & Pengarsipan)"
        });
        cmbAktorAktif.setFont(Fonts.sans(12f, Font.BOLD));
        cmbAktorAktif.setBackground(Color.WHITE);
        cmbAktorAktif.setForeground(COLOR_HEADER_DARK);
        cmbAktorAktif.addActionListener(e -> perbaruiBannerAktor());

        lblPetugasInfo = new JLabel();
        lblPetugasInfo.setFont(Fonts.sans(11.5f, Font.PLAIN));
        lblPetugasInfo.setForeground(new Color(203, 213, 225));

        actorPanel.add(lblAktorTitle);
        actorPanel.add(cmbAktorAktif);
        row2.add(actorPanel, BorderLayout.WEST);
        row2.add(lblPetugasInfo, BorderLayout.CENTER);

        perbaruiBannerAktor();

        headerCard.add(row1, BorderLayout.NORTH);
        headerCard.add(row2, BorderLayout.SOUTH);

        // 4 KPI Summary Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.add(createKpiCard("1. LAPORAN MASUK (VERIFIKASI)", lblCountVerifikasi, "Menunggu verifikasi petugas", COLOR_ACCENT_BLUE));
        kpiPanel.add(createKpiCard("2. ANTREAN KLAIM MAHASISWA", lblCountKlaim, "Menunggu pemeriksaan bukti", COLOR_ACCENT_AMBER));
        kpiPanel.add(createKpiCard("3. REKOMENDASI COCOK (≥70%)", lblCountMatching, "Pasangan siap konfirmasi", COLOR_ACCENT_PURPLE));
        kpiPanel.add(createKpiCard("4. SIAP SERAH TERIMA", lblCountSerahTerima, "Klaim selesai disetujui", COLOR_ACCENT_GREEN));

        topContainer.add(headerCard, BorderLayout.NORTH);
        topContainer.add(kpiPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
    }

    private JPanel createKpiCard(String title, JLabel countLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                BorderFactory.createCompoundBorder(
                        new MatteBorder(3, 0, 0, 0, accentColor),
                        new EmptyBorder(12, 14, 12, 14)
                )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Fonts.sans(11f, Font.BOLD));
        lblTitle.setForeground(new Color(100, 116, 139)); // Slate 500

        countLabel.setFont(Fonts.sans(26f, Font.BOLD));
        countLabel.setForeground(accentColor);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(Fonts.sans(11f, Font.PLAIN));
        lblSub.setForeground(new Color(148, 163, 184)); // Slate 400

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    // 2. Center Tabbed Tables
    // =========================================================================
    private JScrollPane wrapTableScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new LineBorder(COLOR_CARD_BORDER, 1));
        return sp;
    }

    private void initCenterTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(Fonts.sans(12f, Font.BOLD));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(COLOR_PRIMARY);

        // 1. Verifikasi Laporan
        modelVerifikasi = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Lokasi Kejadian", "Pelapor", "Status"});
        tblVerifikasi = createModernTable(modelVerifikasi);
        setupTableSelection(tblVerifikasi, "VERIFIKASI");
        tabbedPane.addTab("1. Verifikasi", wrapTableScroll(tblVerifikasi));

        // 2. Pemeriksaan Klaim
        modelKlaim = createReadOnlyModel(new String[]{"ID Klaim", "ID Barang", "Pengaju", "Bukti / OTP", "Skor", "Status"});
        tblKlaim = createModernTable(modelKlaim);
        setupTableSelection(tblKlaim, "KLAIM");
        tabbedPane.addTab("2. Klaim & OTP", wrapTableScroll(tblKlaim));

        // 3. Smart Matching
        modelMatching = createReadOnlyModel(new String[]{"ID Hilang", "Nama Barang Hilang", "ID Temuan", "Nama Barang Temuan", "Skor (%)"});
        tblMatching = createModernTable(modelMatching);
        setupTableSelection(tblMatching, "MATCHING");
        tabbedPane.addTab("3. Matching", wrapTableScroll(tblMatching));

        // 4. Inventaris & QR Code
        modelInventaris = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Status", "Lokasi Simpan Fisik", "Kode QR"});
        tblInventaris = createModernTable(modelInventaris);
        setupTableSelection(tblInventaris, "INVENTARIS");
        tabbedPane.addTab("4. Lokasi & QR", wrapTableScroll(tblInventaris));

        // 5. Serah Terima
        modelSerahTerima = createReadOnlyModel(new String[]{"ID Barang", "Nama Barang", "Kategori", "Lokasi Simpan", "Status", "Proteksi"});
        tblSerahTerima = createModernTable(modelSerahTerima);
        setupTableSelection(tblSerahTerima, "SERAH_TERIMA");
        tabbedPane.addTab("5. Serah Terima", wrapTableScroll(tblSerahTerima));

        // 6. Audit Log & Statistik (v2.0)
        JPanel auditPanel = new JPanel(new BorderLayout(8, 8));
        auditPanel.setBackground(Color.WHITE);
        auditPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel auditTopBar = new JPanel(new BorderLayout(8, 8));
        auditTopBar.setOpaque(false);
        lblStatistikAudit = new JLabel("Memuat statistik sistem...");
        lblStatistikAudit.setFont(Fonts.sans(12f, Font.BOLD));
        lblStatistikAudit.setForeground(COLOR_PRIMARY);

        JButton btnArsipTop = createModernButton("Arsipkan Barang Lama (> 60 Hari)", new Color(71, 85, 105), Color.WHITE);
        btnArsipTop.addActionListener(e -> aksiArsipkanBarangLama());

        auditTopBar.add(lblStatistikAudit, BorderLayout.WEST);
        auditTopBar.add(btnArsipTop, BorderLayout.EAST);

        modelAuditLog = createReadOnlyModel(new String[]{"Waktu & Catatan Audit Aktivitas Sistem"});
        tblAuditLog = createModernTable(modelAuditLog);

        auditPanel.add(auditTopBar, BorderLayout.NORTH);
        auditPanel.add(wrapTableScroll(tblAuditLog), BorderLayout.CENTER);
        tabbedPane.addTab("6. Audit Log", auditPanel);

        tabbedPane.addChangeListener(e -> clearDetailPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JTable createModernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(Fonts.sans(12f, Font.PLAIN));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(238, 242, 255)); // Indigo 50
        table.setSelectionForeground(new Color(30, 27, 75));     // Indigo 950
        table.setShowVerticalLines(false);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setFont(Fonts.sans(12f, Font.BOLD));
        header.setBackground(new Color(241, 245, 249)); // Slate 100
        header.setForeground(new Color(51, 65, 85));     // Slate 700
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_CARD_BORDER));

        // Custom Cell Renderer untuk Status & Jenis
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setBorder(new EmptyBorder(0, 10, 0, 10));

                String valStr = (value != null) ? value.toString() : "-";

                // Alternating row background
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(COLOR_PRIMARY);
                }

                // Render Badge Warna pada Kolom Status
                if (valStr.equalsIgnoreCase("DILAPORKAN") || valStr.equalsIgnoreCase("DIAJUKAN")) {
                    c.setForeground(new Color(180, 83, 9)); // Amber
                    if (isSelected) c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (valStr.equalsIgnoreCase("DIVERIFIKASI") || valStr.equalsIgnoreCase("TERSEDIA") || valStr.equalsIgnoreCase("DICARI")) {
                    c.setForeground(new Color(29, 78, 216)); // Blue
                } else if (valStr.equalsIgnoreCase("COCOK")) {
                    c.setForeground(new Color(109, 40, 217)); // Purple
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (valStr.equalsIgnoreCase("DIKLAIM") || valStr.equalsIgnoreCase("DALAM_PROSES_KLAIM") || valStr.equalsIgnoreCase("DISETUJUI")) {
                    c.setForeground(new Color(194, 65, 12)); // Orange
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (valStr.equalsIgnoreCase("DIKEMBALIKAN") || valStr.equalsIgnoreCase("DISERAHKAN") || valStr.equalsIgnoreCase("TERKUNCI")) {
                    c.setForeground(new Color(4, 120, 87)); // Green
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (valStr.toUpperCase().contains("TOLAK") || valStr.equalsIgnoreCase("DITOLAK")) {
                    c.setForeground(new Color(185, 28, 28)); // Red
                } else if (valStr.equalsIgnoreCase("DIARSIPKAN")) {
                    c.setForeground(new Color(100, 116, 139)); // Slate
                    c.setFont(c.getFont().deriveFont(Font.ITALIC));
                }

                // Render Jenis Laporan
                if ("TEMUAN".equalsIgnoreCase(valStr)) {
                    c.setForeground(new Color(5, 150, 105));
                } else if ("HILANG".equalsIgnoreCase(valStr)) {
                    c.setForeground(new Color(225, 29, 72));
                }

                return c;
            }
        });

        return table;
    }

    private DefaultTableModel createReadOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // =========================================================================
    // 3. Right Detail Panel & Action Bar (US-008, US-011 s.d. US-021)
    // =========================================================================
    private void initRightDetailPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        rightPanel.setPreferredSize(new Dimension(360, 0));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        // Header Panel Kanan
        JPanel rightHeader = new JPanel();
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));
        rightHeader.setOpaque(false);

        JLabel lblDetailHeader = new JLabel("Rincian Objek & Tindakan");
        lblDetailHeader.setFont(Fonts.sans(14.5f, Font.BOLD));
        lblDetailHeader.setForeground(COLOR_HEADER_DARK);

        lblDetailSubtitle = new JLabel("<html>Pilih baris tabel untuk melihat detail polimorfik & aksi.</html>");
        lblDetailSubtitle.setFont(Fonts.sans(11.5f, Font.PLAIN));
        lblDetailSubtitle.setForeground(new Color(100, 116, 139));

        rightHeader.add(lblDetailHeader);
        rightHeader.add(Box.createVerticalStrut(3));
        rightHeader.add(lblDetailSubtitle);

        // Text Area Detail Rincian (US-008 Polimorfisme Item.tampilkanDetail())
        txtDetailItem = new JTextArea();
        txtDetailItem.setEditable(false);
        txtDetailItem.setLineWrap(true);
        txtDetailItem.setWrapStyleWord(true);
        txtDetailItem.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetailItem.setBackground(new Color(248, 250, 252));
        txtDetailItem.setForeground(new Color(30, 41, 59));
        txtDetailItem.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollDetail = new JScrollPane(txtDetailItem);
        scrollDetail.getViewport().setBackground(new Color(248, 250, 252));
        scrollDetail.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

        // Action Button Panel (Dinamis sesuai Tab)
        actionButtonPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        actionButtonPanel.setOpaque(false);

        // Definisi Tombol Aksi Petugas & v2.0
        btnTerimaLaporan = createModernButton("✓ Terima & Verifikasi (DIVERIFIKASI)", COLOR_ACCENT_GREEN, Color.WHITE);
        btnTerimaLaporan.addActionListener(e -> aksiTerimaLaporan());

        btnTolakLaporan = createModernButton("✕ Tolak Laporan (Wajib Alasan)", COLOR_ACCENT_RED, Color.WHITE);
        btnTolakLaporan.addActionListener(e -> aksiTolakLaporan());

        btnSerahKePosCS = createModernButton("Serahkan ke Pos Security (CS)", new Color(2, 132, 199), Color.WHITE);
        btnSerahKePosCS.addActionListener(e -> aksiSerahkanKePosCS());

        btnKirimOTP = createModernButton("Kirim Tantangan OTP ke Akun", new Color(124, 58, 237), Color.WHITE);
        btnKirimOTP.addActionListener(e -> aksiKirimOTP());

        btnVerifikasiOTP = createModernButton("Masukkan & Cek Kode OTP", new Color(79, 70, 229), Color.WHITE);
        btnVerifikasiOTP.addActionListener(e -> aksiVerifikasiOTP());

        btnUnlockPerangkat = createModernButton("Konfirmasi Buka Layar / Recovery", new Color(13, 148, 136), Color.WHITE);
        btnUnlockPerangkat.addActionListener(e -> aksiUnlockPerangkat());

        btnSetujuiKlaim = createModernButton("✓ Setujui Klaim -> DIKLAIM", COLOR_ACCENT_BLUE, Color.WHITE);
        btnSetujuiKlaim.addActionListener(e -> aksiSetujuiKlaim());

        btnTolakKlaim = createModernButton("✕ Tolak Klaim (Wajib Alasan)", COLOR_ACCENT_RED, Color.WHITE);
        btnTolakKlaim.addActionListener(e -> aksiTolakKlaim());

        btnKonfirmasiCocok = createModernButton("★ Konfirmasi Kecocokan -> COCOK", COLOR_ACCENT_PURPLE, Color.WHITE);
        btnKonfirmasiCocok.addActionListener(e -> aksiKonfirmasiCocok());

        btnUpdateLokasi = createModernButton("Atur Lokasi Simpan Fisik", COLOR_ACCENT_AMBER, Color.WHITE);
        btnUpdateLokasi.addActionListener(e -> aksiUpdateLokasiSimpan());

        btnLihatSimpanQR = createModernButton("Lihat & Simpan QR (.PNG)", new Color(14, 165, 233), Color.WHITE);
        btnLihatSimpanQR.addActionListener(e -> aksiLihatDanSimpanQR());

        btnLihatPrivasiPublik = createModernButton("Tampilan Publik (Privasi)", new Color(71, 85, 105), Color.WHITE);
        btnLihatPrivasiPublik.addActionListener(e -> aksiLihatPrivasiPublik());

        btnArsipkanBarang = createModernButton("Arsipkan Barang Lama (> 60 Hr)", new Color(100, 116, 139), Color.WHITE);
        btnArsipkanBarang.addActionListener(e -> aksiArsipkanBarangLama());

        btnSerahTerima = createModernButton("Proses Serah Terima -> DIKEMBALIKAN", COLOR_ACCENT_GREEN, Color.WHITE);
        btnSerahTerima.addActionListener(e -> aksiSerahTerimaBarang());

        rightPanel.add(rightHeader, BorderLayout.NORTH);
        rightPanel.add(scrollDetail, BorderLayout.CENTER);
        rightPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Fonts.sans(12f, Font.BOLD));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private void setupTableSelection(JTable table, String tipeTab) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                tampilkanDetailBaris(table.getSelectedRow(), tipeTab);
            }
        });
    }

    // =========================================================================
    // 4. Integrasi Polimorfisme & Rincian Baris (US-007, US-008)
    // =========================================================================
    private void tampilkanDetailBaris(int row, String tipeTab) {
        actionButtonPanel.removeAll();

        if ("VERIFIKASI".equals(tipeTab)) {
            String id = (String) modelVerifikasi.getValueAt(row, 0);
            itemService.findById(id).ifPresent(item -> {
                txtDetailItem.setText(formatDetailLengkap(item));
                if (getCurrentActor() instanceof CleaningService) {
                    actionButtonPanel.setLayout(new GridLayout(3, 1, 6, 6));
                    actionButtonPanel.add(btnSerahKePosCS);
                    actionButtonPanel.add(btnTerimaLaporan);
                    actionButtonPanel.add(btnTolakLaporan);
                } else {
                    actionButtonPanel.setLayout(new GridLayout(2, 1, 8, 8));
                    actionButtonPanel.add(btnTerimaLaporan);
                    actionButtonPanel.add(btnTolakLaporan);
                }
            });
        } else if ("KLAIM".equals(tipeTab)) {
            String idKlaim = (String) modelKlaim.getValueAt(row, 0);
            klaimRepository.findById(idKlaim).ifPresent(klaim -> {
                Item item = itemService.findById(klaim.getItemId()).orElse(null);
                String syaratStr = (item != null)
                        ? Arrays.stream(item.getSyaratBuktiUtama()).map(JenisBukti::getLabel).collect(Collectors.joining(", "))
                        : "-";
                String statusOtp = klaim.isOtpTerverifikasi()
                        ? "✅ TERVERIFIKASI (Pemilik Menguasai Akun/Perangkat)"
                        : (klaim.getKodeOTP() != null ? "⏳ Tantangan OTP Terkirim (Menunggu Input Pemilik)" : "⚠ Belum Diverifikasi OTP / Bukti Akun");

                String detailKlaim = "========================================\n" +
                        "  PEMERIKSAAN KLAIM & CHALLENGE-RESPONSE (v2.0)\n" +
                        "========================================\n" +
                        "ID Klaim       : " + klaim.getId() + "\n" +
                        "Status Klaim   : " + klaim.getStatus().name() + "\n" +
                        "Skor Algoritma : " + klaim.getSkorKecocokan() + "% Kecocokan\n" +
                        "Pengaju Klaim  : " + klaim.getPengklaim().getNama() + " (" + klaim.getPengklaim().getNomorIdentitas() + ")\n" +
                        "Syarat Bukti   : " + syaratStr + "\n" +
                        "Status Verif.  : " + statusOtp + "\n" +
                        "Bukti Diajukan : " + klaim.getCiriKhusus() + "\n" +
                        "Lampiran File  : " + (klaim.getBuktiPath() != null ? klaim.getBuktiPath() : "(Tidak ada lampiran)") + "\n\n" +
                        (item != null ? formatDetailLengkap(item) : "Barang tidak ditemukan.");
                txtDetailItem.setText(detailKlaim);

                boolean isElektronik = (item instanceof BarangElektronik);
                if (isElektronik) {
                    actionButtonPanel.setLayout(new GridLayout(5, 1, 6, 6));
                    actionButtonPanel.add(btnKirimOTP);
                    actionButtonPanel.add(btnVerifikasiOTP);
                    actionButtonPanel.add(btnUnlockPerangkat);
                    actionButtonPanel.add(btnSetujuiKlaim);
                    actionButtonPanel.add(btnTolakKlaim);
                } else {
                    actionButtonPanel.setLayout(new GridLayout(2, 1, 8, 8));
                    actionButtonPanel.add(btnSetujuiKlaim);
                    actionButtonPanel.add(btnTolakKlaim);
                }
            });
        } else if ("MATCHING".equals(tipeTab)) {
            if (row < daftarKandidatCocok.size()) {
                MatchPair match = daftarKandidatCocok.get(row);
                String txt = "========================================\n" +
                        "  REKOMENDASI SMART MATCHING (US-015)\n" +
                        "========================================\n" +
                        "Skor Kemiripan : " + match.skor + "% (Ambang Batas >= 70%)\n\n" +
                        "--- LAPORAN HILANG ---\n" +
                        "ID Barang : " + match.hilang.getId() + "\n" +
                        "Nama      : " + match.hilang.getNama() + "\n" +
                        "Lokasi    : " + match.hilang.getLokasiKejadian() + "\n" +
                        "Pelapor   : " + (match.hilang.getPelapor() != null ? match.hilang.getPelapor().getNama() : "-") + "\n\n" +
                        "--- BARANG TEMUAN ---\n" +
                        "ID Barang : " + match.temuan.getId() + "\n" +
                        "Nama      : " + match.temuan.getNama() + "\n" +
                        "Lokasi    : " + match.temuan.getLokasiKejadian() + "\n" +
                        "Pos Simpan: " + petugasService.getLokasiSimpan(match.temuan) + "\n\n" +
                        "Aksi Petugas:\n" +
                        "Tekan 'Konfirmasi Status -> COCOK' untuk menetapkan kedua barang saling berpasangan resmi.";
                txtDetailItem.setText(txt);
                actionButtonPanel.setLayout(new GridLayout(1, 1, 8, 8));
                actionButtonPanel.add(btnKonfirmasiCocok);
            }
        } else if ("INVENTARIS".equals(tipeTab)) {
            String id = (String) modelInventaris.getValueAt(row, 0);
            itemService.findById(id).ifPresent(item -> {
                txtDetailItem.setText(formatDetailLengkap(item));
                actionButtonPanel.setLayout(new GridLayout(4, 1, 6, 6));
                actionButtonPanel.add(btnUpdateLokasi);
                actionButtonPanel.add(btnLihatSimpanQR);
                actionButtonPanel.add(btnLihatPrivasiPublik);
                actionButtonPanel.add(btnArsipkanBarang);
            });
        } else if ("SERAH_TERIMA".equals(tipeTab)) {
            String id = (String) modelSerahTerima.getValueAt(row, 0);
            itemService.findById(id).ifPresent(item -> {
                txtDetailItem.setText(formatDetailLengkap(item));
                actionButtonPanel.setLayout(new GridLayout(1, 1, 8, 8));
                if (!petugasService.isTerkunci(item)) {
                    actionButtonPanel.add(btnSerahTerima);
                } else {
                    JLabel lblLocked = new JLabel("🔒 Data barang sudah dikembalikan & terkunci permanen.", SwingConstants.CENTER);
                    lblLocked.setFont(Fonts.sans(11.5f, Font.ITALIC));
                    lblLocked.setForeground(COLOR_ACCENT_GREEN);
                    actionButtonPanel.add(lblLocked);
                }
            });
        }

        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }

    private void perbaruiBannerAktor() {
        User aktor = getCurrentActor();
        if (lblPetugasInfo != null) {
            lblPetugasInfo.setText(aktor.tampilDashboard() + "  |  ID: " + aktor.getNomorIdentitas());
        }
        clearDetailPanel();
    }

    /**
     * Memanfaatkan method polimorfik item.tampilkanDetail() (US-008) & item.getSyaratBuktiUtama() (v2.0)
     * dan menggabungkannya dengan metadata petugas (Lokasi simpan fisik, QR Code, dan Audit Log US-012).
     */
    private String formatDetailLengkap(Item item) {
        // Pemanggilan Method Polimorfik Asli US-008 & v2.0
        String polymorphicDetails = item.tampilkanDetail();
        String syaratBukti = Arrays.stream(item.getSyaratBuktiUtama())
                .map(JenisBukti::getLabel)
                .collect(Collectors.joining(", "));

        String qrStr = petugasService.getKodeQR(item);
        String lokasiFisik = petugasService.getLokasiSimpan(item);
        String auditLogs = petugasService.getRiwayatLog(item.getId());

        return polymorphicDetails + "\n" +
                "Syarat Bukti    : " + syaratBukti + "\n" +
                "Lokasi Simpan   : " + lokasiFisik + "\n" +
                "Kode QR Unik    : " + qrStr + "\n\n" +
                "--- RIWAYAT AUDIT STATUS OLEH PETUGAS (US-012) ---\n" +
                auditLogs;
    }

    // =========================================================================
    // 5. Eksekusi Aksi Petugas (US-011 s.d. US-021)
    // =========================================================================

    // US-011: Verifikasi Laporan (Terima)
    private void aksiTerimaLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        try {
            Item item = itemService.findById(id).orElseThrow();
            petugasService.verifikasiLaporan(id, true, null, getCurrentPetugas());

            String msgQR = "";
            if (item.getJenis() == JenisLaporan.TEMUAN) {
                msgQR = "\nKode QR unik resmi (" + petugasService.getKodeQR(item) + ") otomatis dibuat.";
            }

            JOptionPane.showMessageDialog(this,
                    "Laporan '" + item.getNama() + "' (" + id + ") berhasil diverifikasi!" + msgQR +
                            "\nBarang kini aktif dalam sistem dan otomatis masuk ke proses Smart Matching.",
                    "Verifikasi Berhasil (US-011)", JOptionPane.INFORMATION_MESSAGE);

            refreshAllQueues();
            jalankanSmartMatchingBackground(item);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal Verifikasi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // US-011: Verifikasi Laporan (Tolak - Wajib Alasan)
    private void aksiTolakLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan laporan (Wajib diisi & akan dikirim ke pelapor):",
                "Tolak Laporan Palsu / Tidak Valid (US-011)", JOptionPane.WARNING_MESSAGE);
        if (alasan == null) return;

        if (alasan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alasan penolakan wajib diisi secara tertulis!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        try {
            petugasService.verifikasiLaporan(id, false, alasan.trim(), getCurrentPetugas());
            JOptionPane.showMessageDialog(this,
                    "Laporan ditolak secara resmi. Laporan ini tidak akan disertakan dalam pencocokan.",
                    "Laporan Ditolak", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // US-010 & US-028: Pemrosesan Matching di Latar Belakang (SwingWorker)
    private void jalankanSmartMatchingBackground(Item itemBaru) {
        SwingWorker<List<MatchPair>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MatchPair> doInBackground() {
                SwingUtilities.invokeLater(() -> {
                    lblStatusWorker.setText("Smart Matching: Menganalisis...");
                    progressWorker.setIndeterminate(true);
                    progressWorker.setVisible(true);
                });

                List<MatchPair> temuanBaru = new ArrayList<>();
                if (kandidatService == null) return temuanBaru;

                try {
                    if (itemBaru.getJenis() == JenisLaporan.HILANG && itemBaru.getPelapor() != null) {
                        List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(
                                itemBaru.getId(), itemBaru.getPelapor().getNomorIdentitas());
                        for (KandidatCocokItem k : kandidat) {
                            if (k.getSkor() >= 70) {
                                temuanBaru.add(new MatchPair(itemBaru, k.getItemTemuan(), k.getSkor()));
                            }
                        }
                    } else if (itemBaru.getJenis() == JenisLaporan.TEMUAN) {
                        for (Item hilang : itemService.findByJenis(JenisLaporan.HILANG)) {
                            if (hilang.getPelapor() != null && (hilang.getStatus() == StatusItem.DICARI || hilang.getStatus() == StatusItem.DIVERIFIKASI)) {
                                List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(
                                        hilang.getId(), hilang.getPelapor().getNomorIdentitas());
                                for (KandidatCocokItem k : kandidat) {
                                    if (k.getItemTemuan().getId().equals(itemBaru.getId()) && k.getSkor() >= 70) {
                                        temuanBaru.add(new MatchPair(hilang, itemBaru, k.getSkor()));
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                return temuanBaru;
            }

            @Override
            protected void done() {
                try {
                    List<MatchPair> hasil = get();
                    for (MatchPair mp : hasil) {
                        boolean ada = daftarKandidatCocok.stream()
                                .anyMatch(e -> e.hilang.getId().equals(mp.hilang.getId())
                                        && e.temuan.getId().equals(mp.temuan.getId()));
                        if (!ada) {
                            daftarKandidatCocok.add(mp);
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    lblStatusWorker.setText("Smart Matching: Siap");
                    progressWorker.setIndeterminate(false);
                    progressWorker.setVisible(false);
                    refreshAllQueues();
                }
            }
        };
        worker.execute();
    }

    private void jalankanSmartMatchingSemua() {
        SwingWorker<List<MatchPair>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MatchPair> doInBackground() {
                SwingUtilities.invokeLater(() -> {
                    lblStatusWorker.setText("Smart Matching: Memindai semua...");
                    progressWorker.setIndeterminate(true);
                    progressWorker.setVisible(true);
                });

                List<MatchPair> list = new ArrayList<>();
                if (kandidatService == null) return list;

                for (Item hilang : itemService.findByJenis(JenisLaporan.HILANG)) {
                    if ((hilang.getStatus() == StatusItem.DICARI || hilang.getStatus() == StatusItem.DIVERIFIKASI)
                            && hilang.getPelapor() != null) {
                        try {
                            Laporan lapHilang = Laporan.dariItem(hilang);
                            List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(
                                    hilang.getId(), hilang.getPelapor().getNomorIdentitas());
                            for (KandidatCocokItem k : kandidat) {
                                Laporan lapTemuan = Laporan.dariItem(k.getItemTemuan());
                                if (k.getSkor() >= 70 && matchingEngine.praFilter(lapHilang, lapTemuan)) {
                                    list.add(new MatchPair(hilang, k.getItemTemuan(), k.getSkor()));
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                return list;
            }

            @Override
            protected void done() {
                try {
                    daftarKandidatCocok.clear();
                    daftarKandidatCocok.addAll(get());
                } catch (Exception ignored) {
                } finally {
                    lblStatusWorker.setText("Smart Matching: Siap");
                    progressWorker.setIndeterminate(false);
                    progressWorker.setVisible(false);
                    refreshAllQueues();
                    tabbedPane.setSelectedIndex(2); // Pindah ke Tab Rekomendasi Cocok
                }
            }
        };
        worker.execute();
    }

    // US-020: Persetujuan Klaim (Setujui Klaim & Otomatis Tolak Klaim Lain)
    private void aksiSetujuiKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Setujui klaim " + idKlaim + "?\n" +
                        "Status barang akan berubah menjadi DIKLAIM, notifikasi dikirim ke pemilik,\n" +
                        "dan klaim-klaim lain pada barang yang sama akan otomatis ditolak.",
                "Konfirmasi Persetujuan Klaim (US-020)", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        try {
            klaimService.setujui(idKlaim, getCurrentPetugas());
            JOptionPane.showMessageDialog(this,
                    "Klaim berhasil disetujui! Barang siap diambil pada Pos Satpam (Status: DIKLAIM).",
                    "Klaim Disetujui", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyetujui klaim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // US-020: Penolakan Klaim (Wajib Alasan Minimal 5 Karakter)
    private void aksiTolakKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan klaim (Wajib diisi, minimal 5 karakter):",
                "Tolak Klaim (US-020)", JOptionPane.WARNING_MESSAGE);
        if (alasan == null) return;

        if (alasan.trim().length() < 5) {
            JOptionPane.showMessageDialog(this, "Alasan penolakan tidak valid (minimal 5 karakter)!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        try {
            klaimService.tolak(idKlaim, alasan.trim(), getCurrentPetugas());
            JOptionPane.showMessageDialog(this,
                    "Klaim telah ditolak dan status barang dikembalikan menjadi TERSEDIA untuk diklaim pemilik sah.",
                    "Klaim Ditolak", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menolak klaim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // US-015: Konfirmasi Status Smart Matching -> COCOK
    private void aksiKonfirmasiCocok() {
        int row = tblMatching.getSelectedRow();
        if (row == -1) return;

        MatchPair match = daftarKandidatCocok.remove(row);
        try {
            petugasService.ubahStatusBarang(match.hilang.getId(), StatusItem.COCOK, getCurrentPetugas());
            petugasService.ubahStatusBarang(match.temuan.getId(), StatusItem.COCOK, getCurrentPetugas());

            JOptionPane.showMessageDialog(this,
                    "Pasangan barang (" + match.hilang.getId() + " & " + match.temuan.getId() + ") berhasil dikonfirmasi!\n" +
                            "Status kedua barang kini resmi menjadi COCOK.",
                    "Konfirmasi Berhasil (US-015)", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal Konfirmasi: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // US-013: Manajemen Lokasi Penyimpanan Fisik (Khusus Temuan)
    private void aksiUpdateLokasiSimpan() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        Item item = itemService.findById(id).orElse(null);
        if (item == null) return;

        // Validasi US-013: Hanya Temuan
        if (item.getJenis() != JenisLaporan.TEMUAN) {
            JOptionPane.showMessageDialog(this,
                    "Sistem Menolak: Lokasi penyimpanan fisik HANYA dapat diisi pada barang berjenis TEMUAN!\n" +
                            "(Barang Hilang berada di luar kepemilikan fisik pos).",
                    "Akses Ditolak (US-013)", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentLoc = petugasService.getLokasiSimpan(item);
        String lokasiBaru = JOptionPane.showInputDialog(this,
                "Masukkan lokasi penyimpanan fisik di Pos Satpam (contoh: 'Loker B-2 Ruang Satpam'):",
                "-".equals(currentLoc) ? "Loker B-2 Ruang Satpam" : currentLoc);
        if (lokasiBaru == null || lokasiBaru.trim().isEmpty()) return;

        try {
            petugasService.aturLokasiPenyimpanan(id, lokasiBaru.trim(), getCurrentPetugas());
            JOptionPane.showMessageDialog(this, "Lokasi penyimpanan fisik berhasil diperbarui menjadi: " + lokasiBaru.trim(),
                    "Lokasi Tersimpan (US-013)", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ditolak", JOptionPane.ERROR_MESSAGE);
        }
    }

    // US-017 & US-029: Cetak & Simpan Gambar QR Code (.PNG)
    private void aksiLihatDanSimpanQR() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        Item item = itemService.findById(id).orElse(null);
        if (item == null) return;

        if (item.getJenis() != JenisLaporan.TEMUAN) {
            JOptionPane.showMessageDialog(this,
                    "QR Code unik hanya diterbitkan untuk barang berjenis TEMUAN yang diamankan pos.",
                    "QR Tidak Tersedia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kodeQr = petugasService.getKodeQR(item);
        BufferedImage qrImage = qrCodeService.generateQRImage(kodeQr, 260, 260);

        JPanel panelQR = new JPanel(new BorderLayout(10, 10));
        panelQR.setBackground(Color.WHITE);
        panelQR.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel lblTitle = new JLabel("<html><center><b>KODE QR RESMI BARANG TEMUAN</b><br>" +
                "<font color='#2563EB'>" + kodeQr + "</font> (" + item.getNama() + ")<br>" +
                "<font color='#64748B'>Lokasi Simpan: " + petugasService.getLokasiSimpan(item) + "</font></center></html>", SwingConstants.CENTER);
        lblTitle.setFont(Fonts.sans(13f, Font.PLAIN));

        panelQR.add(lblTitle, BorderLayout.NORTH);
        panelQR.add(new JLabel(new ImageIcon(qrImage)), BorderLayout.CENTER);

        int pilihan = JOptionPane.showOptionDialog(this, panelQR,
                "Manajemen QR Code Barang (US-017)",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"💾 Simpan Gambar .PNG", "Tutup"}, "💾 Simpan Gambar .PNG");

        if (pilihan == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Gambar QR Code Barang (.PNG)");
            fileChooser.setSelectedFile(new File(kodeQr + ".png"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Gambar PNG (*.png)", "png"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File targetFile = fileChooser.getSelectedFile();
                    File saved = qrCodeService.saveQRToPng(kodeQr, targetFile, 350);
                    JOptionPane.showMessageDialog(this,
                            "File gambar QR Code berhasil disimpan ke:\n" + saved.getAbsolutePath() +
                                    "\nSilakan cetak dan tempelkan pada fisik barang temuan.",
                            "QR Tersimpan", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan file QR: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // US-018 & US-029: Pelacakan Barang via QR Code (Ketik Kode atau Buka File Gambar)
    private void dialogLacakBarangQR() {
        String[] opsi = {"⌨ Ketik Kode Unik QR", "📁 Buka File Gambar QR (.PNG/.JPG)"};
        int mode = JOptionPane.showOptionDialog(this,
                "Pilih metode pelacakan barang via QR Code:",
                "Pelacakan & Pelacakan Barang via QR (US-018)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opsi, opsi[0]);

        String kodeDicari = null;
        try {
            if (mode == 0) {
                kodeDicari = JOptionPane.showInputDialog(this,
                        "Masukkan Kode QR unik atau ID Barang (contoh: QR-T-101 atau T-101):",
                        "Lacak Barang via Kode QR", JOptionPane.QUESTION_MESSAGE);
            } else if (mode == 1) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Pilih File Gambar QR Code untuk Didekode");
                chooser.setFileFilter(new FileNameExtensionFilter("Berkas Gambar QR (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    kodeDicari = qrCodeService.decodeQRFromFile(chooser.getSelectedFile());
                }
            }

            if (kodeDicari == null || kodeDicari.trim().isEmpty()) return;

            Item ditemukan = petugasService.cariBarangByQR(kodeDicari.trim());
            JOptionPane.showMessageDialog(this,
                    formatDetailLengkap(ditemukan),
                    "Hasil Pelacakan Barang: " + kodeDicari,
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Pelacakan QR", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Dokumen Konsep v2.0: Aksi Cleaning Service Menyerahkan Barang ke Pos Security
    private void aksiSerahkanKePosCS() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;
        String id = (String) modelVerifikasi.getValueAt(row, 0);

        User aktor = getCurrentActor();
        if (!(aktor instanceof CleaningService cs)) {
            JOptionPane.showMessageDialog(this, "Aksi ini dikhususkan untuk Cleaning Service (CS) saat menyerahkan barang ke pos Security.",
                    "Info Role", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String lokasiPos = JOptionPane.showInputDialog(this,
                "Masukkan lokasi rak/loker pos tujuan penyerahan (contoh: Loker Pos Security Gedung A):",
                "Penyerahan Barang oleh Cleaning Service (v2.0)", JOptionPane.QUESTION_MESSAGE);
        if (lokasiPos == null) return;

        try {
            petugasService.serahkanBarangKePos(id, cs, lokasiPos);
            JOptionPane.showMessageDialog(this,
                    "Barang " + id + " telah dicatat diserahkan ke Pos Security.\nMenunggu verifikasi oleh Security (Verifikator).",
                    "Penyerahan Tercatat", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dokumen Konsep v2.0: Lapor Temuan Lapangan oleh Security / Cleaning Service / Admin (Interface Pelapor)
    private void dialogLaporTemuanLapangan() {
        JComboBox<String> cmbKat = new JComboBox<>(new String[]{"Elektronik (Verifikasi Akun/OTP)", "Non-Elektronik (Ciri Fisik)"});
        JTextField txtNama = new JTextField();
        JTextField txtLokasi = new JTextField();
        JTextField txtAttr1 = new JTextField(); // Merk atau Warna
        JTextField txtAttr2 = new JTextField(); // Akun Terdaftar atau Bahan
        JTextField txtSeri = new JTextField();
        JTextField txtCiri = new JTextField();

        JPanel panel = new JPanel(new GridLayout(7, 2, 6, 6));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Jenis Barang:"));
        panel.add(cmbKat);
        panel.add(new JLabel("Nama Barang Temuan:"));
        panel.add(txtNama);
        panel.add(new JLabel("Lokasi Ditemukan:"));
        panel.add(txtLokasi);
        panel.add(new JLabel("Merk (Elektronik) / Warna (Non):"));
        panel.add(txtAttr1);
        panel.add(new JLabel("Akun Terdaftar / Bahan:"));
        panel.add(txtAttr2);
        panel.add(new JLabel("Nomor Seri (Cadangan / Opsional):"));
        panel.add(txtSeri);
        panel.add(new JLabel("Ciri Khusus Fisik:"));
        panel.add(txtCiri);

        int res = JOptionPane.showConfirmDialog(this, panel,
                "Buat Laporan Temuan Lapangan (" + getCurrentActor().getNama() + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            if (txtNama.getText().isBlank() || txtLokasi.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Nama barang dan lokasi wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newId = "T-" + (200 + (int) (Math.random() * 799));
            Item baru;
            if (cmbKat.getSelectedIndex() == 0) {
                baru = new BarangElektronik(
                        newId, JenisLaporan.TEMUAN, txtNama.getText().trim(), KategoriBarang.ELEKTRONIK,
                        "Temuan lapangan oleh " + getCurrentActor().getNama(), txtLokasi.getText().trim(),
                        LocalDate.now(), "POS-0001", null, txtCiri.getText().trim(),
                        StatusItem.DILAPORKAN, LocalDateTime.now(), getCurrentActor(),
                        txtAttr1.getText().trim(), txtSeri.getText().trim(), "Standard", txtAttr2.getText().trim()
                );
            } else {
                baru = new BarangNonElektronik(
                        newId, JenisLaporan.TEMUAN, txtNama.getText().trim(), KategoriBarang.TAS_DOMPET,
                        "Temuan lapangan oleh " + getCurrentActor().getNama(), txtLokasi.getText().trim(),
                        LocalDate.now(), "POS-0001", null, txtCiri.getText().trim(),
                        StatusItem.DILAPORKAN, LocalDateTime.now(), getCurrentActor(),
                        txtAttr1.getText().trim(), txtAttr2.getText().trim()
                );
            }
            try {
                petugasService.laporkanTemuanLapangan(baru, getCurrentPetugas());
                JOptionPane.showMessageDialog(this,
                        "Laporan temuan lapangan [" + newId + "] berhasil disimpan!\nSilakan lakukan verifikasi oleh Security.",
                        "Berhasil (Pelapor v2.0)", JOptionPane.INFORMATION_MESSAGE);
                refreshAllQueues();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Dokumen Konsep v2.0 (Section 3.5): Challenge-Response Kirim Tantangan OTP
    private void aksiKirimOTP() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;
        String idKlaim = (String) modelKlaim.getValueAt(row, 0);

        try {
            String otp = klaimService.kirimTantanganOTP(idKlaim, getCurrentPetugas());
            JOptionPane.showMessageDialog(this,
                    "Tantangan OTP berhasil dikirim ke akun/nomor terdaftar pengklaim!\n\n" +
                            "[SIMULASI NOTIFIKASI PENGKLAIM]\nKode OTP Anda: " + otp + "\n\n" +
                            "Petugas Security tidak perlu mengakses akun/perangkat; minta pengklaim membacakan/memasukkan OTP ini.",
                    "Challenge-Response OTP (v2.0)", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Akses / Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dokumen Konsep v2.0 (Section 3.5): Cek Input OTP dari Pemilik
    private void aksiVerifikasiOTP() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;
        String idKlaim = (String) modelKlaim.getValueAt(row, 0);

        String input = JOptionPane.showInputDialog(this,
                "Masukkan 6-digit Kode OTP yang diterima pemilik pada akun terdaftarnya:",
                "Verifikasi Challenge-Response OTP", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.isBlank()) return;

        try {
            boolean cocok = klaimService.verifikasiTantanganOTP(idKlaim, input);
            if (cocok) {
                JOptionPane.showMessageDialog(this,
                        "✅ OTP COCOK! Pengklaim terbukti menguasai akun terdaftar perangkat elektronik.\n" +
                                "Anda kini dapat menyetujui klaim dengan aman.",
                        "Verifikasi OTP Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ OTP TIDAK COCOK! Verifikasi kepemilikan gagal.",
                        "OTP Tidak Valid", JOptionPane.WARNING_MESSAGE);
            }
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dokumen Konsep v2.0: Alternatif Buka Kunci Layar / Recovery Akun di hadapan Security
    private void aksiUnlockPerangkat() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;
        String idKlaim = (String) modelKlaim.getValueAt(row, 0);

        String[] metode = {"Membuka Kunci Layar Perangkat (PIN/Sidik Jari)", "Verifikasi Recovery Akun Perangkat"};
        int pilih = JOptionPane.showOptionDialog(this,
                "Konfirmasi pembuktian langsung kepemilikan perangkat elektronik di pos:",
                "Verifikasi Perangkat (v2.0)", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, metode, metode[0]);
        if (pilih < 0) return;

        try {
            JenisBukti jb = (pilih == 0) ? JenisBukti.UNLOCK_PERANGKAT : JenisBukti.RECOVERY_AKUN;
            klaimService.verifikasiBuktiPerangkatTerbuka(idKlaim, jb, getCurrentPetugas());
            JOptionPane.showMessageDialog(this,
                    "✅ Pembuktian (" + jb.getLabel() + ") tercatat berhasil!",
                    "Terverifikasi", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Akses Ditolak", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Dokumen Konsep v2.0 (Section 2.6 & 3.3): Tampilan Publik dengan Privasi Pelapor Terlindungi
    private void aksiLihatPrivasiPublik() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;
        String id = (String) modelInventaris.getValueAt(row, 0);
        itemService.findById(id).ifPresent(item -> {
            Laporan laporan = Laporan.dariItem(item);
            JOptionPane.showMessageDialog(this,
                    "=== TAMPILAN PUBLIK (MAHASISWA LAIN) ===\n" +
                            laporan.tampilkanKePublik() + "\n\n" +
                            "=== PERBANDINGAN AKSES PETUGAS BERWENANG ===\n" +
                            "Pelapor Asli: " + (laporan.getPelapor() != null ? laporan.getPelapor().getNama() : "-"),
                    "Perlindungan Privasi Pelapor (v2.0)", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // Dokumen Konsep v2.0 (Section 2.6): Pengarsipan Barang Lama (> 60 Hari) -> DIARSIPKAN
    private void aksiArsipkanBarangLama() {
        try {
            int jml = petugasService.arsipkanBarangLama(60, getCurrentActor());
            if (jml > 0) {
                JOptionPane.showMessageDialog(this,
                        "Berhasil mengarsipkan " + jml + " barang lama (> 60 hari tanpa klaim) menjadi status DIARSIPKAN.\n" +
                                "Antrean pos & database kini tetap bersih dan bebas penumpukan.",
                        "Pengarsipan Berhasil (v2.0)", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Tidak ada barang aktif yang melebihi batas usia 60 hari saat ini.",
                        "Informasi Pengarsipan", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Akses Ditolak", JOptionPane.ERROR_MESSAGE);
        }
    }

    // US-021 & US-029: Pencatatan Serah Terima Barang & Penguncian Data
    private void aksiSerahTerimaBarang() {
        int row = tblSerahTerima.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelSerahTerima.getValueAt(row, 0);
        Item item = itemService.findById(id).orElse(null);
        if (item == null) return;

        if (petugasService.isTerkunci(item)) {
            JOptionPane.showMessageDialog(this, "Barang ini sudah diserahterimakan dan terkunci permanen!", "Data Terkunci", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtPenerima = new JTextField();
        JTextField txtTanggal = new JTextField(LocalDate.now().toString());
        JTextField txtPetugasSerah = new JTextField(getCurrentPetugas().getNama());
        txtPetugasSerah.setEditable(false);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Nama Penerima / Pemilik Sah:"));
        form.add(txtPenerima);
        form.add(new JLabel("Tanggal Penyerahan Fisik:"));
        form.add(txtTanggal);
        form.add(new JLabel("Petugas Pos yang Menyerahkan:"));
        form.add(txtPetugasSerah);
        form.add(new JLabel("Peringatan Sistem:"));
        form.add(new JLabel("<html><font color='#DC2626'><b>Status akan DIKEMBALIKAN & Data Terkunci</b></font></html>"));

        int res = JOptionPane.showConfirmDialog(this, form,
                "Formulir Berita Acara Serah Terima Barang (" + item.getId() + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String penerima = txtPenerima.getText();
            if (penerima == null || penerima.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama penerima barang wajib dicatat!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                petugasService.prosesSerahTerima(id, penerima.trim(), LocalDate.now(), getCurrentPetugas());
                JOptionPane.showMessageDialog(this,
                        "Serah terima berhasil dicatat!\n" +
                                "Status barang resmi menjadi DIKEMBALIKAN dan data dikunci secara permanen dari pengajuan klaim baru.",
                        "Serah Terima Berhasil (US-021)", JOptionPane.INFORMATION_MESSAGE);
                refreshAllQueues();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal Serah Terima: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 6. Refresh Seluruh Antrean & Sinkronisasi Real-Time
    // =========================================================================
    public void refreshAllQueues() {
        modelVerifikasi.setRowCount(0);
        modelKlaim.setRowCount(0);
        modelMatching.setRowCount(0);
        modelInventaris.setRowCount(0);
        modelSerahTerima.setRowCount(0);
        if (modelAuditLog != null) modelAuditLog.setRowCount(0);

        int countVerif = 0;
        int countKlaim = 0;
        int countSerah = 0;
        int countDikembalikan = 0;
        int countDiarsipkan = 0;
        int totalItems = 0;

        for (Item item : itemService.findAll()) {
            totalItems++;
            String pelaporNama = (item.getPelapor() != null) ? item.getPelapor().getNama() : "-";
            if (item.getStatus() == StatusItem.DIKEMBALIKAN || item.getStatus() == StatusItem.DISERAHKAN) {
                countDikembalikan++;
            }
            if (item.getStatus() == StatusItem.DIARSIPKAN) {
                countDiarsipkan++;
            }

            // Tab 1: Antrean Verifikasi (DILAPORKAN)
            if (item.getStatus() == StatusItem.DILAPORKAN) {
                modelVerifikasi.addRow(new Object[]{
                        item.getId(),
                        item.getJenis().name(),
                        item.getNama(),
                        item.getKategori().getLabel(),
                        item.getLokasiKejadian(),
                        pelaporNama,
                        item.getStatus().name()
                });
                countVerif++;
            }

            // Tab 4: Inventaris & Lokasi Simpan + QR
            modelInventaris.addRow(new Object[]{
                    item.getId(),
                    item.getJenis().name(),
                    item.getNama(),
                    item.getStatus().getLabel(),
                    petugasService.getLokasiSimpan(item),
                    petugasService.getKodeQR(item)
            });

            // Tab 5: Serah Terima (DIKLAIM atau DIKEMBALIKAN)
            if (item.getStatus() == StatusItem.DALAM_PROSES_KLAIM
                    || item.getStatus() == StatusItem.DIKLAIM
                    || item.getStatus() == StatusItem.DIKEMBALIKAN
                    || item.getStatus() == StatusItem.DISERAHKAN) {
                boolean siapSerah = item.getStatus() == StatusItem.DALAM_PROSES_KLAIM || item.getStatus() == StatusItem.DIKLAIM;
                modelSerahTerima.addRow(new Object[]{
                        item.getId(),
                        item.getNama(),
                        item.getKategori().getLabel(),
                        petugasService.getLokasiSimpan(item),
                        item.getStatus().getLabel(),
                        petugasService.isTerkunci(item) ? "TERKUNCI 🔒" : "AKTIF"
                });
                if (siapSerah) countSerah++;
            }
        }

        // Tab 2: Antrean Klaim (DIAJUKAN)
        if (klaimRepository != null) {
            for (Klaim k : klaimRepository.findAll()) {
                if (k.getStatus() == StatusKlaim.DIAJUKAN || "MENUNGGU_VERIFIKASI".equals(k.getStatus().name())) {
                    String buktiInfo = k.isOtpTerverifikasi()
                            ? "✅ OTP/Akun Terverifikasi | " + k.getCiriKhusus()
                            : (k.getJenisBukti() == JenisBukti.OTP_AKUN_TERDAFTAR ? "🔐 Perlu OTP Akun | " : "📝 Ciri Fisik | ") + k.getCiriKhusus();
                    modelKlaim.addRow(new Object[]{
                            k.getId(),
                            k.getItemId(),
                            k.getPengklaim().getNama(),
                            buktiInfo,
                            k.getSkorKecocokan() + "%",
                            k.getStatus().name()
                    });
                    countKlaim++;
                }
            }
        }

        // Tab 3: Rekomendasi Cocok
        for (MatchPair m : daftarKandidatCocok) {
            modelMatching.addRow(new Object[]{
                    m.hilang.getId(),
                    m.hilang.getNama(),
                    m.temuan.getId(),
                    m.temuan.getNama(),
                    m.skor + "%"
            });
        }

        // Tab 6: Audit Log & Statistik
        if (modelAuditLog != null) {
            for (String logEntry : petugasService.getAuditLogGlobal()) {
                modelAuditLog.addRow(new Object[]{logEntry});
            }
        }
        if (lblStatistikAudit != null) {
            int returnRate = totalItems > 0 ? (countDikembalikan * 100 / totalItems) : 0;
            lblStatistikAudit.setText("Total Barang: " + totalItems + "  |  Dikembalikan: " + countDikembalikan
                    + " (" + returnRate + "%)  |  Diarsipkan (>60hr): " + countDiarsipkan);
        }

        lblCountVerifikasi.setText(String.valueOf(countVerif));
        lblCountKlaim.setText(String.valueOf(countKlaim));
        lblCountMatching.setText(String.valueOf(daftarKandidatCocok.size()));
        lblCountSerahTerima.setText(String.valueOf(countSerah));

        tabbedPane.setTitleAt(0, "1. Verifikasi (" + countVerif + ")");
        tabbedPane.setTitleAt(1, "2. Klaim & OTP (" + countKlaim + ")");
        tabbedPane.setTitleAt(2, "3. Matching (" + daftarKandidatCocok.size() + ")");
        tabbedPane.setTitleAt(3, "4. Lokasi & QR");
        tabbedPane.setTitleAt(4, "5. Serah Terima (" + countSerah + ")");
        if (tabbedPane.getTabCount() > 5) {
            tabbedPane.setTitleAt(5, "6. Audit Log (" + (modelAuditLog != null ? modelAuditLog.getRowCount() : 0) + ")");
        }

        clearDetailPanel();
    }

    private void clearDetailPanel() {
        if (txtDetailItem != null) {
            txtDetailItem.setText("Pilih salah satu baris pada tabel untuk melihat rincian objek (Polimorfisme Item.tampilkanDetail() & Syarat Bukti v2.0) dan melakukan aksi petugas.");
        }
        if (actionButtonPanel != null) {
            actionButtonPanel.removeAll();
            actionButtonPanel.revalidate();
            actionButtonPanel.repaint();
        }
    }
}
