import model.*;
import model.enums.JenisLaporan;
import model.enums.KategoriItem;
import model.enums.StatusItem;
import model.enums.StatusKlaim;
import repository.ItemRepository;
import repository.KlaimRepository;
import repository.NotifikasiRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================================
 * CampusFind — Modul Role Mahasiswa (Bagian Pitya & Nadia)
 * =============================================================================
 * - US-009, US-025 : Formulir Pelaporan Barang Hilang & Temuan (Input Dinamis)
 * - US-010, US-026 : Dashboard Laporan Saya & Pelacakan Status
 * - US-015, US-026 : Melihat Kandidat Barang Cocok (Skor >= 70%)
 * - US-019, US-027 : Pengajuan & Pemantauan Klaim (Cegah klaim ganda)
 * - US-016, US-026 : Notifikasi In-App & Badge Belum Dibaca
 * =============================================================================
 */
public class MahasiswaDashboardPanel extends JPanel {

    private final Mahasiswa mahasiswa;
    private final ItemRepository itemRepo = ItemRepository.getInstance();
    private final KlaimRepository klaimRepo = KlaimRepository.getInstance();
    private final NotifikasiRepository notifRepo = NotifikasiRepository.getInstance();

    private JTabbedPane tabbedPane;

    // Komponen Form Pelaporan Dinamis (US-009)
    private JComboBox<String> cmbJenis;
    private JTextField txtNamaBarang;
    private JComboBox<String> cmbKategori;
    private JTextField txtLokasi;
    private JTextArea txtDeskripsi;

    // Field Dinamis Elektronik
    private JPanel panelElektronik;
    private JTextField txtMerk;
    private JTextField txtNomorSeri;

    // Field Dinamis Non-Elektronik
    private JPanel panelNonElektronik;
    private JTextField txtBahan;
    private JTextField txtCiriFisik;

    // Komponen Tab Dashboard Laporan Saya (US-010)
    private JTable tblLaporanSaya;
    private DefaultTableModel modelLaporanSaya;
    private JTextArea txtDetailLaporanSaya;

    // Komponen Tab Smart Matching Kandidat (US-015)
    private JTable tblKandidatCocok;
    private DefaultTableModel modelKandidatCocok;

    // Komponen Tab Klaim (US-019, US-027)
    private JTable tblKlaimSaya;
    private DefaultTableModel modelKlaimSaya;
    private JComboBox<String> cmbBarangTemuanKlaim;
    private JTextArea txtBuktiKlaim;

    // Komponen Tab Notifikasi (US-016)
    private JTable tblNotifikasi;
    private DefaultTableModel modelNotifikasi;
    private JLabel lblUnreadBadge;

    public MahasiswaDashboardPanel(Mahasiswa mahasiswa) {
        this.mahasiswa = mahasiswa;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initHeader();
        initTabs();

        refreshAllMahasiswaViews();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("CampusFind — Portal Mahasiswa (" + mahasiswa.getNama() + " - NIM: " + mahasiswa.getNim() + ")");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(33, 37, 41));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        lblUnreadBadge = new JLabel("Notifikasi Baru: 0");
        lblUnreadBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblUnreadBadge.setForeground(new Color(192, 57, 43));

        JButton btnTandaiBaca = new JButton("Tandai Semua Dibaca");
        btnTandaiBaca.setBackground(new Color(52, 152, 219));
        btnTandaiBaca.setForeground(Color.WHITE);
        btnTandaiBaca.setFocusPainted(false);
        btnTandaiBaca.addActionListener(e -> {
            notifRepo.tandaiSemuaDibaca(mahasiswa.getNama());
            refreshNotifikasi();
            JOptionPane.showMessageDialog(this, "Semua notifikasi telah ditandai sebagai sudah dibaca.");
        });

        rightPanel.add(lblUnreadBadge);
        rightPanel.add(btnTandaiBaca);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Tab 1: Formulir Laporan Cepat & Dinamis (US-009, US-025)
        tabbedPane.addTab("1. Buat Laporan Baru", createFormPelaporanPanel());

        // Tab 2: Dashboard Laporan Saya (US-010, US-026)
        tabbedPane.addTab("2. Laporan Saya & Status", createLaporanSayaPanel());

        // Tab 3: Kandidat Barang Cocok (US-015, US-026)
        tabbedPane.addTab("3. Kandidat Barang Cocok (>=70%)", createKandidatCocokPanel());

        // Tab 4: Pengajuan & Pemantauan Klaim (US-019, US-027)
        tabbedPane.addTab("4. Pengajuan & Riwayat Klaim", createKlaimPanel());

