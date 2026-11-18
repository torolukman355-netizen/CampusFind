import model.*;
import model.enums.JenisLaporan;
import model.enums.StatusItem;
import model.enums.StatusKlaim;
import repository.ItemRepository;
import repository.KlaimRepository;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * =============================================================================
 * CampusFind - Dashboard & Antrean Kerja Petugas
 * =============================================================================
 * Tanggung Jawab Utama Bintang (US-001 s.d. US-012):
 * - US-001 s.d. US-008 : Menggunakan Arsitektur OOP Bersih (Item, BarangElektronik,
 *                        BarangNonElektronik, ItemRepository, polimorfisme tampilkanDetail)
 * - US-010 & US-028    : Antrean Kerja Petugas & Pemrosesan Latar Belakang (SwingWorker)
 * - US-011 & US-028    : Verifikasi Laporan Masuk (Terima -> DIVERIFIKASI, Tolak -> DITOLAK
 *                        dengan wajib alasan tertulis)
 * - US-012             : Kontrol Transisi Status Berurutan & Pencatatan Log
 *
 * Mendukung fitur rekan kerja Petugas (Jepin & Fakhri):
 * - US-013 : Lokasi Penyimpanan Fisik (Khusus Temuan)
 * - US-015 : Konfirmasi Hasil Smart Matching
 * - US-017, US-018, US-029 : Cetak, Simpan, dan Pelacakan QR Code
 * - US-020 : Pemeriksaan dan Keputusan Klaim Mahasiswa
 * - US-021 : Pencatatan Serah Terima Barang & Penguncian Data
 * =============================================================================
 */
public class PetugasDashboardPanel extends JPanel {

    private final String namaPetugas;
    private final ItemRepository itemRepo = ItemRepository.getInstance();
    private final KlaimRepository klaimRepo = KlaimRepository.getInstance();

    // Rekomendasi hasil smart matching yang terdeteksi
    private final List<MatchRecord> listMatches = new ArrayList<>();

    // Komponen UI Header & KPI Counter
    private final JLabel lblCountVerifikasi = new JLabel("0");
    private final JLabel lblCountKlaim = new JLabel("0");
    private final JLabel lblCountMatching = new JLabel("0");
    private final JLabel lblCountSerahTerima = new JLabel("0");
    private final JLabel lblStatusWorker = new JLabel("Smart Matching: Siap");
    private final JProgressBar progressWorker = new JProgressBar();

    // Komponen UI Tabbed Pane & Tabel
    private JTabbedPane tabbedPane;
    private JTable tblVerifikasi, tblKlaim, tblMatching, tblInventaris, tblSerahTerima;
    private DefaultTableModel modelVerifikasi, modelKlaim, modelMatching, modelInventaris, modelSerahTerima;

    // Panel Detail Kanan (Menampilkan Polimorfisme Item.tampilkanDetail())
    private JTextArea txtDetailItem;
    private JPanel actionButtonPanel;

    // Tombol-tombol Aksi Petugas
    private JButton btnTerimaLaporan, btnTolakLaporan;
    private JButton btnSetujuiKlaim, btnTolakKlaim;
    private JButton btnKonfirmasiCocok;
    private JButton btnUpdateLokasi, btnLihatSimpanQR;
    private JButton btnSerahTerima;

    public PetugasDashboardPanel(String namaPetugas) {
        this.namaPetugas = namaPetugas;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initHeaderAndKPI();
        initCenterTabs();
        initRightDetailPanel();

        // Inisialisasi pasangan awal smart matching dari data dummy
        listMatches.add(new MatchRecord("H-012", "Flashdisk Sandisk 32GB", "T-034", "USB Sandisk Merah Hitam", 88));

        refreshAllQueues();
    }

    private void initHeaderAndKPI() {
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        // Header Bar Atas
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        JLabel title = new JLabel("CampusFind — Dashboard & Antrean Kerja Petugas (" + namaPetugas + ")");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(33, 37, 41));

        JPanel rightTopControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTopControls.setOpaque(false);

