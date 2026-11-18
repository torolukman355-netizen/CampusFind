import model.*;
import model.enums.HakAkses;
import repository.ItemRepository;
import repository.UserRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * LoginFrame:
 * Form Otentikasi dan Pemilihan Role Pengguna (US-001, US-002, US-003, US-010).
 * Mengarahkan pengguna ke Dashboard sesuai Role masing-masing.
 */
public class LoginFrame extends JFrame {

    private final UserRepository userRepo = UserRepository.getInstance();
    private final JTextField txtEmail;
    private final JPasswordField txtPassword;
    private final JComboBox<String> cmbQuickUser;

    public LoginFrame() {
        setTitle("CampusFind — Login Multi-Role");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(new Color(245, 247, 250));

        // Header Title
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("CampusFind System", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(41, 128, 185));

        JLabel lblSubtitle = new JLabel("Sistem Informasi Barang Hilang & Temuan Kampus", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(Color.GRAY);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);

        // Form Input Panel
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 6, 6));
        formPanel.setOpaque(false);

        JLabel lblEmail = new JLabel("Email Pengguna:");
        txtEmail = new JTextField("bintang@campusfind.ac.id");

        JLabel lblPass = new JLabel("Kata Sandi:");
        txtPassword = new JPasswordField("petugas123");

        JLabel lblQuick = new JLabel("Pilih Akun Demo / Uji Coba Cepat:");
        cmbQuickUser = new JComboBox<>(new String[]{
                "Bintang (Petugas) — bintang@campusfind.ac.id",
                "Jepin (Petugas) — jepin@campusfind.ac.id",
                "Fakhri (Petugas) — fakhri@campusfind.ac.id",
                "Lukman (Admin) — admin@campusfind.ac.id",
                "Pitya (Mahasiswa) — pitya@student.ac.id",
                "Nadia (Mahasiswa) — nadia@student.ac.id"
        });

        cmbQuickUser.addActionListener(e -> {
            int idx = cmbQuickUser.getSelectedIndex();
            switch (idx) {
                case 0: txtEmail.setText("bintang@campusfind.ac.id"); txtPassword.setText("petugas123"); break;
                case 1: txtEmail.setText("jepin@campusfind.ac.id"); txtPassword.setText("petugas123"); break;
                case 2: txtEmail.setText("fakhri@campusfind.ac.id"); txtPassword.setText("petugas123"); break;
                case 3: txtEmail.setText("admin@campusfind.ac.id"); txtPassword.setText("admin1234"); break;
                case 4: txtEmail.setText("pitya@student.ac.id"); txtPassword.setText("mhs12345"); break;
                case 5: txtEmail.setText("nadia@student.ac.id"); txtPassword.setText("mhs12345"); break;
            }
        });

        formPanel.add(lblQuick);
        formPanel.add(cmbQuickUser);
        formPanel.add(lblEmail);
        formPanel.add(txtEmail);
        formPanel.add(lblPass);
        formPanel.add(txtPassword);

        // Button Panel
        JButton btnLogin = new JButton("Masuk ke Sistem");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 42));
        btnLogin.addActionListener(e -> prosesLogin());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnLogin, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void prosesLogin() {
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null || !user.getPassword().equals(pass)) {
            JOptionPane.showMessageDialog(this,
                    "Email atau kata sandi tidak cocok!",
                    "Gagal Masuk", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!user.isAktif()) {
            JOptionPane.showMessageDialog(this,
                    "Akun Anda telah dinonaktifkan oleh Admin. Akses ditolak!",
                    "Akun Nonaktif", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tampilkan notifikasi dashboard polimorfik (US-010)
        JOptionPane.showMessageDialog(this,
                "Selamat datang, " + user.getNama() + "!\n" +
                        "Role: " + user.getRole() + "\n" +
                        user.tampilDashboard(),
                "Login Berhasil", JOptionPane.INFORMATION_MESSAGE);

        this.dispose();

        if (user instanceof Petugas) {
            bukaDashboardPetugas((Petugas) user);
        } else if (user instanceof Admin) {
            bukaDashboardAdmin((Admin) user);
        } else if (user instanceof Mahasiswa) {
            bukaDashboardMahasiswa((Mahasiswa) user);
        }
    }

    private void bukaDashboardPetugas(Petugas petugas) {
        JFrame frame = new JFrame("CampusFind — Modul Role Petugas (" + petugas.getNama() + ")");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 680);
        frame.setLocationRelativeTo(null);
        frame.add(new PetugasDashboardPanel(petugas.getNama()));
        frame.setVisible(true);
    }

    private void bukaDashboardAdmin(Admin admin) {
        JFrame frame = new JFrame("CampusFind — Modul Role Admin (Lukman)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 550);
        frame.setLocationRelativeTo(null);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Layar Admin — Kelola & Lihat Semua Laporan Barang (US-003, US-007, US-008)");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder sb = new StringBuilder();
        sb.append("=== DATA SEMUA BARANG VIA ItemRepository.findAll() ===\n\n");
        for (Item item : ItemRepository.getInstance().findAll()) {
            // Polimorfisme Item.tampilkanDetail()
            sb.append(item.tampilkanDetail()).append("\n---------------------------------------------------------------\n");
        }
        txt.setText(sb.toString());

        p.add(title, BorderLayout.NORTH);
        p.add(new JScrollPane(txt), BorderLayout.CENTER);

        frame.add(p);
        frame.setVisible(true);
    }

    private void bukaDashboardMahasiswa(Mahasiswa mhs) {
        JFrame frame = new JFrame("CampusFind — Modul Mahasiswa (" + mhs.getNama() + ")");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 450);
        frame.setLocationRelativeTo(null);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel("Dashboard Mahasiswa (" + mhs.getNama() + " - NIM: " + mhs.getNim() + ")");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText("Hak Akses Mahasiswa:\n" +
                "- BUAT_LAPORAN (US-009)\n" +
                "- LIHAT_LAPORAN_SENDIRI (US-010)\n" +
                "- KLAIM_BARANG (US-019)\n\n" +
                "Bagian formulir pelaporan dan tracking mahasiswa dipegang oleh Pitya dan Nadia.");

        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(area), BorderLayout.CENTER);

        frame.add(p);
        frame.setVisible(true);
    }
}
