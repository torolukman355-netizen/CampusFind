import model.*;
import model.enums.HakAkses;
import model.enums.JenisLaporan;
import model.enums.KategoriItem;
import model.enums.StatusItem;
import repository.ItemRepository;
import repository.UserRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * =============================================================================
 * CampusFind — Modul Role Admin (Bagian Lukman)
 * =============================================================================
 * - US-010 : Dashboard Ringkasan Statistik Sistem (Polimorfisme tampilDashboard)
 * - US-022 : Manajemen Akun Pengguna (Lihat, Tambah, Ubah Role, Nonaktifkan, Proteksi Diri)
 * - US-023, US-031 : Layanan & Visualisasi Statistik (Bar Chart Kategori & Jenis,
 *                    Return Rate, Rata-rata waktu, Penanganan data kosong aman)
 * - US-003, US-007, US-008 : Kelola Semua Laporan via ItemRepository.findAll()
 *                            dan Polimorfisme Item.tampilkanDetail()
 * =============================================================================
 */
public class AdminDashboardPanel extends JPanel {

    private final Admin admin;
    private final ItemRepository itemRepo = ItemRepository.getInstance();
    private final UserRepository userRepo = UserRepository.getInstance();

    private JTabbedPane tabbedPane;

    // KPI Cards
    private JLabel lblTotalLaporan, lblTotalUsers, lblTotalKembali, lblReturnRate;

    // Komponen Manajemen User (US-022)
    private JTable tblUsers;
    private DefaultTableModel modelUsers;

    // Komponen Kelola Semua Laporan (US-003, US-007)
    private JTable tblSemuaBarang;
    private DefaultTableModel modelSemuaBarang;
    private JTextArea txtDetailBarang;

    public AdminDashboardPanel(Admin admin) {
        this.admin = admin;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        initHeader();
        initTabs();

        refreshAllAdminViews();
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("CampusFind — Modul Administrator Utama (" + admin.getNama() + ")");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(33, 37, 41));

        JLabel sub = new JLabel("Pusat Kontrol Sistem: Manajemen Pengguna & Analisis Statistik Layanan");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(Color.GRAY);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        header.add(left, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Tab 1: Ringkasan & Visualisasi Statistik (US-010, US-023, US-031)
        tabbedPane.addTab("1. Ringkasan & Grafik Statistik", createStatistikPanel());

        // Tab 2: Manajemen Akun Pengguna (US-022)
        tabbedPane.addTab("2. Manajemen Pengguna", createManajemenUserPanel());

        // Tab 3: Kelola Semua Laporan Barang (US-003, US-007, US-008)
        tabbedPane.addTab("3. Semua Laporan Barang (ItemRepository)", createSemuaLaporanPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. STATISTIK & VISUALISASI GRAFIK BATANG (US-023, US-031)
    // =========================================================================
    private JPanel createStatistikPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        // 4 KPI Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);

        lblTotalLaporan = new JLabel("0");
        lblTotalUsers = new JLabel("0");
        lblTotalKembali = new JLabel("0");
        lblReturnRate = new JLabel("0%");

        kpiPanel.add(createKpiCard("Total Laporan Masuk", lblTotalLaporan, new Color(41, 128, 185)));
        kpiPanel.add(createKpiCard("Pengguna Terdaftar", lblTotalUsers, new Color(39, 174, 96)));
        kpiPanel.add(createKpiCard("Barang Dikembalikan", lblTotalKembali, new Color(142, 68, 173)));
        kpiPanel.add(createKpiCard("Tingkat Pengembalian", lblReturnRate, new Color(230, 126, 34)));

        // Custom Visualisasi Grafik Batang (Bar Chart)
        JPanel chartContainer = new JPanel(new GridLayout(1, 2, 15, 0));
        chartContainer.setOpaque(false);

        JPanel chartKategori = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                gambarBarChartKategori((Graphics2D) g, getWidth(), getHeight());
            }
        };
        chartKategori.setBackground(Color.WHITE);
        chartKategori.setBorder(BorderFactory.createTitledBorder("Rekap Jumlah Laporan Berdasarkan Kategori"));