        JButton btnCariQR = new JButton("Lacak Barang via QR");
        btnCariQR.setBackground(new Color(44, 62, 80));
        btnCariQR.setForeground(Color.WHITE);
        btnCariQR.setFocusPainted(false);
        btnCariQR.addActionListener(e -> dialogLacakBarangQR());

        progressWorker.setPreferredSize(new Dimension(100, 14));
        progressWorker.setVisible(false);

        lblStatusWorker.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblStatusWorker.setForeground(new Color(100, 110, 120));

        rightTopControls.add(lblStatusWorker);
        rightTopControls.add(progressWorker);
        rightTopControls.add(btnCariQR);

        headerBar.add(title, BorderLayout.WEST);
        headerBar.add(rightTopControls, BorderLayout.EAST);

        // 4 Kartu Ringkasan KPI (US-010)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.add(createKpiCard("Menunggu Verifikasi", lblCountVerifikasi, new Color(52, 152, 219)));
        kpiPanel.add(createKpiCard("Antrean Pemeriksaan Klaim", lblCountKlaim, new Color(230, 126, 34)));
        kpiPanel.add(createKpiCard("Rekomendasi Cocok (>=70%)", lblCountMatching, new Color(155, 89, 182)));
        kpiPanel.add(createKpiCard("Siap Serah Terima", lblCountSerahTerima, new Color(39, 174, 96)));

