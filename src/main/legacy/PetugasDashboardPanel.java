package com.campusfind.legacy;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PetugasDashboardPanel extends JPanel {

    private final String namaPetugas;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Struktur Data Internal Lengkap untuk Simulasi 8 Fitur Petugas
    private static class ItemRecord {
        String id;
        String jenis; // "Temuan" atau "Hilang"
        String nama;
        String kategori;
        String lokasiKejadian;
        String lokasiSimpan; // US-013 (Khusus Temuan)
        String pelapor;
        String noSeri; // Rahasia (khusus petugas)
        String status; // DILAPORKAN -> DIVERIFIKASI -> COCOK -> DIKLAIM -> DIKEMBALIKAN
        String kodeQR;
        boolean terkunci; // US-021 (Terkunci setelah DIKEMBALIKAN)
        String riwayatLog;

        ItemRecord(String id, String jenis, String nama, String kategori, String lokasiKejadian,
                   String pelapor, String noSeri, String status, String petugasAwal) {
            this.id = id;
            this.jenis = jenis;
            this.nama = nama;
            this.kategori = kategori;
            this.lokasiKejadian = lokasiKejadian;
            this.lokasiSimpan = "-";
            this.pelapor = pelapor;
            this.noSeri = noSeri;
            this.status = status;
            this.kodeQR = "Temuan".equalsIgnoreCase(jenis) && !"DILAPORKAN".equals(status) ? "QR-" + id : "-";
            this.terkunci = "DIKEMBALIKAN".equals(status);
            this.riwayatLog = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                    "] Status awal: " + status + " (oleh " + petugasAwal + ")";
        }

        void ubahStatus(String statusBaru, String pelaku) {
            this.status = statusBaru;
            this.riwayatLog += "\n[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                    "] -> " + statusBaru + " (oleh " + pelaku + ")";
        }
    }

    private static class KlaimRecord {
        String idKlaim;
        String idBarang;
        String namaBarang;
        String pengaju;
        String bukti;
        String statusKlaim; // DIAJUKAN, DISETUJUI, DITOLAK

        KlaimRecord(String idKlaim, String idBarang, String namaBarang, String pengaju, String bukti) {
            this.idKlaim = idKlaim;
            this.idBarang = idBarang;
            this.namaBarang = namaBarang;
            this.pengaju = pengaju;
            this.bukti = bukti;
            this.statusKlaim = "DIAJUKAN";
        }
    }

    private static class MatchRecord {
        String idHilang;
        String namaHilang;
        String idTemuan;
        String namaTemuan;
        int skor;

        MatchRecord(String idHilang, String namaHilang, String idTemuan, String namaTemuan, int skor) {
            this.idHilang = idHilang;
            this.namaHilang = namaHilang;
            this.idTemuan = idTemuan;
            this.namaTemuan = namaTemuan;
            this.skor = skor;
        }
    }

    // Penyimpanan Data Sementara
    private final List<ItemRecord> masterItems = new ArrayList<>();
    private final List<KlaimRecord> masterKlaims = new ArrayList<>();
    private final List<MatchRecord> masterMatches = new ArrayList<>();

    // Komponen UI Header & KPI
    private final JLabel lblCountVerifikasi = new JLabel("0");
    private final JLabel lblCountKlaim = new JLabel("0");
    private final JLabel lblCountMatching = new JLabel("0");
    private final JLabel lblCountSerahTerima = new JLabel("0");
    private final JLabel lblStatusWorker = new JLabel("Smart Matching: Siap");
    private final JProgressBar progressWorker = new JProgressBar();

    // Komponen UI Tabs & Tables
    private JTabbedPane tabbedPane;
    private JTable tblVerifikasi, tblKlaim, tblMatching, tblInventaris, tblSerahTerima;
    private DefaultTableModel modelVerifikasi, modelKlaim, modelMatching, modelInventaris, modelSerahTerima;

    // Komponen Panel Detail Kanan
    private JTextArea txtDetailItem;
    private JPanel actionButtonPanel;

    // Tombol-tombol Aksi
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

        initDummyData();
        initHeaderAndKPI();
        initCenterTabs();
        initRightDetailPanel();

        refreshAllQueues();
    }

    private void initHeaderAndKPI() {
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        // Header Bar + Tombol Pelacakan QR Cepat (US-018)
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        JLabel title = new JLabel("Dashboard & Antrean Kerja Petugas — " + namaPetugas);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel rightTopControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTopControls.setOpaque(false);

        JButton btnCariQR = new JButton("Lacak Barang via QR Code");
        btnCariQR.setBackground(new Color(44, 62, 80));
        btnCariQR.setForeground(Color.WHITE);
        btnCariQR.addActionListener(e -> dialogLacakBarangQR());

        progressWorker.setPreferredSize(new Dimension(90, 14));
        progressWorker.setVisible(false);

        rightTopControls.add(lblStatusWorker);
        rightTopControls.add(progressWorker);
        rightTopControls.add(btnCariQR);

        headerBar.add(title, BorderLayout.WEST);
        headerBar.add(rightTopControls, BorderLayout.EAST);

        // 4 Kartu KPI
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.add(createKpiCard("Menunggu Verifikasi", lblCountVerifikasi, new Color(52, 152, 219)));
        kpiPanel.add(createKpiCard("Antrean Klaim", lblCountKlaim, new Color(230, 126, 34)));
        kpiPanel.add(createKpiCard("Rekomendasi Cocok (>=70)", lblCountMatching, new Color(155, 89, 182)));
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

        // Tab 1: Verifikasi Laporan Masuk (US-011)
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

        // Tab 5: Serah Terima Barang (US-021)
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
        rightPanel.setPreferredSize(new Dimension(340, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 224, 230)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblDetailHeader = new JLabel("Rincian Barang & Log Status");
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

        // Inisialisasi Tombol Aksi
        btnTerimaLaporan = createStyledButton("Terima & Verifikasi", new Color(39, 174, 96));
        btnTerimaLaporan.addActionListener(e -> aksiTerimaLaporan());

        btnTolakLaporan = createStyledButton("Tolak Laporan (Wajib Alasan)", new Color(192, 57, 43));
        btnTolakLaporan.addActionListener(e -> aksiTolakLaporan());

        btnSetujuiKlaim = createStyledButton("Setujui Klaim -> DIKLAIM", new Color(41, 128, 185));
        btnSetujuiKlaim.addActionListener(e -> aksiSetujuiKlaim());

        btnTolakKlaim = createStyledButton("Tolak Klaim (Wajib Alasan)", new Color(192, 57, 43));
        btnTolakKlaim.addActionListener(e -> aksiTolakKlaim());

        btnKonfirmasiCocok = createStyledButton("Konfirmasi Status -> COCOK", new Color(142, 68, 173));
        btnKonfirmasiCocok.addActionListener(e -> aksiKonfirmasiCocok());

        btnUpdateLokasi = createStyledButton("Atur / Ubah Lokasi Simpan", new Color(211, 84, 0));
        btnUpdateLokasi.addActionListener(e -> aksiUpdateLokasiSimpan());

        btnLihatSimpanQR = createStyledButton("Lihat & Simpan QR (.PNG)", new Color(22, 160, 133));
        btnLihatSimpanQR.addActionListener(e -> aksiLihatDanSimpanQR());

        btnSerahTerima = createStyledButton("Proses Serah Terima -> DIKEMBALIKAN", new Color(39, 174, 96));
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

    private void tampilkanDetailBaris(int row, String tipeTab) {
        actionButtonPanel.removeAll();

        if ("VERIFIKASI".equals(tipeTab)) {
            String id = (String) modelVerifikasi.getValueAt(row, 0);
            ItemRecord item = findItemById(id);
            if (item != null) {
                txtDetailItem.setText(formatDetailItem(item));
                actionButtonPanel.add(btnTerimaLaporan);
                actionButtonPanel.add(btnTolakLaporan);
            }
        } else if ("KLAIM".equals(tipeTab)) {
            String idKlaim = (String) modelKlaim.getValueAt(row, 0);
            KlaimRecord klaim = findKlaimById(idKlaim);
            if (klaim != null) {
                ItemRecord item = findItemById(klaim.idBarang);
                txtDetailItem.setText(
                        "=== RINCIAN PENGAJUAN KLAIM ===\n" +
                                "ID Klaim   : " + klaim.idKlaim + "\n" +
                                "Pengaju    : " + klaim.pengaju + "\n" +
                                "Bukti Klaim: " + klaim.bukti + "\n\n" +
                                (item != null ? formatDetailItem(item) : "")
                );
                actionButtonPanel.add(btnSetujuiKlaim);
                actionButtonPanel.add(btnTolakKlaim);
            }
        } else if ("MATCHING".equals(tipeTab)) {
            MatchRecord match = masterMatches.get(row);
            txtDetailItem.setText(
                    "=== REKOMENDASI SMART MATCHING ===\n" +
                            "Laporan Hilang : " + match.idHilang + " (" + match.namaHilang + ")\n" +
                            "Barang Temuan  : " + match.idTemuan + " (" + match.namaTemuan + ")\n" +
                            "Skor Kemiripan : " + match.skor + "% (>= 70%)\n" +
                            "Alasan         : Kategori, ciri fisik, & lokasi berdekatan."
            );
            actionButtonPanel.add(btnKonfirmasiCocok);
        } else if ("INVENTARIS".equals(tipeTab)) {
            String id = (String) modelInventaris.getValueAt(row, 0);
            ItemRecord item = findItemById(id);
            if (item != null) {
                txtDetailItem.setText(formatDetailItem(item));
                actionButtonPanel.add(btnUpdateLokasi);
                actionButtonPanel.add(btnLihatSimpanQR);
            }
        } else if ("SERAH_TERIMA".equals(tipeTab)) {
            String id = (String) modelSerahTerima.getValueAt(row, 0);
            ItemRecord item = findItemById(id);
            if (item != null) {
                txtDetailItem.setText(formatDetailItem(item));
                if (!item.terkunci) {
                    actionButtonPanel.add(btnSerahTerima);
                }
            }
        }

        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }

    private String formatDetailItem(ItemRecord item) {
        return "ID Barang      : " + item.id + "\n" +
                "Jenis Laporan  : " + item.jenis + "\n" +
                "Nama Barang    : " + item.nama + "\n" +
                "Kategori       : " + item.kategori + "\n" +
                "Lokasi Kejadian: " + item.lokasiKejadian + "\n" +
                "Lokasi Simpan  : " + item.lokasiSimpan + "\n" +
                "No. Seri/Rahasia: " + item.noSeri + "\n" +
                "Kode QR        : " + item.kodeQR + "\n" +
                "Status Saat Ini: " + item.status + (item.terkunci ? " [TERKUNCI]" : "") + "\n\n" +
                "--- Riwayat Transisi Status ---\n" + item.riwayatLog;
    }

    // =========================================================================
    // 1. VERIFIKASI LAPORAN & SWINGWORKER (US-011, US-028)
    // =========================================================================
    private void aksiTerimaLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        ItemRecord item = findItemById(id);
        if (item == null) return;

        item.ubahStatus("DIVERIFIKASI", namaPetugas);
        String msgQR = "";
        if ("Temuan".equalsIgnoreCase(item.jenis)) {
            item.kodeQR = "QR-" + item.id;
            msgQR = "\nKode QR unik (" + item.kodeQR + ") otomatis dibuat!";
        }

        JOptionPane.showMessageDialog(this,
                "Laporan " + item.nama + " diverifikasi (DIVERIFIKASI)." + msgQR,
                "Verifikasi Berhasil", JOptionPane.INFORMATION_MESSAGE);

        refreshAllQueues();
        jalankanSmartMatchingBackground(item);
    }

    private void aksiTolakLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan laporan (Wajib diisi):",
                "Tolak Laporan", JOptionPane.WARNING_MESSAGE);
        if (alasan == null || alasan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Penolakan dibatalkan: Alasan wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        ItemRecord item = findItemById(id);
        if (item != null) {
            item.ubahStatus("DITOLAK (" + alasan + ")", namaPetugas);
        }
        refreshAllQueues();
    }

    private void jalankanSmartMatchingBackground(ItemRecord itemBaru) {
        SwingWorker<MatchRecord, Void> worker = new SwingWorker<MatchRecord, Void>() {
            @Override
            protected MatchRecord doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    lblStatusWorker.setText("Smart Matching: Memproses...");
                    progressWorker.setIndeterminate(true);
                    progressWorker.setVisible(true);
                });
                Thread.sleep(1200); // Simulasi pencocokan skor >= 70

                for (ItemRecord existing : masterItems) {
                    if (!existing.id.equals(itemBaru.id)
                            && "DIVERIFIKASI".equals(existing.status)
                            && !existing.jenis.equalsIgnoreCase(itemBaru.jenis)
                            && existing.kategori.equalsIgnoreCase(itemBaru.kategori)) {
                        ItemRecord hilang = "Hilang".equalsIgnoreCase(itemBaru.jenis) ? itemBaru : existing;
                        ItemRecord temuan = "Temuan".equalsIgnoreCase(itemBaru.jenis) ? itemBaru : existing;
                        return new MatchRecord(hilang.id, hilang.nama, temuan.id, temuan.nama, 85);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    MatchRecord match = get();
                    if (match != null) {
                        masterMatches.add(match);
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
    // 2. PEMERIKSAAN KLAIM (US-020)
    // =========================================================================
    private void aksiSetujuiKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        KlaimRecord klaimDipilih = findKlaimById(idKlaim);
        if (klaimDipilih == null) return;

        ItemRecord item = findItemById(klaimDipilih.idBarang);
        if (item != null) {
            if (item.terkunci) {
                JOptionPane.showMessageDialog(this, "Barang ini sudah dikembalikan dan terkunci!", "Ditolak", JOptionPane.ERROR_MESSAGE);
                return;
            }
            item.ubahStatus("DIKLAIM", namaPetugas);
        }

        // Setujui klaim terpilih & otomatis tolak klaim lain pada barang yang sama
        for (KlaimRecord k : masterKlaims) {
            if (k.idBarang.equals(klaimDipilih.idBarang) && "DIAJUKAN".equals(k.statusKlaim)) {
                if (k.idKlaim.equals(klaimDipilih.idKlaim)) {
                    k.statusKlaim = "DISETUJUI";
                } else {
                    k.statusKlaim = "DITOLAK (Otomatis: Klaim lain disetujui)";
                }
            }
        }

        JOptionPane.showMessageDialog(this,
                "Klaim disetujui! Status barang menjadi DIKLAIM.\nNotifikasi terkirim ke " + klaimDipilih.pengaju + ".",
                "Klaim Disetujui", JOptionPane.INFORMATION_MESSAGE);
        refreshAllQueues();
    }

    private void aksiTolakKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan klaim (Wajib diisi):",
                "Tolak Klaim", JOptionPane.WARNING_MESSAGE);
        if (alasan == null || alasan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alasan penolakan wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        KlaimRecord klaim = findKlaimById(idKlaim);
        if (klaim != null) {
            klaim.statusKlaim = "DITOLAK (" + alasan + ")";
        }
        refreshAllQueues();
    }

    // =========================================================================
    // 3. KONFIRMASI SMART MATCHING (US-015)
    // =========================================================================
    private void aksiKonfirmasiCocok() {
        int row = tblMatching.getSelectedRow();
        if (row == -1) return;

        MatchRecord match = masterMatches.remove(row);
        ItemRecord hilang = findItemById(match.idHilang);
        ItemRecord temuan = findItemById(match.idTemuan);

        if (hilang != null) hilang.ubahStatus("COCOK", namaPetugas);
        if (temuan != null) temuan.ubahStatus("COCOK", namaPetugas);

        JOptionPane.showMessageDialog(this,
                "Kedua laporan (" + match.idHilang + " & " + match.idTemuan + ") dikonfirmasi menjadi COCOK!",
                "Konfirmasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
        refreshAllQueues();
    }

    // =========================================================================
    // 4. MANAJEMEN LOKASI PENYIMPANAN (US-013) & QR CODE (US-017, US-018, US-029)
    // =========================================================================
    private void aksiUpdateLokasiSimpan() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        ItemRecord item = findItemById(id);
        if (item == null) return;

        if (item.terkunci) {
            JOptionPane.showMessageDialog(this, "Data barang sudah terkunci (DIKEMBALIKAN) dan tidak dapat diubah!", "Terkunci", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi US-013: Sistem menolak jika barang berjenis Hilang
        if (!"Temuan".equalsIgnoreCase(item.jenis)) {
            JOptionPane.showMessageDialog(this,
                    "DITOLAK: Lokasi penyimpanan fisik hanya berlaku untuk barang berjenis TEMUAN!",
                    "Validasi Lokasi Penyimpanan", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String lokasiBaru = JOptionPane.showInputDialog(this,
                "Masukkan lokasi penyimpanan fisik (misal: Ruang C - 101):",
                item.lokasiSimpan.equals("-") ? "Ruang C - 106" : item.lokasiSimpan);

        if (lokasiBaru != null && !lokasiBaru.trim().isEmpty()) {
            item.lokasiSimpan = lokasiBaru.trim();
            item.riwayatLog += "\n[" + LocalDateTime.now().format(dtf) + "] Lokasi simpan diperbarui: " + item.lokasiSimpan + " (oleh " + namaPetugas + ")";
            refreshAllQueues();
        }
    }

    private void aksiLihatDanSimpanQR() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        ItemRecord item = findItemById(id);
        if (item == null) return;

        if (!"Temuan".equalsIgnoreCase(item.jenis) || "-".equals(item.kodeQR)) {
            JOptionPane.showMessageDialog(this,
                    "QR Code hanya tersedia untuk barang TEMUAN yang sudah DIVERIFIKASI.",
                    "QR Tidak Tersedia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BufferedImage qrImage = generateVisualQRImage(item.kodeQR, 220);

        JPanel panelQR = new JPanel(new BorderLayout(10, 10));
        panelQR.add(new JLabel("Kode Unik: " + item.kodeQR + " (" + item.nama + ")", SwingConstants.CENTER), BorderLayout.NORTH);
        panelQR.add(new JLabel(new ImageIcon(qrImage)), BorderLayout.CENTER);

        int pilihan = JOptionPane.showOptionDialog(this, panelQR,
                "QR Code Barang Temuan (US-017)",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Simpan sebagai .PNG", "Tutup"}, "Simpan sebagai .PNG");

        if (pilihan == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Gambar QR Code (.PNG)");
            fileChooser.setSelectedFile(new File(item.kodeQR + ".png"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    ImageIO.write(qrImage, "png", file);
                    JOptionPane.showMessageDialog(this, "QR Code berhasil disimpan ke:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan QR: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Pelacakan Barang via Kode Unik atau Unggah File QR Code (US-018)
    private void dialogLacakBarangQR() {
        String[] opsi = {"Ketik Kode Unik QR", "Pilih File Gambar QR (.PNG)"};
        int mode = JOptionPane.showOptionDialog(this,
                "Pilih metode pelacakan barang via QR Code:",
                "Pelacakan Barang via QR (US-018)",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opsi, opsi[0]);

        String kodeDicari = null;

        if (mode == 0) {
            kodeDicari = JOptionPane.showInputDialog(this, "Masukkan Kode QR (contoh: QR-T-055):");
        } else if (mode == 1) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih File Gambar QR Code");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String fileName = chooser.getSelectedFile().getName();
                kodeDicari = fileName.replaceFirst("[.][^.]+$", ""); // Ambil nama file tanpa ekstensi .png
            }
        }

        if (kodeDicari == null || kodeDicari.trim().isEmpty()) return;

        for (ItemRecord item : masterItems) {
            if (item.kodeQR.equalsIgnoreCase(kodeDicari.trim()) || item.id.equalsIgnoreCase(kodeDicari.trim())) {
                JOptionPane.showMessageDialog(this, formatDetailItem(item),
                        "Hasil Pelacakan QR: " + kodeDicari, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Barang dengan kode QR '" + kodeDicari + "' tidak ditemukan dalam sistem.",
                "Tidak Ditemukan", JOptionPane.WARNING_MESSAGE);
    }

    // Generator pola matriks visual QR deterministik berbasis kode unik barang
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
                // Finder pattern di 3 sudut khas QR Code
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
    // 5. PENCATATAN SERAH TERIMA & PENGUNCIAN DATA (US-021, US-029)
    // =========================================================================
    private void aksiSerahTerimaBarang() {
        int row = tblSerahTerima.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelSerahTerima.getValueAt(row, 0);
        ItemRecord item = findItemById(id);
        if (item == null) return;

        if (item.terkunci) {
            JOptionPane.showMessageDialog(this, "Barang ini sudah diserahterimakan dan terkunci!", "Terkunci", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Formulir Serah Terima
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
                "Formulir Serah Terima Barang (" + item.nama + ")",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            if (txtPenerima.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama penerima wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            item.ubahStatus("DIKEMBALIKAN", namaPetugas);
            item.terkunci = true; // Mengunci data barang agar tidak dapat diklaim/diubah lagi
            item.riwayatLog += "\n   -> Diterima oleh: " + txtPenerima.getText().trim() + " pada " + txtTanggal.getText().trim() + " [DATA DIKUNCI]";

            JOptionPane.showMessageDialog(this,
                    "Serah terima tercatat! Status barang menjadi DIKEMBALIKAN dan data telah dikunci.",
                    "Serah Terima Selesai", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        }
    }

    // =========================================================================
    // REFRESH SEMUA TABEL & KPI (US-028)
    // =========================================================================
    public void refreshAllQueues() {
        modelVerifikasi.setRowCount(0);
        modelKlaim.setRowCount(0);
        modelMatching.setRowCount(0);
        modelInventaris.setRowCount(0);
        modelSerahTerima.setRowCount(0);

        int countVerif = 0, countKlaim = 0, countSerah = 0;

        for (ItemRecord item : masterItems) {
            if ("DILAPORKAN".equals(item.status)) {
                modelVerifikasi.addRow(new Object[]{item.id, item.jenis, item.nama, item.kategori, item.lokasiKejadian, item.pelapor, item.status});
                countVerif++;
            }
            if (!item.status.startsWith("DITOLAK")) {
                modelInventaris.addRow(new Object[]{item.id, item.jenis, item.nama, item.status, item.lokasiSimpan, item.kodeQR});
            }
            if ("DIKLAIM".equals(item.status) || "DIKEMBALIKAN".equals(item.status)) {
                modelSerahTerima.addRow(new Object[]{item.id, item.nama, item.kategori, item.lokasiSimpan, item.status, item.terkunci ? "TERKUNCI" : "AKTIF"});
                if ("DIKLAIM".equals(item.status)) countSerah++;
            }
        }

        for (KlaimRecord k : masterKlaims) {
            if ("DIAJUKAN".equals(k.statusKlaim)) {
                modelKlaim.addRow(new Object[]{k.idKlaim, k.idBarang, k.namaBarang, k.pengaju, k.bukti, k.statusKlaim});
                countKlaim++;
            }
        }

        for (MatchRecord m : masterMatches) {
            modelMatching.addRow(new Object[]{m.idHilang, m.namaHilang, m.idTemuan, m.namaTemuan, m.skor});
        }

        lblCountVerifikasi.setText(String.valueOf(countVerif));
        lblCountKlaim.setText(String.valueOf(countKlaim));
        lblCountMatching.setText(String.valueOf(masterMatches.size()));
        lblCountSerahTerima.setText(String.valueOf(countSerah));

        tabbedPane.setTitleAt(0, "1. Verifikasi (" + countVerif + ")");
        tabbedPane.setTitleAt(1, "2. Klaim (" + countKlaim + ")");
        tabbedPane.setTitleAt(2, "3. Smart Matching (" + masterMatches.size() + ")");
        tabbedPane.setTitleAt(3, "4. Lokasi & QR");
        tabbedPane.setTitleAt(4, "5. Serah Terima (" + countSerah + ")");

        clearDetailPanel();
    }

    private void clearDetailPanel() {
        txtDetailItem.setText("Pilih salah satu baris pada tabel untuk melihat rincian lengkap dan aksi petugas.");
        actionButtonPanel.removeAll();
        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }

    private ItemRecord findItemById(String id) {
        for (ItemRecord i : masterItems) if (i.id.equals(id)) return i;
        return null;
    }

    private KlaimRecord findKlaimById(String idKlaim) {
        for (KlaimRecord k : masterKlaims) if (k.idKlaim.equals(idKlaim)) return k;
        return null;
    }

    private void initDummyData() {
        masterItems.add(new ItemRecord("T-101", "Temuan", "iPhone 13 Hitam", "Elektronik", "Gedung A Lt. 2", "Budi (Mhs)", "IMEI-8839201", "DILAPORKAN", "Sistem"));
        masterItems.add(new ItemRecord("H-102", "Hilang", "Dompet Kulit Coklat", "Non-Elektronik", "Kantin Pusat", "Siti (Mhs)", "KTP a.n Siti", "DILAPORKAN", "Sistem"));

        ItemRecord t055 = new ItemRecord("T-055", "Temuan", "Tumbler Corkcicle Biru", "Non-Elektronik", "Perpustakaan", "Jepin (Petugas)", "Stiker nama di bawah", "DIVERIFIKASI", "Jepin");
        t055.lokasiSimpan = "Ruang C - 101";
        masterItems.add(t055);

        ItemRecord h012 = new ItemRecord("H-012", "Hilang", "Flashdisk Sandisk 32GB", "Elektronik", "Lab Komputer 1", "Rama (Mhs)", "SN-FD9921", "DIVERIFIKASI", "Bintang");
        ItemRecord t034 = new ItemRecord("T-034", "Temuan", "USB Sandisk Merah Hitam", "Elektronik", "Lab Komputer 1", "Agnes (Mhs)", "SN-FD9921", "DIVERIFIKASI", "Bintang");
        t034.lokasiSimpan = "Ruang C - 106";
        masterItems.add(h012);
        masterItems.add(t034);

        masterKlaims.add(new KlaimRecord("K-201", "T-055", "Tumbler Corkcicle Biru", "Andi (Mhs)", "Ada stiker nama Andi di bagian bawah botol"));
        masterKlaims.add(new KlaimRecord("K-202", "T-055", "Tumbler Corkcicle Biru", "Rina (Mhs)", "Warna biru tua"));

        masterMatches.add(new MatchRecord("H-012", "Flashdisk Sandisk 32GB", "T-034", "USB Sandisk Merah Hitam", 88));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CampusFind — Modul Role Petugas (Lengkap)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 650);
            frame.setLocationRelativeTo(null);
            frame.add(new PetugasDashboardPanel("(Nama Petugas)"));
            frame.setVisible(true);
        });
    }
}