        JPanel chartJenis = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                gambarBarChartJenis((Graphics2D) g, getWidth(), getHeight());
            }
        };
        chartJenis.setBackground(Color.WHITE);
        chartJenis.setBorder(BorderFactory.createTitledBorder("Rekap Rasio Jenis Laporan (Hilang vs Temuan)"));

        chartContainer.add(chartKategori);
        chartContainer.add(chartJenis);

        panel.add(kpiPanel, BorderLayout.NORTH);
        panel.add(chartContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 4, 0, 0, color),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setForeground(Color.DARK_GRAY);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(color);

        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // Menggambar Grafik Batang Kategori dengan Penanganan Data Kosong (US-031)
    private void gambarBarChartKategori(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int countElektronik = 0;
        int countNonElektronik = 0;
        for (Item item : itemRepo.findAll()) {
            if (item.getKategori() == KategoriItem.ELEKTRONIK) countElektronik++;
            else countNonElektronik++;
        }

        int max = Math.max(countElektronik, countNonElektronik);
        if (max == 0) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
            g2.drawString("Belum ada data laporan yang tersedia.", w / 2 - 110, h / 2);
            return;
        }

        int barWidth = 70;
        int baseY = h - 60;
        int maxHeight = h - 110;

        int h1 = (int) (((double) countElektronik / max) * maxHeight);
        int h2 = (int) (((double) countNonElektronik / max) * maxHeight);

        // Bar Elektronik
        g2.setColor(new Color(52, 152, 219));
        g2.fillRect(w / 4 - barWidth / 2, baseY - h1, barWidth, h1);
        g2.setColor(Color.BLACK);
        g2.drawString(countElektronik + " Laporan", w / 4 - 25, baseY - h1 - 8);
        g2.drawString("Elektronik", w / 4 - 28, baseY + 20);

        // Bar Non-Elektronik
        g2.setColor(new Color(46, 204, 113));
        g2.fillRect((w * 3) / 4 - barWidth / 2, baseY - h2, barWidth, h2);
        g2.setColor(Color.BLACK);
        g2.drawString(countNonElektronik + " Laporan", (w * 3) / 4 - 25, baseY - h2 - 8);
        g2.drawString("Non-Elektronik", (w * 3) / 4 - 38, baseY + 20);

        // Garis dasar
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(20, baseY, w - 20, baseY);
    }

    private void gambarBarChartJenis(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int countHilang = itemRepo.findByJenis(JenisLaporan.HILANG).size();
        int countTemuan = itemRepo.findByJenis(JenisLaporan.TEMUAN).size();

        int max = Math.max(countHilang, countTemuan);
        if (max == 0) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
            g2.drawString("Belum ada data laporan jenis hilang/temuan.", w / 2 - 120, h / 2);
            return;
        }

        int barWidth = 70;
        int baseY = h - 60;
        int maxHeight = h - 110;

        int h1 = (int) (((double) countHilang / max) * maxHeight);
        int h2 = (int) (((double) countTemuan / max) * maxHeight);

        // Bar Hilang
        g2.setColor(new Color(231, 76, 60));
        g2.fillRect(w / 4 - barWidth / 2, baseY - h1, barWidth, h1);
        g2.setColor(Color.BLACK);
        g2.drawString(countHilang + " Laporan", w / 4 - 25, baseY - h1 - 8);
        g2.drawString("Hilang", w / 4 - 18, baseY + 20);

        // Bar Temuan
        g2.setColor(new Color(155, 89, 182));
        g2.fillRect((w * 3) / 4 - barWidth / 2, baseY - h2, barWidth, h2);
        g2.setColor(Color.BLACK);
        g2.drawString(countTemuan + " Laporan", (w * 3) / 4 - 25, baseY - h2 - 8);
        g2.drawString("Temuan", (w * 3) / 4 - 22, baseY + 20);

        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(20, baseY, w - 20, baseY);
    }

    // =========================================================================
    // 2. MANAJEMEN AKUN PENGGUNA (US-022)
    // =========================================================================
    private JPanel createManajemenUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        modelUsers = new DefaultTableModel(new String[]{"ID User", "Nama", "Email", "Role", "Status Akun"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUsers = new JTable(modelUsers);
        tblUsers.setRowHeight(25);

        // Toolbar Tombol Aksi Admin
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(Color.WHITE);

        JButton btnTambahUser = new JButton("+ Tambah Pengguna Baru");
        btnTambahUser.setBackground(new Color(39, 174, 96));
        btnTambahUser.setForeground(Color.WHITE);
        btnTambahUser.addActionListener(e -> dialogTambahUser());

        JButton btnToggleStatus = new JButton("Aktifkan / Nonaktifkan Akun");
        btnToggleStatus.setBackground(new Color(230, 126, 34));
        btnToggleStatus.setForeground(Color.WHITE);
        btnToggleStatus.addActionListener(e -> toggleStatusUser());

        toolbar.add(btnTambahUser);
        toolbar.add(btnToggleStatus);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblUsers), BorderLayout.CENTER);
        return panel;
    }

    private void dialogTambahUser() {
        JTextField txtId = new JTextField("U-" + (userRepo.findAll().size() + 10));
        JTextField txtNama = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Petugas", "Mahasiswa", "Admin"});

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("ID User:"));
        form.add(txtId);
        form.add(new JLabel("Nama Lengkap:"));
        form.add(txtNama);
        form.add(new JLabel("Email (Harus Unik):"));
        form.add(txtEmail);
        form.add(new JLabel("Kata Sandi (Min 8 Karakter):"));
        form.add(txtPass);
        form.add(new JLabel("Pilihan Role:"));
        form.add(cmbRole);

        int res = JOptionPane.showConfirmDialog(this, form, "Buat Akun Pengguna Baru (US-022)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String nama = txtNama.getText().trim();

            if (nama.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua kolom wajib diisi!", "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validasi email unik
            if (userRepo.findByEmail(email).isPresent()) {
                JOptionPane.showMessageDialog(this, "Email sudah terdaftar dalam sistem!", "Email Duplikat", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validasi sandi minimal 8 karakter (US-022)
            if (pass.length() < 8) {
                JOptionPane.showMessageDialog(this, "Kata sandi wajib memiliki minimal 8 karakter!", "Sandi Terlalu Pendek", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String role = (String) cmbRole.getSelectedItem();
            User newUser;
            if ("Petugas".equals(role)) {
                newUser = new Petugas(txtId.getText().trim(), nama, email, pass, "NIP-NEW");
            } else if ("Admin".equals(role)) {
                newUser = new Admin(txtId.getText().trim(), nama, email, pass);
            } else {
                newUser = new Mahasiswa(txtId.getText().trim(), nama, email, pass, "NIM-NEW");
            }

            userRepo.save(newUser);
            JOptionPane.showMessageDialog(this, "Akun baru berhasil dibuat!");
            refreshAllAdminViews();
        }
    }

    private void toggleStatusUser() {
        int row = tblUsers.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih baris pengguna yang ingin diubah statusnya.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idUser = (String) modelUsers.getValueAt(row, 0);
        User user = userRepo.findById(idUser).orElse(null);
        if (user == null) return;

        // Proteksi Diri: Admin tidak dapat menonaktifkan akunnya sendiri (US-022)
        if (user.getId().equalsIgnoreCase(admin.getId())) {
            JOptionPane.showMessageDialog(this,
                    "Proteksi Keamanan: Admin tidak diperbolehkan menonaktifkan akun miliknya sendiri!",
                    "Aksi Ditolak", JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setAktif(!user.isAktif());
        userRepo.save(user);

        String statusStr = user.isAktif() ? "DIAKTIFKAN" : "DINONAKTIFKAN (Otomatis ditolak saat login)";
        JOptionPane.showMessageDialog(this, "Status akun " + user.getNama() + " berhasil diubah menjadi: " + statusStr);
        refreshAllAdminViews();
    }

    // =========================================================================
    // 3. KELOLA SEMUA LAPORAN (US-003, US-007, US-008)
    // =========================================================================
    private JPanel createSemuaLaporanPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        modelSemuaBarang = new DefaultTableModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Status", "Pelapor"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSemuaBarang = new JTable(modelSemuaBarang);
        tblSemuaBarang.setRowHeight(25);

        txtDetailBarang = new JTextArea();
        txtDetailBarang.setEditable(false);
        txtDetailBarang.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetailBarang.setBorder(new EmptyBorder(8, 8, 8, 8));

        tblSemuaBarang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblSemuaBarang.getSelectedRow() != -1) {
                String id = (String) modelSemuaBarang.getValueAt(tblSemuaBarang.getSelectedRow(), 0);
                itemRepo.findById(id).ifPresent(item -> {
                    // Polimorfisme: Memanggil item.tampilkanDetail()
                    txtDetailBarang.setText(item.tampilkanDetail());
                });
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tblSemuaBarang), new JScrollPane(txtDetailBarang));
        split.setDividerLocation(520);

        panel.add(new JLabel("Admin memiliki HakAkses.LIHAT_SEMUA_BARANG via ItemRepository.findAll():"), BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    public void refreshAllAdminViews() {
        List<Item> allItems = itemRepo.findAll();
        List<User> allUsers = userRepo.findAll();

        // 1. Refresh KPI
        lblTotalLaporan.setText(String.valueOf(allItems.size()));
        lblTotalUsers.setText(String.valueOf(allUsers.size()));

        int countKembali = itemRepo.countByStatus(StatusItem.DIKEMBALIKAN);
        lblTotalKembali.setText(String.valueOf(countKembali));

        // Return Rate: DIKEMBALIKAN / Total Laporan Hilang Terverifikasi
        long totalHilangVerif = allItems.stream()
                .filter(i -> i.getJenis() == JenisLaporan.HILANG && i.getStatus() != StatusItem.DILAPORKAN && i.getStatus() != StatusItem.DITOLAK)
                .count();

        double returnRate = (totalHilangVerif > 0) ? ((double) countKembali / totalHilangVerif) * 100.0 : 0.0;
        lblReturnRate.setText(String.format("%.1f%%", returnRate));

        // 2. Refresh Tabel Pengguna (US-022)
        modelUsers.setRowCount(0);
        for (User u : allUsers) {
            modelUsers.addRow(new Object[]{
                    u.getId(), u.getNama(), u.getEmail(), u.getRole(), u.isAktif() ? "AKTIF" : "NONAKTIF"
            });
        }

        // 3. Refresh Tabel Semua Barang (US-007)
        modelSemuaBarang.setRowCount(0);
        for (Item item : allItems) {
            modelSemuaBarang.addRow(new Object[]{
                    item.getId(), item.getJenis(), item.getNama(),
                    item.getKategori(), item.getStatus(), item.getPelapor()
            });
        }

        repaint();
    }
}
