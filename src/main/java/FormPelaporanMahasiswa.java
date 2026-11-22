import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class FormPelaporanMahasiswa extends JDialog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Color BG_MAIN = new Color(244, 246, 249);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color BORDER_LIGHT = new Color(226, 232, 240);

    private final String namaMahasiswa;
    private final Consumer<LaporanMahasiswaItem> onLaporanTersimpan;

    private JComboBox<String> cbJenisLaporan;
    private JTextField tfNamaBarang;
    private JComboBox<String> cbKategori;
    private JTextField tfWarna;
    private JTextField tfLokasiKejadian;
    private JTextField tfKeteranganLokasi;
    private JTextField tfWaktuKejadian;
    private JTextArea taDeskripsi;
    private JLabel lblPathFoto;
    private String pathFotoTerpilih = "";

    private JPanel panelDinamis;
    private CardLayout cardLayoutDinamis;
    private JTextField tfMerk;
    private JTextField tfNomorSeri;
    private JTextField tfBahanJenis;
    private JTextField tfCiriFisik;

    public FormPelaporanMahasiswa(Frame owner, String namaMahasiswa,
            Consumer<LaporanMahasiswaItem> onLaporanTersimpan) {
        super(owner, "Buat Laporan Baru — CampusFind", true);
        this.namaMahasiswa = namaMahasiswa;
        this.onLaporanTersimpan = onLaporanTersimpan;

        setSize(560, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        // Header Banner
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel lblJudul = new JLabel("Form Pelaporan Barang Hilang / Temuan");
        lblJudul.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblJudul.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Lengkapi detail barang dengan jelas agar proses pencocokan lebih akurat");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(219, 234, 254));

        header.add(lblJudul);
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // Konten Utama
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_MAIN);
        content.setBorder(new EmptyBorder(14, 16, 14, 16));

        content.add(buildFormUtama());
        content.add(Box.createVerticalStrut(12));
        content.add(buildPanelDinamis());
        content.add(Box.createVerticalStrut(12));
        content.add(buildPanelFoto());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        add(buildFooterButtons(), BorderLayout.SOUTH);

        cardLayoutDinamis.show(panelDinamis, (String) cbKategori.getSelectedItem());
    }

    private JPanel buildCardContainer(String title) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel lblSection = new JLabel(title);
        lblSection.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSection.setForeground(new Color(30, 41, 59));
        wrapper.add(lblSection, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildFormUtama() {
        JPanel card = buildCardContainer("1. Informasi Utama Barang");
        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 8));
        grid.setOpaque(false);

        grid.add(new JLabel("Jenis Laporan *:"));
        cbJenisLaporan = new JComboBox<>(new String[] { "Hilang", "Temuan" });
        grid.add(cbJenisLaporan);

        grid.add(new JLabel("Nama Barang *:"));
        tfNamaBarang = new JTextField();
        grid.add(tfNamaBarang);

        grid.add(new JLabel("Kategori *:"));
        cbKategori = new JComboBox<>(new String[] { "Elektronik", "Non-Elektronik" });
        grid.add(cbKategori);

        grid.add(new JLabel("Warna *:"));
        tfWarna = new JTextField();
        grid.add(tfWarna);

        grid.add(new JLabel("Lokasi Kejadian (Tempat) *:"));
        tfLokasiKejadian = new JTextField();
        grid.add(tfLokasiKejadian);

        grid.add(new JLabel("Keterangan Lokasi *:"));
        tfKeteranganLokasi = new JTextField();
        grid.add(tfKeteranganLokasi);

        grid.add(new JLabel("Waktu Kejadian (yyyy-MM-dd HH:mm) *:"));
        tfWaktuKejadian = new JTextField(LocalDateTime.now().minusHours(1).format(FORMATTER));
        grid.add(tfWaktuKejadian);

        grid.add(new JLabel("Deskripsi Detail *:"));
        taDeskripsi = new JTextArea(3, 20);
        taDeskripsi.setLineWrap(true);
        taDeskripsi.setWrapStyleWord(true);
        grid.add(new JScrollPane(taDeskripsi));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPanelDinamis() {
        JPanel card = buildCardContainer("2. Spesifikasi Khusus Kategori");

        cardLayoutDinamis = new CardLayout();
        panelDinamis = new JPanel(cardLayoutDinamis);
        panelDinamis.setOpaque(false);

        JPanel panelElektronik = new JPanel(new GridLayout(2, 2, 10, 8));
        panelElektronik.setOpaque(false);
        panelElektronik.add(new JLabel("Merk Perangkat *:"));
        tfMerk = new JTextField();
        panelElektronik.add(tfMerk);
        panelElektronik.add(new JLabel("Nomor Seri / IMEI *:"));
        tfNomorSeri = new JTextField();
        panelElektronik.add(tfNomorSeri);

        JPanel panelNonElektronik = new JPanel(new GridLayout(2, 2, 10, 8));
        panelNonElektronik.setOpaque(false);
        panelNonElektronik.add(new JLabel("Bahan / Jenis *:"));
        tfBahanJenis = new JTextField();
        panelNonElektronik.add(tfBahanJenis);
        panelNonElektronik.add(new JLabel("Ciri Fisik Khusus *:"));
        tfCiriFisik = new JTextField();
        panelNonElektronik.add(tfCiriFisik);

        panelDinamis.add(panelElektronik, "Elektronik");
        panelDinamis.add(panelNonElektronik, "Non-Elektronik");

        cbKategori.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                cardLayoutDinamis.show(panelDinamis, (String) e.getItem());
            }
        });

        card.add(panelDinamis, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPanelFoto() {
        JPanel card = buildCardContainer("3. Lampiran Foto Barang (Opsional)");
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        lblPathFoto = new JLabel("Belum ada file foto dipilih");
        lblPathFoto.setForeground(new Color(100, 116, 139));

        JButton btnPilihFoto = new JButton("Pilih Gambar...");
        btnPilihFoto.setFocusPainted(false);
        btnPilihFoto.addActionListener(e -> pilihFileFoto());

        row.add(lblPathFoto, BorderLayout.CENTER);
        row.add(btnPilihFoto, BorderLayout.EAST);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private void pilihFileFoto() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            pathFotoTerpilih = file.getAbsolutePath();
            lblPathFoto.setText(file.getName());
            lblPathFoto.setForeground(new Color(30, 41, 59));
        }
    }

    private JPanel buildFooterButtons() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new LineBorder(BORDER_LIGHT, 1));

        JButton btnBatal = new JButton("Batal");
        btnBatal.setFocusPainted(false);
        btnBatal.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnBatal.addActionListener(e -> dispose());

        JButton btnSimpan = new JButton("Kirim Laporan");
        btnSimpan.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSimpan.setBackground(PRIMARY);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFocusPainted(false);
        btnSimpan.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnSimpan.addActionListener(e -> validasiDanSimpan());

        footer.add(btnBatal);
        footer.add(btnSimpan);
        return footer;
    }

    private void validasiDanSimpan() {
        String jenis = (String) cbJenisLaporan.getSelectedItem();
        String namaBarang = tfNamaBarang.getText().trim();
        String kategori = (String) cbKategori.getSelectedItem();
        String warna = tfWarna.getText().trim();
        String lokasi = tfLokasiKejadian.getText().trim();
        String ketLokasi = tfKeteranganLokasi.getText().trim();
        String waktuStr = tfWaktuKejadian.getText().trim();
        String deskripsi = taDeskripsi.getText().trim();

        if (namaBarang.isEmpty() || warna.isEmpty() || lokasi.isEmpty() || ketLokasi.isEmpty() || deskripsi.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Semua kolom bertanda (*) wajib diisi!",
                    "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime waktuKejadian;
        try {
            waktuKejadian = LocalDateTime.parse(waktuStr, FORMATTER);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Format waktu kejadian harus yyyy-MM-dd HH:mm (contoh: 2026-11-21 10:30).",
                    "Format Waktu Salah", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (waktuKejadian.isAfter(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this,
                    "Waktu kejadian tidak boleh di masa depan!",
                    "Validasi Waktu Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String merk = "";
        String noSeri = "";
        String bahanJenis = "";
        String ciriFisik = "";

        if ("Elektronik".equals(kategori)) {
            merk = tfMerk.getText().trim();
            noSeri = tfNomorSeri.getText().trim();
            if (merk.isEmpty() || noSeri.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Untuk kategori Elektronik, Merk dan Nomor Seri wajib diisi!",
                        "Validasi Kategori Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            bahanJenis = tfBahanJenis.getText().trim();
            ciriFisik = tfCiriFisik.getText().trim();
            if (bahanJenis.isEmpty() || ciriFisik.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Untuk kategori Non-Elektronik, Bahan/Jenis dan Ciri Fisik wajib diisi!",
                        "Validasi Kategori Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String prefix = "Hilang".equalsIgnoreCase(jenis) ? "H-" : "T-";
        String idBaru = prefix + (System.currentTimeMillis() % 10000);

        LaporanMahasiswaItem itemBaru = new LaporanMahasiswaItem(
                idBaru, namaMahasiswa, jenis, namaBarang, kategori, warna,
                lokasi, ketLokasi, waktuKejadian, deskripsi, pathFotoTerpilih,
                merk, noSeri, bahanJenis, ciriFisik, "DILAPORKAN");

        if (onLaporanTersimpan != null) {
            onLaporanTersimpan.accept(itemBaru);
        }

        JOptionPane.showMessageDialog(this,
                "Laporan berhasil dikirim dengan status DILAPORKAN (ID: " + idBaru + ").",
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}