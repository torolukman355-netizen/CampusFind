package com.campusfind.ui.pages;

import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.JenisBukti;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Laporan;
import com.campusfind.model.MatchResult;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.User;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingEngine;
import com.campusfind.service.PetugasService;
import com.campusfind.ui.components.ModernSwingKit;
import com.campusfind.ui.theme.Fonts;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Antarmuka lengkap Role Mahasiswa (Pitya & Nadia):
 * - Pelaporan Barang Hilang & Temuan dengan input dinamis Elektronik/Non-Elektronik (US-009, US-025)
 * - Dashboard Laporan Saya & 5-Step Status Tracker dengan privasi (US-010, US-026)
 * - Kandidat Barang Cocok >= 70% via MatchingEngine (US-015)
 * - Pengajuan & Pemantauan Klaim + Tantangan OTP v2.0 (US-019, US-027)
 */
public class MahasiswaDesktopPage extends JPanel {

    private final ItemRepository itemRepository;
    private final KlaimRepository klaimRepository;
    private final KlaimService klaimService;
    private final PetugasService petugasService;
    private final MatchingEngine matchingEngine;
    private User activeUser;
    private Runnable onDataChanged;

    private final JLabel lblKpiLaporan = new JLabel("0");
    private final JLabel lblKpiKlaim = new JLabel("0");
    private final JLabel lblKpiTemuan = new JLabel("0");

    private JTabbedPane tabs;
    private DefaultTableModel modelMine, modelPublic, modelKlaim;
    private JTable tblMine, tblPublic, tblKlaim;

    private JPanel stepperContainer;
    private JTextArea txtDetail;
    private JButton btnCekKandidat;

    // Form Lapor fields
    private JComboBox<JenisLaporan> cmbJenis;
    private JComboBox<KategoriBarang> cmbKategori;
    private JTextField txtNama, txtLokasi, txtTanggal, txtCiri;
    private JTextArea txtDeskripsi;
    private JTextField txtMerek, txtTipe, txtAkunTerdaftar, txtNomorSeri;
    private JTextField txtWarna, txtBahan;
    private JPanel dynamicAttrCard;
    private CardLayout dynamicAttrLayout;

    public MahasiswaDesktopPage(ItemRepository itemRepository,
                                KlaimRepository klaimRepository,
                                KlaimService klaimService,
                                PetugasService petugasService,
                                MatchingEngine matchingEngine,
                                User activeUser,
                                Runnable onDataChanged) {
        this.itemRepository = itemRepository;
        this.klaimRepository = klaimRepository;
        this.klaimService = klaimService;
        this.petugasService = petugasService;
        this.matchingEngine = matchingEngine;
        this.activeUser = activeUser;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(14, 14));
        setBackground(ModernSwingKit.PAGE_BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initKpiRow();
        initCenterTabs();
        refreshData();
    }

    public void setActiveUser(User user) {
        this.activeUser = user;
        refreshData();
    }

    private void initKpiRow() {
        JPanel kpi = new JPanel(new GridLayout(1, 3, 14, 0));
        kpi.setOpaque(false);
        kpi.add(ModernSwingKit.createKpiCard("LAPORAN SAYA (PRIVAT)", lblKpiLaporan, "Hanya dapat dilihat oleh Anda & Petugas", ModernSwingKit.BLUE_600));
        kpi.add(ModernSwingKit.createKpiCard("PENGAJUAN KLAIM SAYA", lblKpiKlaim, "Pantau status & verifikasi OTP", ModernSwingKit.AMBER_600));
        kpi.add(ModernSwingKit.createKpiCard("BARANG TEMUAN TERSEDIA", lblKpiTemuan, "Tampilan publik (privasi pelapor aman)", ModernSwingKit.EMERALD_600));
        add(kpi, BorderLayout.NORTH);
    }

