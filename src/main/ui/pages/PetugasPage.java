package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KandidatCocokItem;
import com.campusfind.model.Klaim;
import com.campusfind.model.Petugas;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.PetugasService;
import com.campusfind.service.QRCodeService;
import com.campusfind.ui.components.ItemDetailDialog;
import com.campusfind.ui.components.ItemImages;
import com.campusfind.ui.theme.Fonts;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private JTable tblVerifikasi, tblKlaim, tblMatching, tblInventaris, tblSerahTerima;
    private DefaultTableModel modelVerifikasi, modelKlaim, modelMatching, modelInventaris, modelSerahTerima;

    // Right Detail Panel
    private JTextArea txtDetailItem;
    private JLabel fotoPreview;
    private Item currentDetailItem;
    private JPanel actionButtonPanel;
    private JLabel lblDetailSubtitle;

    // Action Buttons
    private JButton btnTerimaLaporan, btnTolakLaporan;
    private JButton btnSetujuiKlaim, btnTolakKlaim;
    private JButton btnKonfirmasiCocok;
    private JButton btnUpdateLokasi, btnLihatSimpanQR;
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

    private Petugas getCurrentPetugas() {
        if (appSession != null && appSession.isLoggedIn()) {
            return new Petugas(appSession.getNama(), appSession.getNomorIdentitas());
        }
        // Default role officer identitas Bintang
        return new Petugas("Bintang (Petugas)", "NIP-19950412");
    }

    // =========================================================================
    // 1. Header & KPI Cards (US-010, US-028)
    // =========================================================================
    private void initHeaderAndKPI() {
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Header Banner Atas
        JPanel headerCard = new JPanel(new BorderLayout(14, 0)) {
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
        headerCard.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Judul & Badge Petugas
        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);

        JLabel lblEyebrow = new JLabel("CAMPUSFIND • SISTEM PENGELOLAAN BARANG HILANG & TEMUAN");
        lblEyebrow.setFont(Fonts.sans(10.5f, Font.BOLD));
        lblEyebrow.setForeground(new Color(148, 163, 184)); // Slate 400

        JLabel lblTitle = new JLabel("Pusat Kendali & Antrean Kerja Petugas");
        lblTitle.setFont(Fonts.sans(18f, Font.BOLD));
        lblTitle.setForeground(Color.WHITE);

        String namaP = getCurrentPetugas().getNama();
        String nipP = getCurrentPetugas().getNomorIdentitas();
        JLabel lblPetugasInfo = new JLabel("Petugas Aktif: " + namaP + "  |  NIP: " + nipP + "  •  Status: ON-DUTY (Aktif)");
        lblPetugasInfo.setFont(Fonts.sans(12f, Font.PLAIN));
        lblPetugasInfo.setForeground(new Color(203, 213, 225)); // Slate 300

        leftHeader.add(lblEyebrow);
        leftHeader.add(Box.createVerticalStrut(2));
        leftHeader.add(lblTitle);
        leftHeader.add(Box.createVerticalStrut(4));
        leftHeader.add(lblPetugasInfo);

        // Tombol Kontrol Cepat Kanan
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightControls.setOpaque(false);

        lblStatusWorker.setFont(Fonts.sans(11.5f, Font.ITALIC));
        lblStatusWorker.setForeground(new Color(226, 232, 240));

        progressWorker.setPreferredSize(new Dimension(80, 14));
        progressWorker.setVisible(false);

        JButton btnScanMatch = createModernButton("⚡ Pindai Matching", new Color(139, 92, 246), Color.WHITE);
        btnScanMatch.setToolTipText("Jalankan algoritma pencocokan cerdas di latar belakang (SwingWorker)");
        btnScanMatch.addActionListener(e -> jalankanSmartMatchingSemua());

        JButton btnCariQR = createModernButton("🔍 Lacak via QR Code", new Color(51, 65, 85), Color.WHITE);
        btnCariQR.setToolTipText("Lacak dan verifikasi barang fisik menggunakan QR Code unik");
        btnCariQR.addActionListener(e -> dialogLacakBarangQR());

        JButton btnRefresh = createModernButton("🔄 Segarkan", new Color(71, 85, 105), Color.WHITE);
        btnRefresh.setToolTipText("Muat ulang seluruh antrean data");
        btnRefresh.addActionListener(e -> {
            refreshAllQueues();
            JOptionPane.showMessageDialog(this, "Seluruh antrean data berhasil disegarkan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        });

        rightControls.add(lblStatusWorker);
        rightControls.add(progressWorker);
        rightControls.add(btnScanMatch);
        rightControls.add(btnCariQR);
        rightControls.add(btnRefresh);

        headerCard.add(leftHeader, BorderLayout.WEST);
        headerCard.add(rightControls, BorderLayout.EAST);

        // 4 KPI Summary Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.add(createKpiCard("LAPORAN MASUK", lblCountVerifikasi, "Menunggu verifikasi petugas", COLOR_ACCENT_BLUE));
        kpiPanel.add(createKpiCard("ANTREAN KLAIM", lblCountKlaim, "Menunggu pemeriksaan bukti", COLOR_ACCENT_AMBER));
        kpiPanel.add(createKpiCard("SMART MATCHING (≥70%)", lblCountMatching, "Pasangan siap dikonfirmasi", COLOR_ACCENT_PURPLE));
        kpiPanel.add(createKpiCard("SIAP SERAH TERIMA", lblCountSerahTerima, "Klaim selesai disetujui", COLOR_ACCENT_GREEN));

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
    private void initCenterTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Fonts.sans(12.5f, Font.BOLD));
        tabbedPane.setBackground(Color.WHITE);

        // 1. Verifikasi Laporan
        modelVerifikasi = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Lokasi Kejadian", "Pelapor", "Status"});
        tblVerifikasi = createModernTable(modelVerifikasi);
        setupTableSelection(tblVerifikasi, "VERIFIKASI");
        tabbedPane.addTab("1. Verifikasi Laporan", wrapTableInScrollPane(tblVerifikasi));

        // 2. Pemeriksaan Klaim
        modelKlaim = createReadOnlyModel(new String[]{"ID Klaim", "ID Barang", "Pengaju", "Ciri Khusus / Bukti", "Skor", "Status Klaim"});
        tblKlaim = createModernTable(modelKlaim);
        setupTableSelection(tblKlaim, "KLAIM");
        tabbedPane.addTab("2. Antrean Klaim", wrapTableInScrollPane(tblKlaim));

        // 3. Smart Matching
        modelMatching = createReadOnlyModel(new String[]{"ID Hilang", "Nama Barang Hilang", "ID Temuan", "Nama Barang Temuan", "Skor Kecocokan (%)"});
        tblMatching = createModernTable(modelMatching);
        setupTableSelection(tblMatching, "MATCHING");
        tabbedPane.addTab("3. Rekomendasi Cocok", wrapTableInScrollPane(tblMatching));

        // 4. Inventaris & QR Code
        modelInventaris = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Status", "Lokasi Simpan Fisik", "Kode QR Unik"});
        tblInventaris = createModernTable(modelInventaris);
        setupTableSelection(tblInventaris, "INVENTARIS");
        tabbedPane.addTab("4. Lokasi & QR Code", wrapTableInScrollPane(tblInventaris));

        // 5. Serah Terima
        modelSerahTerima = createReadOnlyModel(new String[]{"ID Barang", "Nama Barang", "Kategori", "Lokasi Simpan", "Status", "Proteksi Data"});
        tblSerahTerima = createModernTable(modelSerahTerima);
        setupTableSelection(tblSerahTerima, "SERAH_TERIMA");
        tabbedPane.addTab("5. Serah Terima", wrapTableInScrollPane(tblSerahTerima));

        tabbedPane.addChangeListener(e -> clearDetailPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JScrollPane wrapTableInScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(COLOR_CARD_BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setOpaque(true);
        scroll.setBackground(Color.WHITE);
        return scroll;
    }

    private JTable createModernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(Fonts.sans(12f, Font.PLAIN));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(238, 242, 255)); // Indigo 50
        table.setSelectionForeground(new Color(30, 27, 75));     // Indigo 950
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setFont(Fonts.sans(12f, Font.BOLD));
        header.setBackground(new Color(241, 245, 249)); // Slate 100
        header.setForeground(new Color(51, 65, 85));     // Slate 700
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_CARD_BORDER));
        header.setReorderingAllowed(false);

        // Apply Custom Cell Renderers based on column role
        for (int i = 0; i < table.getColumnCount(); i++) {
            String colName = table.getColumnName(i).toLowerCase();
            if (colName.contains("status") || colName.contains("jenis") || colName.contains("proteksi")) {
                table.getColumnModel().getColumn(i).setCellRenderer(new BadgeCellRenderer());
            } else if (colName.contains("id") || colName.contains("skor") || colName.contains("qr")) {
                table.getColumnModel().getColumn(i).setCellRenderer(new ModernTextRenderer(SwingConstants.CENTER));
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(new ModernTextRenderer(SwingConstants.LEFT));
            }
        }

        return table;
    }

    private static class BadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String text = (value != null) ? value.toString() : "-";
            String upper = text.toUpperCase();

            Color bg = new Color(241, 245, 249);
            Color fg = new Color(71, 85, 105);

            if (upper.contains("DILAPORKAN") || upper.contains("DIAJUKAN")) {
                bg = new Color(254, 243, 199); // Amber 100
                fg = new Color(180, 83, 9);    // Amber 800
            } else if (upper.contains("DIVERIFIKASI") || upper.contains("TERSEDIA") || upper.contains("DICARI")) {
                bg = new Color(239, 246, 255); // Blue 100
                fg = new Color(29, 78, 216);   // Blue 700
            } else if (upper.contains("COCOK")) {
                bg = new Color(245, 243, 255); // Purple 100
                fg = new Color(109, 40, 217);  // Purple 700
            } else if (upper.contains("DIKLAIM") || upper.contains("PROSES") || upper.contains("DISETUJUI")) {
                bg = new Color(255, 237, 213); // Orange 100
                fg = new Color(194, 65, 12);   // Orange 800
            } else if (upper.contains("DIKEMBALIKAN") || upper.contains("DISERAHKAN") || upper.contains("TERKUNCI")) {
                bg = new Color(209, 250, 229); // Emerald 100
                fg = new Color(4, 120, 87);    // Emerald 800
            } else if (upper.contains("TOLAK") || upper.contains("DITOLAK")) {
                bg = new Color(254, 226, 226); // Red 100
                fg = new Color(185, 28, 28);   // Red 700
            } else if (upper.equals("TEMUAN")) {
                bg = new Color(209, 250, 229); // Emerald 100
                fg = new Color(4, 120, 87);
            } else if (upper.equals("HILANG")) {
                bg = new Color(255, 228, 230); // Rose 100
                fg = new Color(225, 29, 72);
            }

            final Color badgeBg = bg;
            final Color badgeFg = fg;

            JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            pillPanel.setOpaque(true);
            pillPanel.setBackground(isSelected ? new Color(238, 242, 255) : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

            JLabel badge = new JLabel(text, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(badgeBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setFont(Fonts.sans(11f, Font.BOLD));
            badge.setForeground(badgeFg);
            badge.setBorder(new EmptyBorder(2, 9, 2, 9));

            pillPanel.add(badge);
            return pillPanel;
        }
    }

    private static class ModernTextRenderer extends DefaultTableCellRenderer {
        public ModernTextRenderer(int alignment) {
            setHorizontalAlignment(alignment);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(Fonts.sans(12f, Font.PLAIN));

            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                c.setForeground(COLOR_PRIMARY);
            } else {
                c.setBackground(new Color(238, 242, 255));
                c.setForeground(new Color(30, 27, 75));
            }
            return c;
        }
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
        rightPanel.setPreferredSize(new Dimension(410, 0));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel headerPanel = new JPanel(new BorderLayout(6, 0));
        headerPanel.setOpaque(false);

        JLabel lblDetailHeader = new JLabel("Rincian & Aksi Petugas");
        lblDetailHeader.setFont(Fonts.sans(14f, Font.BOLD));
        lblDetailHeader.setForeground(COLOR_HEADER_DARK);

        JButton btnDetailLengkap = createModernButton("Detail Lengkap →", new Color(241, 245, 249), COLOR_ACCENT_BLUE);
        btnDetailLengkap.setFont(Fonts.sans(11.5f, Font.BOLD));
        btnDetailLengkap.addActionListener(e -> {
            if (currentDetailItem != null) {
                ItemDetailDialog.show(PetugasPage.this, currentDetailItem);
            }
        });

        headerPanel.add(lblDetailHeader, BorderLayout.WEST);
        headerPanel.add(btnDetailLengkap, BorderLayout.EAST);

        fotoPreview = new JLabel("📷 Pilih barang untuk melihat foto", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(COLOR_CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        fotoPreview.setFont(Fonts.sans(12f, Font.PLAIN));
        fotoPreview.setForeground(new Color(148, 163, 184));
        fotoPreview.setOpaque(false);
        fotoPreview.setPreferredSize(new Dimension(350, 175));
        fotoPreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fotoPreview.setToolTipText("Klik untuk membuka detail lengkap");
        fotoPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentDetailItem != null) {
                    ItemDetailDialog.show(PetugasPage.this, currentDetailItem);
                }
            }
        });

        txtDetailItem = new JTextArea();
        txtDetailItem.setEditable(false);
        txtDetailItem.setLineWrap(true);
        txtDetailItem.setWrapStyleWord(true);
        txtDetailItem.setFont(Fonts.sans(12f, Font.PLAIN));
        txtDetailItem.setBackground(new Color(248, 250, 252));
        txtDetailItem.setForeground(new Color(30, 41, 59));
        txtDetailItem.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(fotoPreview, BorderLayout.NORTH);
        JScrollPane scrollDetail = new JScrollPane(txtDetailItem);
        scrollDetail.setBorder(new LineBorder(COLOR_CARD_BORDER, 1));
        centerPanel.add(scrollDetail, BorderLayout.CENTER);

        actionButtonPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        actionButtonPanel.setOpaque(false);

        // Definisi Tombol Aksi Petugas
        btnTerimaLaporan = createModernButton("✔ Terima & Verifikasi Laporan", COLOR_ACCENT_GREEN, Color.WHITE);
        btnTerimaLaporan.addActionListener(e -> aksiTerimaLaporan());

        btnTolakLaporan = createModernButton("✖ Tolak Laporan", COLOR_ACCENT_RED, Color.WHITE);
        btnTolakLaporan.addActionListener(e -> aksiTolakLaporan());

        btnSetujuiKlaim = createModernButton("✔ Setujui Klaim", COLOR_ACCENT_BLUE, Color.WHITE);
        btnSetujuiKlaim.addActionListener(e -> aksiSetujuiKlaim());

        btnTolakKlaim = createModernButton("✖ Tolak Klaim", COLOR_ACCENT_RED, Color.WHITE);
        btnTolakKlaim.addActionListener(e -> aksiTolakKlaim());

        btnKonfirmasiCocok = createModernButton("★ Konfirmasi Pasangan Cocok", COLOR_ACCENT_PURPLE, Color.WHITE);
        btnKonfirmasiCocok.addActionListener(e -> aksiKonfirmasiCocok());

        btnUpdateLokasi = createModernButton("📦 Atur Lokasi Simpan Pos", COLOR_ACCENT_AMBER, Color.WHITE);
        btnUpdateLokasi.addActionListener(e -> aksiUpdateLokasiSimpan());

        btnLihatSimpanQR = createModernButton("🖨 Lihat / Cetak QR Code", new Color(14, 165, 233), Color.WHITE);
        btnLihatSimpanQR.addActionListener(e -> aksiLihatDanSimpanQR());

        btnSerahTerima = createModernButton("🤝 Selesaikan Serah Terima", COLOR_ACCENT_GREEN, Color.WHITE);
        btnSerahTerima.addActionListener(e -> aksiSerahTerimaBarang());

        rightPanel.add(headerPanel, BorderLayout.NORTH);
        rightPanel.add(centerPanel, BorderLayout.CENTER);
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
        currentDetailItem = null;
        tampilkanFoto(null);

        if ("VERIFIKASI".equals(tipeTab)) {
            String id = (String) modelVerifikasi.getValueAt(row, 0);
            itemService.findById(id).ifPresent(item -> {
                currentDetailItem = item;
                tampilkanFoto(item);
                txtDetailItem.setText(formatDetailLengkap(item));
                actionButtonPanel.setLayout(new GridLayout(2, 1, 8, 8));
                actionButtonPanel.add(btnTerimaLaporan);
                actionButtonPanel.add(btnTolakLaporan);
            });
        } else if ("KLAIM".equals(tipeTab)) {
            String idKlaim = (String) modelKlaim.getValueAt(row, 0);
            klaimRepository.findById(idKlaim).ifPresent(klaim -> {
                Item item = itemService.findById(klaim.getItemId()).orElse(null);
                currentDetailItem = item;
                tampilkanFoto(item);
                String detailKlaim = "========================================\n" +
                        "  PEMERIKSAAN KLAIM MAHASISWA (US-020)\n" +
                        "========================================\n" +
                        "ID Klaim       : " + klaim.getId() + "\n" +
                        "Status Klaim   : " + klaim.getStatus().name() + "\n" +
                        "Skor Algoritma : " + klaim.getSkorKecocokan() + "% Kecocokan\n" +
                        "Pengaju Klaim  : " + klaim.getPengklaim().getNama() + " (" + klaim.getPengklaim().getNomorIdentitas() + ")\n" +
                        "Bukti Diajukan : " + klaim.getCiriKhusus() + "\n" +
                        "Lampiran File  : " + (klaim.getBuktiPath() != null ? klaim.getBuktiPath() : "(Tidak ada lampiran)") + "\n\n" +
                        (item != null ? formatDetailLengkap(item) : "Barang tidak ditemukan.");
                txtDetailItem.setText(detailKlaim);
                actionButtonPanel.setLayout(new GridLayout(2, 1, 8, 8));
                actionButtonPanel.add(btnSetujuiKlaim);
                actionButtonPanel.add(btnTolakKlaim);
            });
        } else if ("MATCHING".equals(tipeTab)) {
            if (row < daftarKandidatCocok.size()) {
                MatchPair match = daftarKandidatCocok.get(row);
                currentDetailItem = match.temuan;
                tampilkanFoto(match.temuan);
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
                currentDetailItem = item;
                tampilkanFoto(item);
                txtDetailItem.setText(formatDetailLengkap(item));
                actionButtonPanel.setLayout(new GridLayout(2, 1, 8, 8));
                actionButtonPanel.add(btnUpdateLokasi);
                actionButtonPanel.add(btnLihatSimpanQR);
            });
        } else if ("SERAH_TERIMA".equals(tipeTab)) {
            String id = (String) modelSerahTerima.getValueAt(row, 0);
            itemService.findById(id).ifPresent(item -> {
                currentDetailItem = item;
                tampilkanFoto(item);
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

    private void tampilkanFoto(Item item) {
        BufferedImage foto = ItemImages.load(item);
        if (foto != null) {
            fotoPreview.setIcon(new ImageIcon(ItemImages.fit(foto, 350, 175)));
            fotoPreview.setText("");
        } else {
            fotoPreview.setIcon(null);
            fotoPreview.setText(item == null
                    ? "📷 Pilih barang pada tabel untuk melihat foto"
                    : "📷 Foto barang belum diunggah");
        }
    }

    private String formatDetailItem(Item item) {
        return formatDetailLengkap(item);
    }

    /**
     * Memanfaatkan method polimorfik item.tampilkanDetail() (US-008)
     * dan menggabungkannya dengan metadata petugas (Lokasi simpan fisik, QR Code, dan Audit Log US-012).
     */
    private String formatDetailLengkap(Item item) {
        // Pemanggilan Method Polimorfik Asli US-008
        String polymorphicDetails = item.tampilkanDetail();

        String qrStr = petugasService.getKodeQR(item);
        String lokasiFisik = petugasService.getLokasiSimpan(item);
        String auditLogs = petugasService.getRiwayatLog(item.getId());

        return polymorphicDetails + "\n" +
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
                            List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(
                                    hilang.getId(), hilang.getPelapor().getNomorIdentitas());
                            for (KandidatCocokItem k : kandidat) {
                                if (k.getSkor() >= 70) {
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

        int countVerif = 0;
        int countKlaim = 0;
        int countSerah = 0;

        for (Item item : itemService.findAll()) {
            String pelaporNama = (item.getPelapor() != null) ? item.getPelapor().getNama() : "-";

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
                    modelKlaim.addRow(new Object[]{
                            k.getId(),
                            k.getItemId(),
                            k.getPengklaim().getNama(),
                            k.getCiriKhusus(),
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

        lblCountVerifikasi.setText(String.valueOf(countVerif));
        lblCountKlaim.setText(String.valueOf(countKlaim));
        lblCountMatching.setText(String.valueOf(daftarKandidatCocok.size()));
        lblCountSerahTerima.setText(String.valueOf(countSerah));

        tabbedPane.setTitleAt(0, "1. Verifikasi Laporan (" + countVerif + ")");
        tabbedPane.setTitleAt(1, "2. Antrean Klaim (" + countKlaim + ")");
        tabbedPane.setTitleAt(2, "3. Rekomendasi Cocok (" + daftarKandidatCocok.size() + ")");
        tabbedPane.setTitleAt(3, "4. Lokasi & QR Code");
        tabbedPane.setTitleAt(4, "5. Serah Terima (" + countSerah + ")");

        clearDetailPanel();
    }

    private void clearDetailPanel() {
        currentDetailItem = null;
        tampilkanFoto(null);
        txtDetailItem.setText("Pilih salah satu baris pada antrean tabel di samping untuk melihat rincian barang dan melakukan aksi petugas.");
        actionButtonPanel.removeAll();
        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }
}