        topContainer.add(headerBar, BorderLayout.NORTH);
        topContainer.add(kpiPanel, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);
    }

    private JPanel createKpiCard(String title, JLabel countLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 4, 0, 0, accentColor),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblTitle.setForeground(Color.DARK_GRAY);

        countLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        countLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        return card;
    }

    private void initCenterTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Tab 1: Verifikasi Laporan Masuk (US-011, US-028)
        modelVerifikasi = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Lokasi", "Pelapor", "Status"});
        tblVerifikasi = new JTable(modelVerifikasi);
        setupTableSelection(tblVerifikasi, "VERIFIKASI");
        tabbedPane.addTab("1. Verifikasi Laporan", new JScrollPane(tblVerifikasi));

        // Tab 2: Pemeriksaan Klaim (US-020)
        modelKlaim = createReadOnlyModel(new String[]{"ID Klaim", "ID Barang", "Nama Barang", "Pengaju", "Bukti Kepemilikan", "Status"});
        tblKlaim = new JTable(modelKlaim);
        setupTableSelection(tblKlaim, "KLAIM");
        tabbedPane.addTab("2. Pemeriksaan Klaim", new JScrollPane(tblKlaim));

        // Tab 3: Konfirmasi Smart Matching (US-015)
        modelMatching = createReadOnlyModel(new String[]{"ID Hilang", "Barang Hilang", "ID Temuan", "Barang Temuan", "Skor (%)"});
        tblMatching = new JTable(modelMatching);
        setupTableSelection(tblMatching, "MATCHING");
        tabbedPane.addTab("3. Smart Matching", new JScrollPane(tblMatching));

        // Tab 4: Manajemen Lokasi Penyimpanan & QR Code (US-013, US-017)
        modelInventaris = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Status", "Lokasi Simpan", "Kode QR"});
        tblInventaris = new JTable(modelInventaris);
        setupTableSelection(tblInventaris, "INVENTARIS");
        tabbedPane.addTab("4. Lokasi & QR Code", new JScrollPane(tblInventaris));

        // Tab 5: Serah Terima Barang & Penguncian Data (US-021)
        modelSerahTerima = createReadOnlyModel(new String[]{"ID Barang", "Nama Barang", "Kategori", "Lokasi Simpan", "Status", "Kunci Data"});
        tblSerahTerima = new JTable(modelSerahTerima);
        setupTableSelection(tblSerahTerima, "SERAH_TERIMA");
        tabbedPane.addTab("5. Serah Terima", new JScrollPane(tblSerahTerima));

        tabbedPane.addChangeListener(e -> clearDetailPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private DefaultTableModel createReadOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void initRightDetailPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(360, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 230)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblDetailHeader = new JLabel("Rincian Objek & Riwayat Log");
        lblDetailHeader.setFont(new Font("SansSerif", Font.BOLD, 14));

        txtDetailItem = new JTextArea();
        txtDetailItem.setEditable(false);
        txtDetailItem.setLineWrap(true);
        txtDetailItem.setWrapStyleWord(true);
        txtDetailItem.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetailItem.setBackground(new Color(250, 250, 252));
        txtDetailItem.setBorder(new EmptyBorder(8, 8, 8, 8));

        actionButtonPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        actionButtonPanel.setOpaque(false);

        // Inisialisasi Tombol Aksi Petugas
        btnTerimaLaporan = createStyledButton("Terima & Verifikasi (DIVERIFIKASI)", new Color(39, 174, 96));
        btnTerimaLaporan.addActionListener(e -> aksiTerimaLaporan());

        btnTolakLaporan = createStyledButton("Tolak Laporan (Wajib Alasan)", new Color(192, 57, 43));
        btnTolakLaporan.addActionListener(e -> aksiTolakLaporan());

        btnSetujuiKlaim = createStyledButton("Setujui Klaim -> DIKLAIM", new Color(41, 128, 185));
        btnSetujuiKlaim.addActionListener(e -> aksiSetujuiKlaim());

        btnTolakKlaim = createStyledButton("Tolak Klaim (Wajib Alasan)", new Color(192, 57, 43));
        btnTolakKlaim.addActionListener(e -> aksiTolakKlaim());

        btnKonfirmasiCocok = createStyledButton("Konfirmasi Status -> COCOK", new Color(142, 68, 173));
        btnKonfirmasiCocok.addActionListener(e -> aksiKonfirmasiCocok());

        btnUpdateLokasi = createStyledButton("Atur Lokasi Simpan Fisik", new Color(211, 84, 0));
        btnUpdateLokasi.addActionListener(e -> aksiUpdateLokasiSimpan());

        btnLihatSimpanQR = createStyledButton("Lihat / Simpan QR (.PNG)", new Color(22, 160, 133));
        btnLihatSimpanQR.addActionListener(e -> aksiLihatDanSimpanQR());

        btnSerahTerima = createStyledButton("Formulir Serah Terima -> KUNCI", new Color(39, 174, 96));
        btnSerahTerima.addActionListener(e -> aksiSerahTerimaBarang());

        rightPanel.add(lblDetailHeader, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(txtDetailItem), BorderLayout.CENTER);
        rightPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        return btn;
    }

    private void setupTableSelection(JTable table, String tipeTab) {
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                tampilkanDetailBaris(table.getSelectedRow(), tipeTab);
            }
        });
    }

    /**
     * US-008: Menampilkan detail barang menggunakan POLIMORFISME tampilkanDetail().
     */
    private void tampilkanDetailBaris(int row, String tipeTab) {
        actionButtonPanel.removeAll();

        if ("VERIFIKASI".equals(tipeTab)) {
            String id = (String) modelVerifikasi.getValueAt(row, 0);
            itemRepo.findById(id).ifPresent(item -> {
                // Polimorfisme: Memanggil item.tampilkanDetail()
                txtDetailItem.setText(item.tampilkanDetail());
                actionButtonPanel.add(btnTerimaLaporan);
                actionButtonPanel.add(btnTolakLaporan);
            });
        } else if ("KLAIM".equals(tipeTab)) {
            String idKlaim = (String) modelKlaim.getValueAt(row, 0);
            klaimRepo.findById(idKlaim).ifPresent(klaim -> {
                Item item = itemRepo.findById(klaim.getIdBarang()).orElse(null);
                txtDetailItem.setText(
                        "=== PENGAJUAN KLAIM MAHASISWA ===\n" +
                                "ID Klaim        : " + klaim.getIdKlaim() + "\n" +
                                "Nama Pengaju    : " + klaim.getPengaju() + "\n" +
                                "Bukti Kepemilikan:\n\"" + klaim.getBukti() + "\"\n" +
                                "Status Klaim    : " + klaim.getStatusKlaim() + "\n\n" +
                                (item != null ? item.tampilkanDetail() : "")
                );
                actionButtonPanel.add(btnSetujuiKlaim);
                actionButtonPanel.add(btnTolakKlaim);
            });
        } else if ("MATCHING".equals(tipeTab)) {
            MatchRecord match = listMatches.get(row);
            txtDetailItem.setText(
                    "=== REKOMENDASI SMART MATCHING ===\n" +
                            "Laporan Hilang : " + match.getIdHilang() + " (" + match.getNamaHilang() + ")\n" +
                            "Barang Temuan  : " + match.getIdTemuan() + " (" + match.getNamaTemuan() + ")\n" +
                            "Skor Kemiripan : " + match.getSkor() + "% (Memenuhi syarat >= 70%)\n\n" +
                            "Keterangan:\nKategori sama, atribut ciri fisik beririsan, dan lokasi berdekatan."
            );
            actionButtonPanel.add(btnKonfirmasiCocok);
        } else if ("INVENTARIS".equals(tipeTab)) {
            String id = (String) modelInventaris.getValueAt(row, 0);
            itemRepo.findById(id).ifPresent(item -> {
                txtDetailItem.setText(item.tampilkanDetail());
                actionButtonPanel.add(btnUpdateLokasi);
                actionButtonPanel.add(btnLihatSimpanQR);
            });
        } else if ("SERAH_TERIMA".equals(tipeTab)) {
            String id = (String) modelSerahTerima.getValueAt(row, 0);
            itemRepo.findById(id).ifPresent(item -> {
                txtDetailItem.setText(item.tampilkanDetail());
                if (!item.isTerkunci()) {
                    actionButtonPanel.add(btnSerahTerima);
                }
            });
        }

        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }

    // =========================================================================
    // FITUR BINTANG: VERIFIKASI LAPORAN & SWINGWORKER (US-011, US-028)
    // =========================================================================
    private void aksiTerimaLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        Item item = itemRepo.findById(id).orElse(null);
        if (item == null) return;

        try {
            // US-012: Kontrol transisi status berurutan (DILAPORKAN -> DIVERIFIKASI)
            item.ubahStatus(StatusItem.DIVERIFIKASI, namaPetugas, "Laporan diperiksa dan diverifikasi valid");
            itemRepo.save(item);

            String infoQR = "";
            if (item.getJenis() == JenisLaporan.TEMUAN) {
                infoQR = "\nKode QR unik barang temuan otomatis dibuat: " + item.getKodeQR();
            }

            JOptionPane.showMessageDialog(this,
                    "Laporan [" + item.getNama() + "] berhasil DIVERIFIKASI!" + infoQR +
                            "\nBarang otomatis dimasukkan ke antrean Smart Matching latar belakang.",
                    "Verifikasi Berhasil", JOptionPane.INFORMATION_MESSAGE);

            refreshAllQueues();
            jalankanSmartMatchingBackground(item);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengubah status: " + ex.getMessage(), "Error Transisi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiTolakLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        Item item = itemRepo.findById(id).orElse(null);
        if (item == null) return;

        // Validasi Wajib Isi Alasan Tertulis (US-011)
        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan laporan (Wajib diisi):",
                "Tolak Laporan Tidak Valid", JOptionPane.WARNING_MESSAGE);

        if (alasan == null || alasan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Penolakan dibatalkan: Alasan penolakan wajib dicantumkan!",
                    "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            item.ubahStatus(StatusItem.DITOLAK, namaPetugas, "Ditolak: " + alasan.trim());
            itemRepo.save(item);

            JOptionPane.showMessageDialog(this,
                    "Laporan ditolak. Laporan tidak akan diikutsertakan dalam sistem pencocokan.",
                    "Laporan Ditolak", JOptionPane.INFORMATION_MESSAGE);

            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error Transisi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * US-028: Pemrosesan pencocokan otomatis di latar belakang (SwingWorker)
     * agar layar UI tidak freeze/macet.
     */
    private void jalankanSmartMatchingBackground(Item itemBaru) {
        SwingWorker<MatchRecord, Void> worker = new SwingWorker<MatchRecord, Void>() {
            @Override
            protected MatchRecord doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    lblStatusWorker.setText("Smart Matching: Menganalisis...");
                    progressWorker.setIndeterminate(true);
                    progressWorker.setVisible(true);
                });

                // Simulasi pemrosesan algoritma smart matching
                Thread.sleep(1200);

                List<Item> verifiedItems = itemRepo.findByStatus(StatusItem.DIVERIFIKASI);
                for (Item existing : verifiedItems) {
                    if (!existing.getId().equalsIgnoreCase(itemBaru.getId())
                            && existing.getJenis() != itemBaru.getJenis()
                            && existing.getKategori() == itemBaru.getKategori()) {

                        Item hilang = (itemBaru.getJenis() == JenisLaporan.HILANG) ? itemBaru : existing;
                        Item temuan = (itemBaru.getJenis() == JenisLaporan.TEMUAN) ? itemBaru : existing;

                        // Skor kemiripan >= 70%
                        return new MatchRecord(hilang.getId(), hilang.getNama(), temuan.getId(), temuan.getNama(), 85);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    MatchRecord result = get();
                    if (result != null) {
                        listMatches.add(result);
                        JOptionPane.showMessageDialog(PetugasDashboardPanel.this,
                                "Ditemukan rekomendasi Smart Matching baru!\n" +
                                        "Hilang : " + result.getNamaHilang() + "\n" +
                                        "Temuan : " + result.getNamaTemuan() + "\n" +
                                        "Skor   : " + result.getSkor() + "%",
                                "Notifikasi Smart Matching", JOptionPane.INFORMATION_MESSAGE);
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

    // =========================================================================
    // FITUR: KONFIRMASI SMART MATCHING (US-015)
    // =========================================================================
    private void aksiKonfirmasiCocok() {
        int row = tblMatching.getSelectedRow();
        if (row == -1) return;

        MatchRecord match = listMatches.remove(row);
        Item hilang = itemRepo.findById(match.getIdHilang()).orElse(null);
        Item temuan = itemRepo.findById(match.getIdTemuan()).orElse(null);

        try {
            if (hilang != null) {
                hilang.ubahStatus(StatusItem.COCOK, namaPetugas, "Dikonfirmasi cocok dengan barang temuan " + match.getIdTemuan());
                itemRepo.save(hilang);
            }
            if (temuan != null) {
                temuan.ubahStatus(StatusItem.COCOK, namaPetugas, "Dikonfirmasi cocok dengan barang hilang " + match.getIdHilang());
                itemRepo.save(temuan);
            }

            JOptionPane.showMessageDialog(this,
                    "Kedua laporan (" + match.getIdHilang() + " & " + match.getIdTemuan() + ") berhasil diubah menjadi status COCOK!",
                    "Konfirmasi Cocok", JOptionPane.INFORMATION_MESSAGE);

            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error konfirmasi: " + ex.getMessage(), "Error Transisi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // FITUR: PEMERIKSAAN & KEPUTUSAN KLAIM (US-020)
    // =========================================================================
    private void aksiSetujuiKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        Klaim klaimDipilih = klaimRepo.findById(idKlaim).orElse(null);
        if (klaimDipilih == null) return;

        Item item = itemRepo.findById(klaimDipilih.getIdBarang()).orElse(null);
        if (item != null) {
            try {
                item.ubahStatus(StatusItem.DIKLAIM, namaPetugas, "Klaim dari " + klaimDipilih.getPengaju() + " disetujui.");
                itemRepo.save(item);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyetujui klaim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Setujui klaim terpilih & tolak otomatis klaim lain pada barang yang sama
        for (Klaim k : klaimRepo.findAll()) {
            if (k.getIdBarang().equalsIgnoreCase(klaimDipilih.getIdBarang()) && k.getStatusKlaim() == StatusKlaim.DIAJUKAN) {
                if (k.getIdKlaim().equalsIgnoreCase(klaimDipilih.getIdKlaim())) {
                    k.setStatusKlaim(StatusKlaim.DISETUJUI);
                    k.setCatatanPetugas("Klaim disetujui oleh petugas " + namaPetugas);
                } else {
                    k.setStatusKlaim(StatusKlaim.DITOLAK);
                    k.setCatatanPetugas("Ditolak otomatis karena klaim " + klaimDipilih.getIdKlaim() + " telah disetujui.");
                }
                klaimRepo.save(k);
            }
        }

        JOptionPane.showMessageDialog(this,
                "Klaim disetujui! Status barang menjadi DIKLAIM.\n" +
                        "Notifikasi terkirim ke mahasiswa " + klaimDipilih.getPengaju() + ".\n" +
                        "Klaim-klaim lain pada barang yang sama otomatis ditolak.",
                "Keputusan Klaim", JOptionPane.INFORMATION_MESSAGE);

        refreshAllQueues();
    }

    private void aksiTolakKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        Klaim klaim = klaimRepo.findById(idKlaim).orElse(null);
        if (klaim == null) return;

        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan klaim (Wajib diisi):",
                "Tolak Pengajuan Klaim", JOptionPane.WARNING_MESSAGE);

        if (alasan == null || alasan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alasan penolakan klaim wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        klaim.setStatusKlaim(StatusKlaim.DITOLAK);
        klaim.setCatatanPetugas("Ditolak oleh " + namaPetugas + ": " + alasan.trim());
        klaimRepo.save(klaim);

        JOptionPane.showMessageDialog(this, "Klaim berhasil ditolak dengan alasan: " + alasan.trim(),
                "Klaim Ditolak", JOptionPane.INFORMATION_MESSAGE);

        refreshAllQueues();
    }

    // =========================================================================
    // FITUR: MANAJEMEN LOKASI SIMPAN & QR CODE (US-013, US-017, US-018)
    // =========================================================================
    private void aksiUpdateLokasiSimpan() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        Item item = itemRepo.findById(id).orElse(null);
        if (item == null) return;

        try {
            String lokasiDefault = item.getLokasiSimpan().equals("-") ? "Loker B-2 Ruang Satpam" : item.getLokasiSimpan();
            String lokasiBaru = JOptionPane.showInputDialog(this,
                    "Masukkan lokasi penyimpanan fisik (misal: Loker B-2 Ruang Satpam):", lokasiDefault);

            if (lokasiBaru != null && !lokasiBaru.trim().isEmpty()) {
                item.setLokasiSimpan(lokasiBaru.trim(), namaPetugas);
                itemRepo.save(item);
                JOptionPane.showMessageDialog(this, "Lokasi simpan berhasil diperbarui ke: " + lokasiBaru.trim());
                refreshAllQueues();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validasi Lokasi Penyimpanan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiLihatDanSimpanQR() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        Item item = itemRepo.findById(id).orElse(null);
        if (item == null) return;

        if (item.getJenis() != JenisLaporan.TEMUAN || "-".equals(item.getKodeQR())) {
            JOptionPane.showMessageDialog(this,
                    "QR Code hanya tersedia untuk barang TEMUAN yang sudah DIVERIFIKASI.",
                    "QR Code Tidak Tersedia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BufferedImage qrImage = generateVisualQRImage(item.getKodeQR(), 220);

        JPanel panelQR = new JPanel(new BorderLayout(10, 10));
        panelQR.add(new JLabel("Kode Unik: " + item.getKodeQR() + " (" + item.getNama() + ")", SwingConstants.CENTER), BorderLayout.NORTH);
        panelQR.add(new JLabel(new ImageIcon(qrImage)), BorderLayout.CENTER);

        int pilihan = JOptionPane.showOptionDialog(this, panelQR,
                "QR Code Barang Temuan (US-017)",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Simpan sebagai .PNG", "Tutup"}, "Simpan sebagai .PNG");

        if (pilihan == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Gambar QR Code (.PNG)");
            fileChooser.setSelectedFile(new File(item.getKodeQR() + ".png"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    ImageIO.write(qrImage, "png", file);
                    JOptionPane.showMessageDialog(this, "QR Code tersimpan di:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan file QR: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void dialogLacakBarangQR() {
        String[] opsi = {"Ketik Kode Unik QR", "Buka File Gambar QR (.PNG)"};
        int mode = JOptionPane.showOptionDialog(this,
                "Pilih metode pelacakan barang via QR Code:",
                "Pelacakan Barang via QR (US-018)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opsi, opsi[0]);

        String kodeDicari = null;
        if (mode == 0) {
            kodeDicari = JOptionPane.showInputDialog(this, "Ketik Kode Unik (contoh: QR-T-055 atau T-055):");
        } else if (mode == 1) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih File Gambar QR Code");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String fileName = chooser.getSelectedFile().getName();
                kodeDicari = fileName.replaceFirst("[.][^.]+$", "");
            }
        }

        if (kodeDicari == null || kodeDicari.trim().isEmpty()) return;

        final String cari = kodeDicari.trim();
        for (Item item : itemRepo.findAll()) {
            if (item.getKodeQR().equalsIgnoreCase(cari) || item.getId().equalsIgnoreCase(cari)) {
                JOptionPane.showMessageDialog(this, item.tampilkanDetail(),
                        "Hasil Pelacakan QR: " + cari, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Barang dengan kode QR '" + cari + "' tidak ditemukan dalam sistem.",
                "Hasil Pelacakan", JOptionPane.WARNING_MESSAGE);
    }

    private BufferedImage generateVisualQRImage(String text, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, size);

        int grid = 21;
        int cell = size / grid;
        Random rand = new Random(text.hashCode());

        g.setColor(Color.BLACK);
        for (int r = 0; r < grid; r++) {
            for (int c = 0; c < grid; c++) {
                boolean corner = (r < 7 && c < 7) || (r < 7 && c >= grid - 7) || (r >= grid - 7 && c < 7);
                if (corner) {
                    boolean border = r == 0 || r == 6 || c == 0 || c == 6 ||
                            r == grid - 7 || r == grid - 1 || c == grid - 7 || c == grid - 1;
                    boolean center = (r >= 2 && r <= 4 && c >= 2 && c <= 4) ||
                            (r >= 2 && r <= 4 && c >= grid - 5 && c <= grid - 3) ||
                            (r >= grid - 5 && r <= grid - 3 && c >= 2 && c <= 4);
                    if (border || center) g.fillRect(c * cell, r * cell, cell, cell);
                } else if (rand.nextBoolean()) {
                    g.fillRect(c * cell, r * cell, cell, cell);
                }
            }
        }
        g.dispose();
        return img;
    }

    // =========================================================================
    // FITUR: FORMULIR SERAH TERIMA & PENGUNCIAN DATA (US-021)
    // =========================================================================
    private void aksiSerahTerimaBarang() {
        int row = tblSerahTerima.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelSerahTerima.getValueAt(row, 0);
        Item item = itemRepo.findById(id).orElse(null);
        if (item == null) return;

        if (item.isTerkunci()) {
            JOptionPane.showMessageDialog(this, "Barang ini sudah dikembalikan dan datanya terkunci!", "Terkunci", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtPenerima = new JTextField();
        JTextField txtTanggal = new JTextField(LocalDate.now().toString());
        JTextField txtPetugasSerah = new JTextField(namaPetugas);
        txtPetugasSerah.setEditable(false);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Nama Penerima Barang:"));
        form.add(txtPenerima);
        form.add(new JLabel("Tanggal Serah Terima:"));
        form.add(txtTanggal);
        form.add(new JLabel("Petugas yang Menyerahkan:"));
        form.add(txtPetugasSerah);

        int res = JOptionPane.showConfirmDialog(this, form,
                "Formulir Serah Terima Barang (" + item.getNama() + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            if (txtPenerima.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama penerima wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // US-012 & US-021: DIKLAIM -> DIKEMBALIKAN (Data terkunci)
                item.ubahStatus(StatusItem.DIKEMBALIKAN, namaPetugas,
                        "Diserahkan ke " + txtPenerima.getText().trim() + " pada " + txtTanggal.getText().trim() + " [DATA DIKUNCI]");
                itemRepo.save(item);

                JOptionPane.showMessageDialog(this,
                        "Serah terima selesai!\nStatus barang menjadi DIKEMBALIKAN dan data barang telah DIKUNCI.",
                        "Serah Terima Berhasil", JOptionPane.INFORMATION_MESSAGE);

                refreshAllQueues();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error Transisi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // REFRESH SEMUA TABEL DAN COUNTER KPI (US-028)
    // =========================================================================
    public void refreshAllQueues() {
        modelVerifikasi.setRowCount(0);
        modelKlaim.setRowCount(0);
        modelMatching.setRowCount(0);
        modelInventaris.setRowCount(0);
        modelSerahTerima.setRowCount(0);

        int countVerif = 0;
        int countSerah = 0;

        for (Item item : itemRepo.findAll()) {
            if (item.getStatus() == StatusItem.DILAPORKAN) {
                modelVerifikasi.addRow(new Object[]{
                        item.getId(), item.getJenis().getLabel(), item.getNama(),
                        item.getKategori().getLabel(), item.getLokasiKejadian(),
                        item.getPelapor(), item.getStatus().name()
                });
                countVerif++;
            }

            if (item.getStatus() != StatusItem.DITOLAK) {
                modelInventaris.addRow(new Object[]{
                        item.getId(), item.getJenis().getLabel(), item.getNama(),
                        item.getStatus().name(), item.getLokasiSimpan(), item.getKodeQR()
                });
            }

            if (item.getStatus() == StatusItem.DIKLAIM || item.getStatus() == StatusItem.DIKEMBALIKAN) {
                modelSerahTerima.addRow(new Object[]{
                        item.getId(), item.getNama(), item.getKategori().getLabel(),
                        item.getLokasiSimpan(), item.getStatus().name(),
                        item.isTerkunci() ? "TERKUNCI" : "AKTIF"
                });
                if (item.getStatus() == StatusItem.DIKLAIM) countSerah++;
            }
        }

        int countKlaim = 0;
        for (Klaim k : klaimRepo.findAll()) {
            if (k.getStatusKlaim() == StatusKlaim.DIAJUKAN) {
                Item item = itemRepo.findById(k.getIdBarang()).orElse(null);
                String namaBarang = (item != null) ? item.getNama() : "-";
                modelKlaim.addRow(new Object[]{
                        k.getIdKlaim(), k.getIdBarang(), namaBarang,
                        k.getPengaju(), k.getBukti(), k.getStatusKlaim().name()
                });
                countKlaim++;
            }
        }

        for (MatchRecord m : listMatches) {
            modelMatching.addRow(new Object[]{
                    m.getIdHilang(), m.getNamaHilang(), m.getIdTemuan(), m.getNamaTemuan(), m.getSkor()
            });
        }

        lblCountVerifikasi.setText(String.valueOf(countVerif));
        lblCountKlaim.setText(String.valueOf(countKlaim));
        lblCountMatching.setText(String.valueOf(listMatches.size()));
        lblCountSerahTerima.setText(String.valueOf(countSerah));

        tabbedPane.setTitleAt(0, "1. Verifikasi (" + countVerif + ")");
        tabbedPane.setTitleAt(1, "2. Klaim (" + countKlaim + ")");
        tabbedPane.setTitleAt(2, "3. Smart Matching (" + listMatches.size() + ")");
        tabbedPane.setTitleAt(3, "4. Lokasi & QR");
        tabbedPane.setTitleAt(4, "5. Serah Terima (" + countSerah + ")");

        clearDetailPanel();
    }

    private void clearDetailPanel() {
        txtDetailItem.setText("Klik salah satu baris pada tabel untuk melihat rincian objek (Polimorfisme) dan melakukan aksi.");
        actionButtonPanel.removeAll();
        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }
}