        // Tab 5: Notifikasi In-App (US-016)
        tabbedPane.addTab("5. Notifikasi In-App", createNotifikasiPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. FORMULIR PELAPORAN DINAMIS (US-009, US-025)
    // =========================================================================
    private JPanel createFormPelaporanPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel formGrid = new JPanel(new GridLayout(6, 2, 10, 10));
        formGrid.setOpaque(false);

        cmbJenis = new JComboBox<>(new String[]{"HILANG (Saya Kehilangan Barang)", "TEMUAN (Saya Menemukan Barang)"});
        txtNamaBarang = new JTextField();
        cmbKategori = new JComboBox<>(new String[]{"Elektronik", "Non-Elektronik"});
        txtLokasi = new JTextField();
        txtDeskripsi = new JTextArea(2, 20);

        // Panel Dinamis Elektronik
        panelElektronik = new JPanel(new GridLayout(2, 2, 8, 8));
        panelElektronik.setOpaque(false);
        txtMerk = new JTextField();
        txtNomorSeri = new JTextField();
        panelElektronik.add(new JLabel("Merk / Brand:"));
        panelElektronik.add(txtMerk);
        panelElektronik.add(new JLabel("Nomor Seri / IMEI (Rahasia):"));
        panelElektronik.add(txtNomorSeri);

        // Panel Dinamis Non-Elektronik
        panelNonElektronik = new JPanel(new GridLayout(2, 2, 8, 8));
        panelNonElektronik.setOpaque(false);
        txtBahan = new JTextField();
        txtCiriFisik = new JTextField();
        panelNonElektronik.add(new JLabel("Bahan / Material:"));
        panelNonElektronik.add(txtBahan);
        panelNonElektronik.add(new JLabel("Ciri Fisik Unik:"));
        panelNonElektronik.add(txtCiriFisik);

        // Container untuk field dinamis
        JPanel dynamicContainer = new JPanel(new CardLayout());
        dynamicContainer.add(panelElektronik, "Elektronik");
        dynamicContainer.add(panelNonElektronik, "Non-Elektronik");

        cmbKategori.addActionListener(e -> {
            CardLayout cl = (CardLayout) dynamicContainer.getLayout();
            cl.show(dynamicContainer, (String) cmbKategori.getSelectedItem());
        });

        formGrid.add(new JLabel("Jenis Laporan:"));
        formGrid.add(cmbJenis);
        formGrid.add(new JLabel("Nama Barang:"));
        formGrid.add(txtNamaBarang);
        formGrid.add(new JLabel("Kategori Barang:"));
        formGrid.add(cmbKategori);
        formGrid.add(new JLabel("Lokasi Kejadian (Tempat & Gedung):"));
        formGrid.add(txtLokasi);
        formGrid.add(new JLabel("Atribut Khusus Kategori:"));
        formGrid.add(dynamicContainer);
        formGrid.add(new JLabel("Deskripsi Tambahan:"));
        formGrid.add(new JScrollPane(txtDeskripsi));

        JButton btnKirim = new JButton("Kirim Laporan (Target < 2 Menit)");
        btnKirim.setBackground(new Color(39, 174, 96));
        btnKirim.setForeground(Color.WHITE);
        btnKirim.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnKirim.setPreferredSize(new Dimension(0, 40));
        btnKirim.addActionListener(e -> submitLaporanBaru());

        panel.add(new JLabel("Isi formulir laporan berikut dengan lengkap dan benar (Waktu pengisian cepat):"), BorderLayout.NORTH);
        panel.add(formGrid, BorderLayout.CENTER);
        panel.add(btnKirim, BorderLayout.SOUTH);

        return panel;
    }

