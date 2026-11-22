package com.campusfind.ui.pages;

import com.campusfind.model.BarangElektronik;
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

import javax.swing.BorderFactory;
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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PetugasPage extends JPanel {

    private final ItemService itemService;
    private final KlaimService klaimService;
    private final KlaimRepository klaimRepository;
    private final KandidatService kandidatService;
    private final PetugasService petugasService;
    private final QRCodeService qrCodeService;
    private final AppSession appSession;

    private final List<MatchPair> daftarKandidatCocok = new ArrayList<>();

    private final JLabel lblCountVerifikasi = new JLabel("0");
    private final JLabel lblCountKlaim = new JLabel("0");
    private final JLabel lblCountMatching = new JLabel("0");
    private final JLabel lblCountSerahTerima = new JLabel("0");
    private final JLabel lblStatusWorker = new JLabel("Smart Matching: Siap");
    private final JProgressBar progressWorker = new JProgressBar();

    private JTabbedPane tabbedPane;
    private JTable tblVerifikasi, tblKlaim, tblMatching, tblInventaris, tblSerahTerima;
    private DefaultTableModel modelVerifikasi, modelKlaim, modelMatching, modelInventaris, modelSerahTerima;

    private JTextArea txtDetailItem;
    private JPanel actionButtonPanel;

    private JButton btnTerimaLaporan, btnTolakLaporan;
    private JButton btnSetujuiKlaim, btnTolakKlaim;
    private JButton btnKonfirmasiCocok;
    private JButton btnUpdateLokasi, btnLihatSimpanQR;
    private JButton btnSerahTerima;

    private static class MatchPair {
        final Item hilang;
        final Item temuan;
        final int skor;

        MatchPair(Item hilang, Item temuan, int skor) {
            this.hilang = hilang;
            this.temuan = temuan;
            this.skor = skor;
        }
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

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 24, 15, 24));
        setBackground(new Color(245, 247, 250));

        initHeaderAndKPI();
        initCenterTabs();
        initRightDetailPanel();

        refreshAllQueues();
    }

    private Petugas getCurrentPetugas() {
        if (appSession != null && appSession.isLoggedIn()) {
            return new Petugas(appSession.getNama(), appSession.getNomorIdentitas());
        }
        return new Petugas("Petugas Pos Kampus", "PTG-001");
    }

    private void initHeaderAndKPI() {
        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        JLabel title = new JLabel("Dashboard Petugas - " + getCurrentPetugas().getNama());
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel rightTopControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTopControls.setOpaque(false);

        JButton btnScanMatch = new JButton("Pindai Smart Matching");
        btnScanMatch.setFocusPainted(false);
        btnScanMatch.addActionListener(e -> jalankanSmartMatchingSemua());

        JButton btnCariQR = createStyledButton("Lacak Barang via QR Code", new Color(44, 62, 80));
        btnCariQR.addActionListener(e -> dialogLacakBarangQR());

        progressWorker.setPreferredSize(new Dimension(90, 14));
        progressWorker.setVisible(false);

        rightTopControls.add(lblStatusWorker);
        rightTopControls.add(progressWorker);
        rightTopControls.add(btnScanMatch);
        rightTopControls.add(btnCariQR);

        headerBar.add(title, BorderLayout.WEST);
        headerBar.add(rightTopControls, BorderLayout.EAST);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.add(createKpiCard("Laporan Masuk / Verifikasi", lblCountVerifikasi, new Color(52, 152, 219)));
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

        modelVerifikasi = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Lokasi", "Pelapor", "Status"});
        tblVerifikasi = new JTable(modelVerifikasi);
        setupTableSelection(tblVerifikasi, "VERIFIKASI");
        tabbedPane.addTab("1. Verifikasi Laporan", new JScrollPane(tblVerifikasi));

        modelKlaim = createReadOnlyModel(new String[]{"ID Klaim", "ID Barang", "Pengaju", "Ciri Khusus / Bukti", "Skor", "Status"});
        tblKlaim = new JTable(modelKlaim);
        setupTableSelection(tblKlaim, "KLAIM");
        tabbedPane.addTab("2. Pemeriksaan Klaim", new JScrollPane(tblKlaim));

        modelMatching = createReadOnlyModel(new String[]{"ID Hilang", "Barang Hilang", "ID Temuan", "Barang Temuan", "Skor (%)"});
        tblMatching = new JTable(modelMatching);
        setupTableSelection(tblMatching, "MATCHING");
        tabbedPane.addTab("3. Smart Matching", new JScrollPane(tblMatching));

        modelInventaris = createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Status", "Lokasi Simpan", "Kode QR"});
        tblInventaris = new JTable(modelInventaris);
        setupTableSelection(tblInventaris, "INVENTARIS");
        tabbedPane.addTab("4. Lokasi & QR Code", new JScrollPane(tblInventaris));

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
        rightPanel.setPreferredSize(new Dimension(350, 0));
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
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
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
            itemService.findById(id).ifPresent(item -> {
                txtDetailItem.setText(formatDetailItem(item));
                actionButtonPanel.add(btnTerimaLaporan);
                actionButtonPanel.add(btnTolakLaporan);
            });
        } else if ("KLAIM".equals(tipeTab)) {
            String idKlaim = (String) modelKlaim.getValueAt(row, 0);
            klaimRepository.findById(idKlaim).ifPresent(klaim -> {
                Item item = itemService.findById(klaim.getItemId()).orElse(null);
                txtDetailItem.setText(
                        "Rincian Pengajuan Klaim\n" +
                                "ID Klaim    : " + klaim.getId() + "\n" +
                                "Pengaju     : " + klaim.getPengklaim().getNama() + " (" + klaim.getPengklaim().getNomorIdentitas() + ")\n" +
                                "Ciri Klaim  : " + klaim.getCiriKhusus() + "\n" +
                                "File Bukti  : " + klaim.getBuktiPath() + "\n" +
                                "Skor Klaim  : " + klaim.getSkorKecocokan() + "%\n\n" +
                                (item != null ? formatDetailItem(item) : "")
                );
                actionButtonPanel.add(btnSetujuiKlaim);
                actionButtonPanel.add(btnTolakKlaim);
            });
        } else if ("MATCHING".equals(tipeTab)) {
            MatchPair match = daftarKandidatCocok.get(row);
            txtDetailItem.setText(
                    "Rekomendasi Smart Matching\n" +
                            "Laporan Hilang : " + match.hilang.getId() + " (" + match.hilang.getNama() + ")\n" +
                            "Barang Temuan  : " + match.temuan.getId() + " (" + match.temuan.getNama() + ")\n" +
                            "Skor Kemiripan : " + match.skor + "% (>= 70%)\n" +
                            "Lokasi Hilang  : " + match.hilang.getLokasiKejadian() + "\n" +
                            "Lokasi Temuan  : " + match.temuan.getLokasiKejadian()
            );
            actionButtonPanel.add(btnKonfirmasiCocok);
        } else if ("INVENTARIS".equals(tipeTab)) {
            String id = (String) modelInventaris.getValueAt(row, 0);
            itemService.findById(id).ifPresent(item -> {
                txtDetailItem.setText(formatDetailItem(item));
                actionButtonPanel.add(btnUpdateLokasi);
                actionButtonPanel.add(btnLihatSimpanQR);
            });
        } else if ("SERAH_TERIMA".equals(tipeTab)) {
            String id = (String) modelSerahTerima.getValueAt(row, 0);
            itemService.findById(id).ifPresent(item -> {
                txtDetailItem.setText(formatDetailItem(item));
                if (!petugasService.isTerkunci(item)) {
                    actionButtonPanel.add(btnSerahTerima);
                }
            });
        }

        actionButtonPanel.revalidate();
        actionButtonPanel.repaint();
    }

    private String formatDetailItem(Item item) {
        String infoKhusus = "";
        if (item instanceof BarangElektronik be) {
            infoKhusus = "Merk / Seri    : " + be.getMerek() + " / " + be.getNomorSeri() + "\n";
        }
        String pelaporNama = (item.getPelapor() != null) ? item.getPelapor().getNama() : "-";
        return "ID Barang      : " + item.getId() + "\n" +
                "Jenis Laporan  : " + item.getJenis().name() + "\n" +
                "Nama Barang    : " + item.getNama() + "\n" +
                "Kategori       : " + item.getKategori().name() + "\n" +
                infoKhusus +
                "Lokasi Kejadian: " + item.getLokasiKejadian() + "\n" +
                "Lokasi Simpan  : " + petugasService.getLokasiSimpan(item) + "\n" +
                "Ciri Rahasia   : " + item.getCiriRahasia() + "\n" +
                "Pelapor        : " + pelaporNama + "\n" +
                "Kode QR        : " + petugasService.getKodeQR(item) + "\n" +
                "Status Saat Ini: " + item.getStatus().getLabel() + (petugasService.isTerkunci(item) ? " [TERKUNCI]" : "") + "\n\n" +
                "Riwayat Aktivitas Petugas:\n" + petugasService.getRiwayatLog(item.getId());
    }

    private void aksiTerimaLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        try {
            Item item = itemService.findById(id).orElseThrow();
            petugasService.verifikasiLaporan(id, true, null, getCurrentPetugas());

            String msgQR = "";
            if (item.getJenis() == JenisLaporan.TEMUAN) {
                msgQR = "\nKode QR unik (" + petugasService.getKodeQR(item) + ") siap dicetak.";
            }

            JOptionPane.showMessageDialog(this,
                    "Laporan '" + item.getNama() + "' telah diverifikasi." + msgQR,
                    "Verifikasi Berhasil", JOptionPane.INFORMATION_MESSAGE);

            refreshAllQueues();
            jalankanSmartMatchingBackground(item);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Verifikasi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiTolakLaporan() {
        int row = tblVerifikasi.getSelectedRow();
        if (row == -1) return;

        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan laporan (Wajib diisi):",
                "Tolak Laporan", JOptionPane.WARNING_MESSAGE);
        if (alasan == null) return;

        String id = (String) modelVerifikasi.getValueAt(row, 0);
        try {
            petugasService.verifikasiLaporan(id, false, alasan, getCurrentPetugas());
            JOptionPane.showMessageDialog(this, "Laporan ditolak dan dihapus dari antrean.", "Laporan Ditolak", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jalankanSmartMatchingBackground(Item itemBaru) {
        SwingWorker<List<MatchPair>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MatchPair> doInBackground() {
                SwingUtilities.invokeLater(() -> {
                    lblStatusWorker.setText("Smart Matching: Memproses...");
                    progressWorker.setIndeterminate(true);
                    progressWorker.setVisible(true);
                });

                List<MatchPair> ditemukan = new ArrayList<>();
                if (kandidatService == null) return ditemukan;

                if (itemBaru.getJenis() == JenisLaporan.HILANG && itemBaru.getPelapor() != null) {
                    List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(
                            itemBaru.getId(), itemBaru.getPelapor().getNomorIdentitas());
                    for (KandidatCocokItem k : kandidat) {
                        ditemukan.add(new MatchPair(itemBaru, k.getItemTemuan(), k.getSkor()));
                    }
                } else if (itemBaru.getJenis() == JenisLaporan.TEMUAN) {
                    for (Item hilang : itemService.findByJenis(JenisLaporan.HILANG)) {
                        if (hilang.getPelapor() != null) {
                            List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(
                                    hilang.getId(), hilang.getPelapor().getNomorIdentitas());
                            for (KandidatCocokItem k : kandidat) {
                                if (k.getItemTemuan().getId().equals(itemBaru.getId())) {
                                    ditemukan.add(new MatchPair(hilang, itemBaru, k.getSkor()));
                                }
                            }
                        }
                    }
                }
                return ditemukan;
            }

            @Override
            protected void done() {
                try {
                    List<MatchPair> hasil = get();
                    for (MatchPair mp : hasil) {
                        boolean sudahAda = daftarKandidatCocok.stream()
                                .anyMatch(existing -> existing.hilang.getId().equals(mp.hilang.getId())
                                        && existing.temuan.getId().equals(mp.temuan.getId()));
                        if (!sudahAda) {
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
                    lblStatusWorker.setText("Smart Matching: Memindai...");
                    progressWorker.setIndeterminate(true);
                    progressWorker.setVisible(true);
                });

                List<MatchPair> hasil = new ArrayList<>();
                if (kandidatService == null) return hasil;

                for (Item hilang : itemService.findByJenis(JenisLaporan.HILANG)) {
                    if (hilang.getStatus() == StatusItem.DICARI && hilang.getPelapor() != null) {
                        try {
                            List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(
                                    hilang.getId(), hilang.getPelapor().getNomorIdentitas());
                            for (KandidatCocokItem k : kandidat) {
                                hasil.add(new MatchPair(hilang, k.getItemTemuan(), k.getSkor()));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                return hasil;
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
                }
            }
        };
        worker.execute();
    }

    private void aksiSetujuiKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Setujui klaim " + idKlaim + "? Klaim lain pada barang yang sama akan otomatis ditolak.",
                "Konfirmasi Persetujuan Klaim", JOptionPane.YES_NO_OPTION);
        if (konfirmasi != JOptionPane.YES_OPTION) return;

        try {
            klaimService.setujui(idKlaim, getCurrentPetugas());
            JOptionPane.showMessageDialog(this,
                    "Klaim disetujui. Notifikasi otomatis terkirim ke pengaju klaim.",
                    "Klaim Disetujui", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyetujui klaim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiTolakKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row == -1) return;

        String alasan = JOptionPane.showInputDialog(this,
                "Masukkan alasan penolakan klaim (minimal 5 karakter):",
                "Tolak Klaim", JOptionPane.WARNING_MESSAGE);
        if (alasan == null) return;

        String idKlaim = (String) modelKlaim.getValueAt(row, 0);
        try {
            klaimService.tolak(idKlaim, alasan, getCurrentPetugas());
            JOptionPane.showMessageDialog(this, "Klaim ditolak dan status barang kembali tersedia.", "Klaim Ditolak", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menolak klaim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiKonfirmasiCocok() {
        int row = tblMatching.getSelectedRow();
        if (row == -1) return;

        MatchPair match = daftarKandidatCocok.remove(row);
        try {
            petugasService.ubahStatusBarang(match.hilang.getId(), StatusItem.COCOK, getCurrentPetugas());
            petugasService.ubahStatusBarang(match.temuan.getId(), StatusItem.COCOK, getCurrentPetugas());
            JOptionPane.showMessageDialog(this,
                    "Kedua laporan (" + match.hilang.getId() + " & " + match.temuan.getId() + ") dikonfirmasi COCOK.",
                    "Konfirmasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Konfirmasi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiUpdateLokasiSimpan() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        Item item = itemService.findById(id).orElse(null);
        if (item == null) return;

        String lokasiBaru = JOptionPane.showInputDialog(this,
                "Masukkan lokasi penyimpanan fisik (misal: Loker B-2 Ruang Satpam):",
                petugasService.getLokasiSimpan(item));
        if (lokasiBaru == null) return;

        try {
            petugasService.aturLokasiPenyimpanan(id, lokasiBaru, getCurrentPetugas());
            JOptionPane.showMessageDialog(this, "Lokasi penyimpanan berhasil diperbarui.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            refreshAllQueues();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ditolak", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiLihatDanSimpanQR() {
        int row = tblInventaris.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelInventaris.getValueAt(row, 0);
        Item item = itemService.findById(id).orElse(null);
        if (item == null) return;

        if (item.getJenis() != JenisLaporan.TEMUAN) {
            JOptionPane.showMessageDialog(this,
                    "QR Code hanya tersedia untuk barang berjenis TEMUAN.",
                    "QR Tidak Tersedia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kodeQr = petugasService.getKodeQR(item);
        BufferedImage qrImage = qrCodeService.generateQRImage(kodeQr, 240, 240);

        JPanel panelQR = new JPanel(new BorderLayout(10, 10));
        panelQR.add(new JLabel("Kode Unik: " + kodeQr + " (" + item.getNama() + ")", SwingConstants.CENTER), BorderLayout.NORTH);
        panelQR.add(new JLabel(new ImageIcon(qrImage)), BorderLayout.CENTER);

        int pilihan = JOptionPane.showOptionDialog(this, panelQR,
                "QR Code Barang Temuan",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Simpan sebagai .PNG", "Tutup"}, "Simpan sebagai .PNG");

        if (pilihan == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Gambar QR Code (.PNG)");
            fileChooser.setSelectedFile(new File(kodeQr + ".png"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File saved = qrCodeService.saveQRToPng(kodeQr, fileChooser.getSelectedFile(), 300);
                    JOptionPane.showMessageDialog(this, "QR Code berhasil disimpan ke:\n" + saved.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan QR: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void dialogLacakBarangQR() {
        String[] opsi = {"Ketik Kode Unik QR", "Pilih File Gambar QR (.PNG)"};
        int mode = JOptionPane.showOptionDialog(this,
                "Pilih metode pelacakan barang via QR Code:",
                "Pelacakan Barang via QR",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opsi, opsi[0]);

        String kodeDicari = null;

        try {
            if (mode == 0) {
                kodeDicari = JOptionPane.showInputDialog(this, "Masukkan Kode QR atau ID Barang (contoh: QR-ARC-0001):");
            } else if (mode == 1) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Pilih File Gambar QR Code");
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    kodeDicari = qrCodeService.decodeQRFromFile(chooser.getSelectedFile());
                }
            }

            if (kodeDicari == null || kodeDicari.trim().isEmpty()) return;

            Item ditemukan = petugasService.cariBarangByQR(kodeDicari);
            JOptionPane.showMessageDialog(this, formatDetailItem(ditemukan),
                    "Hasil Pelacakan QR: " + kodeDicari, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Pelacakan QR", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiSerahTerimaBarang() {
        int row = tblSerahTerima.getSelectedRow();
        if (row == -1) return;

        String id = (String) modelSerahTerima.getValueAt(row, 0);
        Item item = itemService.findById(id).orElse(null);
        if (item == null) return;

        JTextField txtPenerima = new JTextField();
        JTextField txtTanggal = new JTextField(LocalDate.now().toString());
        JTextField txtPetugasSerah = new JTextField(getCurrentPetugas().getNama());
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
            try {
                petugasService.prosesSerahTerima(id, txtPenerima.getText(), LocalDate.now(), getCurrentPetugas());
                JOptionPane.showMessageDialog(this,
                        "Serah terima tercatat. Status barang menjadi DIKEMBALIKAN dan data telah dikunci.",
                        "Serah Terima Selesai", JOptionPane.INFORMATION_MESSAGE);
                refreshAllQueues();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Gagal Serah Terima", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshAllQueues() {
        modelVerifikasi.setRowCount(0);
        modelKlaim.setRowCount(0);
        modelMatching.setRowCount(0);
        modelInventaris.setRowCount(0);
        modelSerahTerima.setRowCount(0);

        int countVerif = 0, countKlaim = 0, countSerah = 0;

        for (Item item : itemService.findAll()) {
            String pelaporNama = (item.getPelapor() != null) ? item.getPelapor().getNama() : "-";
            if (item.getStatus() == StatusItem.DILAPORKAN
                    || item.getStatus() == StatusItem.DICARI
                    || item.getStatus() == StatusItem.TERSEDIA) {
                modelVerifikasi.addRow(new Object[]{
                        item.getId(),
                        item.getJenis().name(),
                        item.getNama(),
                        item.getKategori().name(),
                        item.getLokasiKejadian(),
                        pelaporNama,
                        item.getStatus().getLabel()
                });
                countVerif++;
            }

            modelInventaris.addRow(new Object[]{
                    item.getId(),
                    item.getJenis().name(),
                    item.getNama(),
                    item.getStatus().getLabel(),
                    petugasService.getLokasiSimpan(item),
                    petugasService.getKodeQR(item)
            });

            if (item.getStatus() == StatusItem.DALAM_PROSES_KLAIM
                    || item.getStatus() == StatusItem.DIKLAIM
                    || item.getStatus() == StatusItem.DIKEMBALIKAN
                    || item.getStatus() == StatusItem.DISERAHKAN) {
                boolean siapSerah = item.getStatus() == StatusItem.DALAM_PROSES_KLAIM || item.getStatus() == StatusItem.DIKLAIM;
                modelSerahTerima.addRow(new Object[]{
                        item.getId(),
                        item.getNama(),
                        item.getKategori().name(),
                        petugasService.getLokasiSimpan(item),
                        item.getStatus().getLabel(),
                        petugasService.isTerkunci(item) ? "TERKUNCI" : "AKTIF"
                });
                if (siapSerah) countSerah++;
            }
        }

        for (Klaim k : klaimRepository.findAll()) {
            if (k.getStatus() == StatusKlaim.DIAJUKAN || k.getStatus() == StatusKlaim.MENUNGGU_VERIFIKASI) {
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

        for (MatchPair m : daftarKandidatCocok) {
            modelMatching.addRow(new Object[]{
                    m.hilang.getId(),
                    m.hilang.getNama(),
                    m.temuan.getId(),
                    m.temuan.getNama(),
                    m.skor
            });
        }

        lblCountVerifikasi.setText(String.valueOf(countVerif));
        lblCountKlaim.setText(String.valueOf(countKlaim));
        lblCountMatching.setText(String.valueOf(daftarKandidatCocok.size()));
        lblCountSerahTerima.setText(String.valueOf(countSerah));

        tabbedPane.setTitleAt(0, "1. Laporan & Verifikasi (" + countVerif + ")");
        tabbedPane.setTitleAt(1, "2. Klaim (" + countKlaim + ")");
        tabbedPane.setTitleAt(2, "3. Smart Matching (" + daftarKandidatCocok.size() + ")");
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
}