    private void initCenterTabs() {
        tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(Color.WHITE);
        tabs.setFont(Fonts.sans(12.5f, Font.BOLD));

        // Tab 1: Laporan Saya + Detail & Stepper
        JPanel tab1 = new JPanel(new BorderLayout(12, 12));
        tab1.setOpaque(false);

        modelMine = ModernSwingKit.createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Lokasi", "Tanggal", "Status"});
        tblMine = ModernSwingKit.createModernTable(modelMine);
        tblMine.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblMine.getSelectedRow() != -1) {
                tampilkanDetailMine((String) modelMine.getValueAt(tblMine.getSelectedRow(), 0));
            }
        });

        JPanel mineTableCard = ModernSwingKit.createRoundedCard(12);
        mineTableCard.add(ModernSwingKit.wrapScroll(tblMine), BorderLayout.CENTER);

        JPanel detailCard = ModernSwingKit.createRoundedCard(14);
        detailCard.setPreferredSize(new java.awt.Dimension(420, 0));

        JPanel detailTop = new JPanel();
        detailTop.setLayout(new BoxLayout(detailTop, BoxLayout.Y_AXIS));
        detailTop.setOpaque(false);
        JLabel lblDetTitle = new JLabel("Pelacakan Status & Detail Laporan");
        lblDetTitle.setFont(Fonts.sans(14f, Font.BOLD));
        lblDetTitle.setForeground(ModernSwingKit.SLATE_900);
        stepperContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        stepperContainer.setOpaque(false);
        detailTop.add(lblDetTitle);
        detailTop.add(Box.createVerticalStrut(4));
        detailTop.add(stepperContainer);

        txtDetail = new JTextArea("Klik baris laporan Anda untuk melihat progres 5 tahap & kandidat cocok.");
        txtDetail.setEditable(false);
        txtDetail.setLineWrap(true);
        txtDetail.setWrapStyleWord(true);
        txtDetail.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetail.setBackground(ModernSwingKit.SLATE_50);
        txtDetail.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnCekKandidat = ModernSwingKit.createButton("★ Cek Kandidat Cocok (>= 70%)", ModernSwingKit.PURPLE_600, Color.WHITE);
        btnCekKandidat.setEnabled(false);
        btnCekKandidat.addActionListener(e -> aksiCekKandidatTerpilih());

        detailCard.add(detailTop, BorderLayout.NORTH);
        detailCard.add(ModernSwingKit.wrapScroll(txtDetail), BorderLayout.CENTER);
        detailCard.add(btnCekKandidat, BorderLayout.SOUTH);

        tab1.add(mineTableCard, BorderLayout.CENTER);
        tab1.add(detailCard, BorderLayout.EAST);
        tabs.addTab("1. Laporan Saya", tab1);

        // Tab 2: Buat Laporan Baru (Input Dinamis)
        tabs.addTab("2. Buat Laporan Baru", createFormLaporPanel());

        // Tab 3: Jelajah Barang Temuan (Privasi Terlindungi)
        JPanel tab3 = ModernSwingKit.createRoundedCard(12);
        modelPublic = ModernSwingKit.createReadOnlyModel(new String[]{"ID", "Nama Barang", "Ringkasan Publik (Privasi Pelapor Disembunyikan)", "Pos Simpan", "Status"});
        tblPublic = ModernSwingKit.createModernTable(modelPublic);
        JButton btnAjukanKlaim = ModernSwingKit.createButton("Ajukan Klaim Barang Terpilih", ModernSwingKit.BLUE_600, Color.WHITE);
        btnAjukanKlaim.addActionListener(e -> aksiAjukanKlaimDariTabel());
        JPanel tab3Bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tab3Bot.setOpaque(false);
        tab3Bot.add(btnAjukanKlaim);
        tab3.add(ModernSwingKit.wrapScroll(tblPublic), BorderLayout.CENTER);
        tab3.add(tab3Bot, BorderLayout.SOUTH);
        tabs.addTab("3. Jelajah Barang Temuan", tab3);

        // Tab 4: Pengajuan Klaim Saya & OTP
        JPanel tab4 = ModernSwingKit.createRoundedCard(12);
        modelKlaim = ModernSwingKit.createReadOnlyModel(new String[]{"ID Klaim", "ID Barang", "Nama Barang", "Bukti Diajukan", "Status", "Catatan / Info OTP"});
        tblKlaim = ModernSwingKit.createModernTable(modelKlaim);

        JButton btnInputOtp = ModernSwingKit.createButton("Masukkan Kode OTP", ModernSwingKit.PURPLE_600, Color.WHITE);
        btnInputOtp.addActionListener(e -> aksiMahasiswaInputOtp());
        JButton btnBatalkanKlaim = ModernSwingKit.createButton("✕ Batalkan Klaim", ModernSwingKit.ROSE_600, Color.WHITE);
        btnBatalkanKlaim.addActionListener(e -> aksiMahasiswaBatalkanKlaim());

        JPanel tab4Bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        tab4Bot.setOpaque(false);
        tab4Bot.add(btnInputOtp);
        tab4Bot.add(btnBatalkanKlaim);

        tab4.add(ModernSwingKit.wrapScroll(tblKlaim), BorderLayout.CENTER);
        tab4.add(tab4Bot, BorderLayout.SOUTH);
        tabs.addTab("4. Klaim Saya & OTP", tab4);

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createFormLaporPanel() {
        JPanel wrapper = ModernSwingKit.createRoundedCard(20);
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        cmbJenis = new JComboBox<>(JenisLaporan.values());
        cmbKategori = new JComboBox<>(KategoriBarang.values());
        txtNama = new JTextField();
        txtLokasi = new JTextField();
        txtTanggal = new JTextField(LocalDate.now().toString());
        txtCiri = new JTextField();
        txtDeskripsi = new JTextArea(3, 20);
        txtDeskripsi.setBorder(new LineBorder(ModernSwingKit.SLATE_200));

        form.add(labelField("Jenis Laporan:", cmbJenis));
        form.add(labelField("Kategori Barang:", cmbKategori));
        form.add(labelField("Nama Barang *:", txtNama));
        form.add(labelField("Tanggal Kejadian (YYYY-MM-DD) *:", txtTanggal));
        form.add(labelField("Lokasi Kejadian *:", txtLokasi));
        form.add(labelField("Ciri Khusus Rahasia:", txtCiri));

        // Dynamic Elektronik vs Non-Elektronik
        dynamicAttrLayout = new CardLayout();
        dynamicAttrCard = new JPanel(dynamicAttrLayout);
        dynamicAttrCard.setOpaque(false);

        JPanel pEl = new JPanel(new GridLayout(2, 2, 10, 8));
        pEl.setBorder(BorderFactory.createTitledBorder("Atribut Elektronik (Bukti Akun Terdaftar v2.0)"));
        pEl.setBackground(ModernSwingKit.SLATE_50);
        txtMerek = new JTextField();
        txtTipe = new JTextField();
        txtAkunTerdaftar = new JTextField();
        txtNomorSeri = new JTextField();
        pEl.add(labelField("Merk / Brand:", txtMerek));
        pEl.add(labelField("Tipe / Model:", txtTipe));
        pEl.add(labelField("Email/No.HP Terdaftar (OTP):", txtAkunTerdaftar));
        pEl.add(labelField("Nomor Seri / IMEI (Opsional):", txtNomorSeri));

        JPanel pNonEl = new JPanel(new GridLayout(1, 2, 10, 8));
        pNonEl.setBorder(BorderFactory.createTitledBorder("Atribut Non-Elektronik"));
        pNonEl.setBackground(ModernSwingKit.SLATE_50);
        txtWarna = new JTextField();
        txtBahan = new JTextField();
        pNonEl.add(labelField("Warna Dominan:", txtWarna));
        pNonEl.add(labelField("Bahan / Material:", txtBahan));

        dynamicAttrCard.add(pEl, "EL");
        dynamicAttrCard.add(pNonEl, "NON");
        cmbKategori.addActionListener(e -> {
            KategoriBarang kat = (KategoriBarang) cmbKategori.getSelectedItem();
            dynamicAttrLayout.show(dynamicAttrCard, kat == KategoriBarang.ELEKTRONIK ? "EL" : "NON");
        });

        JButton btnKirim = ModernSwingKit.createButton("🚀 Kirim Laporan Baru", ModernSwingKit.BLUE_600, Color.WHITE);
        btnKirim.addActionListener(e -> submitFormLaporan());

        JPanel centerBox = new JPanel(new BorderLayout(0, 12));
        centerBox.setOpaque(false);
        centerBox.add(form, BorderLayout.NORTH);
        centerBox.add(dynamicAttrCard, BorderLayout.CENTER);
        centerBox.add(labelField("Deskripsi Singkat Kejadian *:", new JScrollPane(txtDeskripsi)), BorderLayout.SOUTH);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        bottomBar.add(btnKirim);

        wrapper.add(centerBox, BorderLayout.CENTER);
        wrapper.add(bottomBar, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel labelField(String title, java.awt.Component comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setFont(Fonts.sans(12f, Font.BOLD));
        l.setForeground(ModernSwingKit.SLATE_700);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void submitFormLaporan() {
        try {
            JenisLaporan jenis = (JenisLaporan) cmbJenis.getSelectedItem();
            KategoriBarang kat = (KategoriBarang) cmbKategori.getSelectedItem();
            String nama = txtNama.getText().trim();
            String lokasi = txtLokasi.getText().trim();
            String desk = txtDeskripsi.getText().trim();
            String ciri = txtCiri.getText().trim();
            LocalDate tgl = LocalDate.parse(txtTanggal.getText().trim());

            if (tgl.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Tanggal kejadian tidak boleh di masa depan!");
            }
            if (nama.isEmpty() || lokasi.isEmpty() || desk.isEmpty()) {
                throw new IllegalArgumentException("Nama barang, lokasi kejadian, dan deskripsi wajib diisi!");
            }

            String id = itemRepository.nextId();
            Item item;
            if (kat == KategoriBarang.ELEKTRONIK) {
                item = new BarangElektronik(
                        id, jenis, nama, kat, desk, lokasi, tgl,
                        jenis == JenisLaporan.TEMUAN ? "POS-0001" : null, null, ciri,
                        StatusItem.DILAPORKAN, LocalDateTime.now(), activeUser,
                        txtMerek.getText().trim(), txtNomorSeri.getText().trim(),
                        txtTipe.getText().trim(), txtAkunTerdaftar.getText().trim()
                );
            } else {
                item = new BarangNonElektronik(
                        id, jenis, nama, kat, desk, lokasi, tgl,
                        jenis == JenisLaporan.TEMUAN ? "POS-0001" : null, null, ciri,
                        StatusItem.DILAPORKAN, LocalDateTime.now(), activeUser,
                        txtWarna.getText().trim(), txtBahan.getText().trim()
                );
            }

            itemRepository.save(item);
            JOptionPane.showMessageDialog(this,
                    "Laporan [" + id + "] berhasil dikirim dengan status DILAPORKAN.\nMenunggu verifikasi Petugas Security.",
                    "Laporan Tersimpan (US-009)", JOptionPane.INFORMATION_MESSAGE);

            txtNama.setText("");
            txtLokasi.setText("");
            txtDeskripsi.setText("");
            txtCiri.setText("");
            refreshData();
            tabs.setSelectedIndex(0);
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Validasi Gagal: " + ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void tampilkanDetailMine(String itemId) {
        itemRepository.findById(itemId).ifPresent(item -> {
            stepperContainer.removeAll();
            stepperContainer.add(ModernSwingKit.createStatusStepper(item.getStatus()));
            stepperContainer.revalidate();
            stepperContainer.repaint();

            txtDetail.setText(item.tampilkanDetail() + "\n\nLokasi Simpan: " + petugasService.getLokasiSimpan(item));
            btnCekKandidat.setEnabled(item.getJenis() == JenisLaporan.HILANG);
        });
    }

    private void aksiCekKandidatTerpilih() {
        int row = tblMine.getSelectedRow();
        if (row < 0) return;
        String id = (String) modelMine.getValueAt(row, 0);
        Item hilang = itemRepository.findById(id).orElse(null);
        if (hilang == null) return;

        Laporan lapH = Laporan.dariItem(hilang);
        List<Item> cocokItems = new ArrayList<>();
        StringBuilder sb = new StringBuilder("Kandidat Barang Temuan Cocok (>= 70%):\n\n");
        for (Item t : itemRepository.findAll()) {
            if (t.getJenis() == JenisLaporan.TEMUAN
                    && (t.getStatus() == StatusItem.TERSEDIA || t.getStatus() == StatusItem.DIVERIFIKASI || t.getStatus() == StatusItem.COCOK)) {
                MatchResult mr = matchingEngine.evaluasiDetail(lapH, Laporan.dariItem(t));
                if (mr.getSkor() >= 70) {
                    cocokItems.add(t);
                    sb.append("• [").append(t.getId()).append("] ").append(t.getNama())
                      .append(" — Skor ").append(mr.getSkor()).append("%\n")
                      .append("  Alasan: ").append(String.join(", ", mr.getAlasan())).append("\n\n");
                }
            }
        }
        if (cocokItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Belum ada kandidat barang temuan yang mencapai skor >= 70%.",
                    "Kandidat Cocok (US-015)", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Kandidat Barang Cocok (US-015)", JOptionPane.INFORMATION_MESSAGE);
    }

    private void aksiAjukanKlaimDariTabel() {
        int row = tblPublic.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu barang temuan terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String itemId = (String) modelPublic.getValueAt(row, 0);
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) return;

        JComboBox<JenisBukti> cmbBukti = new JComboBox<>(item.getSyaratBuktiUtama());
        JTextArea txtBukti = new JTextArea(4, 28);
        txtBukti.setLineWrap(true);
        final File[] selectedFile = {null};
        JButton btnFile = new JButton("📎 Lampirkan Foto/Nota (Opsional)");
        btnFile.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fc.getSelectedFile();
                btnFile.setText("📎 " + selectedFile[0].getName());
            }
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new GridLayout(2, 1, 4, 4));
        top.add(labelField("Metode Bukti Kepemilikan (v2.0):", cmbBukti));
        top.add(btnFile);
        panel.add(top, BorderLayout.NORTH);
        panel.add(labelField("Penjelasan Ciri Khusus / Akun Terdaftar *:", new JScrollPane(txtBukti)), BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, panel,
                "Ajukan Klaim Kepemilikan (" + item.getNama() + ")", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            String ciri = txtBukti.getText().trim();
            if (ciri.length() < 5) {
                throw new IllegalArgumentException("Jelaskan bukti kepemilikan Anda minimal 5 karakter.");
            }
            Path tmpProof = null;
            File fileBukti = selectedFile[0];
            if (fileBukti == null) {
                Files.createDirectories(Path.of("data", "bukti"));
                tmpProof = Files.createTempFile(Path.of("data", "bukti"), "mhs-bukti-", ".txt");
                Files.writeString(tmpProof, ciri);
                fileBukti = tmpProof.toFile();
            }
            try {
                Klaim klaim = klaimService.ajukan(
                        itemId, activeUser.getNomorIdentitas(), activeUser.getNama(),
                        ciri, fileBukti, LocalDate.now().plusDays(1), SesiLayanan.PAGI, true, true
                );
                klaim.setJenisBukti((JenisBukti) cmbBukti.getSelectedItem());
                klaimRepository.save(klaim);
                JOptionPane.showMessageDialog(this, "Klaim [" + klaim.getId() + "] berhasil diajukan! Menunggu pemeriksaan Security.",
                        "Klaim Terkirim (US-019)", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
                tabs.setSelectedIndex(3);
                if (onDataChanged != null) onDataChanged.run();
            } finally {
                if (tmpProof != null) Files.deleteIfExists(tmpProof);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal Mengajukan Klaim: " + ex.getMessage(), "Ditolak", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiMahasiswaInputOtp() {
        int row = tblKlaim.getSelectedRow();
        if (row < 0) return;
        String klaimId = (String) modelKlaim.getValueAt(row, 0);
        String otp = JOptionPane.showInputDialog(this, "Masukkan 6-Digit Kode OTP yang dikirim ke akun Anda:");
        if (otp == null || otp.isBlank()) return;
        try {
            boolean valid = klaimService.verifikasiTantanganOTP(klaimId, otp);
            if (valid) {
                JOptionPane.showMessageDialog(this, "✅ Kode OTP COCOK! Kepemilikan akun perangkat terverifikasi.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Kode OTP tidak cocok!", "Gagal", JOptionPane.WARNING_MESSAGE);
            }
            refreshData();
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiMahasiswaBatalkanKlaim() {
        int row = tblKlaim.getSelectedRow();
        if (row < 0) return;
        String klaimId = (String) modelKlaim.getValueAt(row, 0);
        try {
            klaimService.batalkan(klaimId, activeUser.getNomorIdentitas());
            JOptionPane.showMessageDialog(this, "Klaim " + klaimId + " berhasil dibatalkan.", "Dibatalkan", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        if (activeUser == null) return;
        modelMine.setRowCount(0);
        modelPublic.setRowCount(0);
        modelKlaim.setRowCount(0);

        int countMine = 0;
        int countPub = 0;
        for (Item item : itemRepository.findAll()) {
            if (item.getPelapor() != null && item.getPelapor().getNomorIdentitas().equals(activeUser.getNomorIdentitas())) {
                modelMine.addRow(new Object[]{
                        item.getId(), item.getJenis().name(), item.getNama(),
                        item.getKategori().getLabel(), item.getLokasiKejadian(),
                        item.getTanggalKejadian(), item.getStatus().name()
                });
                countMine++;
            }
            if (item.getJenis() == JenisLaporan.TEMUAN
                    && (item.getStatus() == StatusItem.TERSEDIA || item.getStatus() == StatusItem.DIVERIFIKASI || item.getStatus() == StatusItem.COCOK)) {
                modelPublic.addRow(new Object[]{
                        item.getId(), item.getNama(), Laporan.dariItem(item).tampilkanKePublik(),
                        petugasService.getLokasiSimpan(item), item.getStatus().getLabel()
                });
                countPub++;
            }
        }

        List<Klaim> klaimSaya = klaimRepository.findByPengklaim(activeUser.getNomorIdentitas());
        for (Klaim k : klaimSaya) {
            String itemName = itemRepository.findById(k.getItemId()).map(Item::getNama).orElse(k.getItemId());
            String infoOtp = k.isOtpTerverifikasi() ? "✅ OTP Terverifikasi"
                    : (k.getKodeOTP() != null ? "Kode OTP: " + k.getKodeOTP() : (k.getCatatanPetugas() != null ? k.getCatatanPetugas() : "-"));
            modelKlaim.addRow(new Object[]{
                    k.getId(), k.getItemId(), itemName, k.getCiriKhusus(), k.getStatus().name(), infoOtp
            });
        }

        lblKpiLaporan.setText(String.valueOf(countMine));
        lblKpiKlaim.setText(String.valueOf(klaimSaya.size()));
        lblKpiTemuan.setText(String.valueOf(countPub));
    }
}