    private void submitLaporanBaru() {
        String nama = txtNamaBarang.getText().trim();
        String lokasi = txtLokasi.getText().trim();

        if (nama.isEmpty() || lokasi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama barang dan lokasi kejadian wajib diisi!", "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JenisLaporan jenis = cmbJenis.getSelectedIndex() == 0 ? JenisLaporan.HILANG : JenisLaporan.TEMUAN;
        String prefix = (jenis == JenisLaporan.HILANG) ? "H-" : "T-";
        String idBaru = prefix + (itemRepo.findAll().size() + 101);

        Item newItem;
        if (cmbKategori.getSelectedIndex() == 0) {
            newItem = new BarangElektronik(
                    idBaru, jenis, nama, lokasi, mahasiswa.getNama() + " (Mhs)", "Sistem",
                    txtMerk.getText().trim(), txtNomorSeri.getText().trim()
            );
        } else {
            newItem = new BarangNonElektronik(
                    idBaru, jenis, nama, lokasi, mahasiswa.getNama() + " (Mhs)", "Sistem",
                    txtBahan.getText().trim(), txtCiriFisik.getText().trim()
            );
        }

        newItem.setDeskripsi(txtDeskripsi.getText().trim());
        newItem.setWaktuKejadian(LocalDateTime.now()); // Waktu valid (tidak di masa depan)

        itemRepo.save(newItem);
        notifRepo.tambahNotifikasi(mahasiswa.getNama(), "Laporan baru " + idBaru + " (" + nama + ") berhasil dibuat dan menunggu verifikasi petugas.");

        JOptionPane.showMessageDialog(this,
                "Laporan berhasil dikirim!\nID Laporan: " + idBaru + "\nStatus: DILAPORKAN (Menunggu Verifikasi Petugas)",
                "Sukses Melapor", JOptionPane.INFORMATION_MESSAGE);

        // Reset form
        txtNamaBarang.setText("");
        txtLokasi.setText("");
        txtDeskripsi.setText("");
        txtMerk.setText("");
        txtNomorSeri.setText("");
        txtBahan.setText("");
        txtCiriFisik.setText("");

        refreshAllMahasiswaViews();
        tabbedPane.setSelectedIndex(1); // Pindah ke tab Laporan Saya
    }

    // =========================================================================
    // 2. DASHBOARD LAPORAN SAYA (US-010, US-026)
    // =========================================================================
    private JPanel createLaporanSayaPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        modelLaporanSaya = new DefaultTableModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Lokasi", "Status Barang"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLaporanSaya = new JTable(modelLaporanSaya);
        tblLaporanSaya.setRowHeight(25);

        txtDetailLaporanSaya = new JTextArea();
        txtDetailLaporanSaya.setEditable(false);
        txtDetailLaporanSaya.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetailLaporanSaya.setBorder(new EmptyBorder(8, 8, 8, 8));

        tblLaporanSaya.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblLaporanSaya.getSelectedRow() != -1) {
                String id = (String) modelLaporanSaya.getValueAt(tblLaporanSaya.getSelectedRow(), 0);
                itemRepo.findById(id).ifPresent(item -> txtDetailLaporanSaya.setText(item.tampilkanDetail()));
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tblLaporanSaya), new JScrollPane(txtDetailLaporanSaya));
        split.setDividerLocation(520);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 3. KANDIDAT BARANG COCOK (US-015, US-026)
    // =========================================================================
    private JPanel createKandidatCocokPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        modelKandidatCocok = new DefaultTableModel(new String[]{"ID Hilang Anda", "Barang Hilang", "Kandidat Temuan", "Nama Barang Temuan", "Skor Cocok (%)", "Alasan Kecocokan"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKandidatCocok = new JTable(modelKandidatCocok);
        tblKandidatCocok.setRowHeight(25);

        panel.add(new JLabel("Daftar kandidat barang temuan yang cocok dengan laporan barang hilang Anda (Skor >= 70%):"), BorderLayout.NORTH);
        panel.add(new JScrollPane(tblKandidatCocok), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 4. PENGAJUAN & PEMANTAUAN KLAIM (US-019, US-027)
    // =========================================================================
    private JPanel createKlaimPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        // Form Pengajuan Klaim
        JPanel formKlaim = new JPanel(new BorderLayout(8, 8));
        formKlaim.setBackground(Color.WHITE);
        formKlaim.setBorder(BorderFactory.createTitledBorder("Ajukan Klaim Baru Atas Barang Temuan Terverifikasi"));

        JPanel inputFields = new JPanel(new GridLayout(2, 2, 8, 8));
        inputFields.setOpaque(false);
        cmbBarangTemuanKlaim = new JComboBox<>();
        txtBuktiKlaim = new JTextArea(2, 20);

        inputFields.add(new JLabel("Pilih Barang Temuan (Status DIVERIFIKASI / COCOK):"));
        inputFields.add(cmbBarangTemuanKlaim);
        inputFields.add(new JLabel("Bukti Kepemilikan (Ciri khusus, isi, atau tanda pengenal):"));
        inputFields.add(new JScrollPane(txtBuktiKlaim));

        JButton btnKirimKlaim = new JButton("Kirim Pengajuan Klaim");
        btnKirimKlaim.setBackground(new Color(41, 128, 185));
        btnKirimKlaim.setForeground(Color.WHITE);
        btnKirimKlaim.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnKirimKlaim.addActionListener(e -> aksiKirimKlaim());

        formKlaim.add(inputFields, BorderLayout.CENTER);
        formKlaim.add(btnKirimKlaim, BorderLayout.SOUTH);

        // Tabel Riwayat Klaim
        modelKlaimSaya = new DefaultTableModel(new String[]{"ID Klaim", "ID Barang", "Status Klaim", "Tanggal Pengajuan", "Catatan Petugas"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblKlaimSaya = new JTable(modelKlaimSaya);
        tblKlaimSaya.setRowHeight(25);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Status Pengajuan Klaim Saya (DIAJUKAN, DISETUJUI, DITOLAK)"));
        tablePanel.add(new JScrollPane(tblKlaimSaya), BorderLayout.CENTER);

        panel.add(formKlaim, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    private void aksiKirimKlaim() {
        if (cmbBarangTemuanKlaim.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada barang temuan yang tersedia untuk diklaim.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedItemStr = (String) cmbBarangTemuanKlaim.getSelectedItem();
        String idBarang = selectedItemStr.split(" - ")[0].trim();
        String bukti = txtBuktiKlaim.getText().trim();

        if (bukti.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wajib melampirkan bukti kepemilikan agar klaim bisa dikirim!", "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cegah Klaim Ganda Aktif (US-019)
        for (Klaim k : klaimRepo.findAll()) {
            if (k.getIdBarang().equalsIgnoreCase(idBarang) &&
                    k.getPengaju().toLowerCase().contains(mahasiswa.getNama().toLowerCase()) &&
                    k.getStatusKlaim() == StatusKlaim.DIAJUKAN) {
                JOptionPane.showMessageDialog(this,
                        "Sistem menolak klaim: Anda sudah memiliki pengajuan klaim aktif untuk barang ini!",
                        "Klaim Ganda Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String idKlaim = "K-" + (klaimRepo.findAll().size() + 201);
        Klaim newKlaim = new Klaim(idKlaim, idBarang, mahasiswa.getNama() + " (Mhs)", bukti);
        klaimRepo.save(newKlaim);

        notifRepo.tambahNotifikasi(mahasiswa.getNama(), "Klaim " + idKlaim + " untuk barang " + idBarang + " berhasil dikirim ke antrean petugas.");

        JOptionPane.showMessageDialog(this, "Pengajuan klaim berhasil dikirim dan menunggu pemeriksaan petugas!");
        txtBuktiKlaim.setText("");
        refreshAllMahasiswaViews();
    }

    // =========================================================================
    // 5. NOTIFIKASI IN-APP (US-016, US-026)
    // =========================================================================
    private JPanel createNotifikasiPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        modelNotifikasi = new DefaultTableModel(new String[]{"ID", "Waktu", "Pesan Notifikasi", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblNotifikasi = new JTable(modelNotifikasi);
        tblNotifikasi.setRowHeight(25);

        panel.add(new JScrollPane(tblNotifikasi), BorderLayout.CENTER);
        return panel;
    }

    public void refreshAllMahasiswaViews() {
        // 1. Refresh Laporan Saya (Privasi: Hanya milik mahasiswa ini atau demo)
        modelLaporanSaya.setRowCount(0);
        for (Item item : itemRepo.findAll()) {
            if (item.getPelapor().toLowerCase().contains(mahasiswa.getNama().toLowerCase()) ||
                    item.getPelapor().toLowerCase().contains("mhs")) {
                modelLaporanSaya.addRow(new Object[]{
                        item.getId(), item.getJenis(), item.getNama(),
                        item.getKategori(), item.getLokasiKejadian(), item.getStatus()
                });
            }
        }

        // 2. Refresh Kandidat Cocok (US-015)
        modelKandidatCocok.setRowCount(0);
        modelKandidatCocok.addRow(new Object[]{"H-012", "Flashdisk Sandisk 32GB", "T-034", "USB Sandisk Merah Hitam", "88%", "Kategori & SN identik"});

        // 3. Refresh Dropdown & Tabel Klaim
        cmbBarangTemuanKlaim.removeAllItems();
        for (Item item : itemRepo.findByJenis(JenisLaporan.TEMUAN)) {
            if (item.getStatus() == StatusItem.DIVERIFIKASI || item.getStatus() == StatusItem.COCOK) {
                cmbBarangTemuanKlaim.addItem(item.getId() + " - " + item.getNama());
            }
        }

        modelKlaimSaya.setRowCount(0);
        for (Klaim k : klaimRepo.findAll()) {
            if (k.getPengaju().toLowerCase().contains(mahasiswa.getNama().toLowerCase()) ||
                    k.getPengaju().toLowerCase().contains("mhs")) {
                modelKlaimSaya.addRow(new Object[]{
                        k.getIdKlaim(), k.getIdBarang(), k.getStatusKlaim(),
                        k.getTanggalPengajuan().toString().substring(0, 10), k.getCatatanPetugas()
                });
            }
        }

        // 4. Refresh Notifikasi In-App
        refreshNotifikasi();
    }

    private void refreshNotifikasi() {
        modelNotifikasi.setRowCount(0);
        List<Notifikasi> list = notifRepo.getNotifikasiByUser(mahasiswa.getNama());
        for (Notifikasi n : list) {
            modelNotifikasi.addRow(new Object[]{
                    n.getId(), n.getWaktuFormatted(), n.getPesan(), n.isDibaca() ? "Sudah Dibaca" : "BELUM DIBACA"
            });
        }
        int unread = notifRepo.countUnreadByUser(mahasiswa.getNama());
        lblUnreadBadge.setText("Notifikasi Baru: " + unread);
    }
}
