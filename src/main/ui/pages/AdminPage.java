package com.campusfind.ui.pages;

import com.campusfind.model.HakAkses;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.Klaim;
import com.campusfind.model.Role;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.ui.components.FooterBar;
import com.campusfind.ui.components.ItemDetailDialog;
import com.campusfind.ui.components.PageHeader;
import com.campusfind.ui.components.ResponsivePage;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import java.util.Comparator;
import java.util.function.IntConsumer;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.campusfind.model.User;

/**
 * Dashboard khusus role Admin: statistik layanan, ringkasan klaim,
 * laporan terbaru, dan daftar hak akses yang dimiliki role Admin.
 */
public class AdminPage extends ResponsivePage {

    private final NavigationController controller;
    private final PageHeader header;
    private final JPanel statRow;
    private final RoundedPanel kartuPengguna;
    private final RoundedPanel kartuStatus;
    private final RoundedPanel kartuKlaim;
    private final RoundedPanel kartuLaporan;
    private final RoundedPanel kartuHakAkses;
    private final FooterBar footerBar;
    private DefaultTableModel userTableModel;

    public AdminPage(NavigationController controller,
                     ItemService itemService,
                     KlaimRepository klaimRepository,
                     AppSession appSession) {
        super(1060);
        this.controller = controller;

        List<Item> items = itemService.findAll();
        List<Klaim> daftarKlaim = klaimRepository == null ? List.of() : klaimRepository.findAll();

        header = new PageHeader(
                "ADMINISTRASI SISTEM",
                "Dashboard Admin",
                "Ringkasan statistik layanan, pemantauan klaim, dan hak akses role Admin."
        );
        addContent(header);

        addContentStrut(24);

        statRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statRow.setOpaque(false);
        statRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        statRow.add(statCard("Total Laporan", String.valueOf(items.size()), "seluruh laporan masuk"));
        statRow.add(statCard("Barang Hilang", String.valueOf(hitungJenis(items, JenisLaporan.HILANG)), "laporan kehilangan"));
        statRow.add(statCard("Barang Temuan", String.valueOf(hitungJenis(items, JenisLaporan.TEMUAN)), "barang dititipkan"));
        statRow.add(statCard("Tingkat Pengembalian", hitungTingkatPengembalian(items) + "%", "barang kembali"));
        addContent(statRow);

        addContentStrut(20);

        kartuPengguna = buatKartuPengguna();
        addContent(kartuPengguna);

        addContentStrut(16);

        kartuStatus = buatTabel("Distribusi Status Barang",
                "Jumlah barang pada setiap status alur layanan",
                new String[]{"Status", "Jumlah"},
                dataStatus(items));
        addContent(kartuStatus);

        addContentStrut(16);

        kartuKlaim = buatTabel("Ringkasan Klaim",
                "Rekapitulasi klaim berdasarkan status keputusan",
                new String[]{"Status Klaim", "Jumlah"},
                dataKlaim(daftarKlaim));
        addContent(kartuKlaim);

        addContentStrut(16);

        List<Item> laporanTerbaru = items.stream()
                .sorted(Comparator.comparing(Item::getDibuatPada).reversed())
                .limit(8)
                .toList();

        kartuLaporan = buatTabel("Laporan Terbaru",
                "Delapan laporan terakhir - klik salah satu baris untuk melihat detail & foto",
                new String[]{"ID", "Jenis", "Nama Barang", "Status", "Pelapor", "Role"},
                dataLaporanTerbaru(laporanTerbaru),
                row -> {
                    if (row >= 0 && row < laporanTerbaru.size()) {
                        ItemDetailDialog.show(this, laporanTerbaru.get(row));
                    }
                });
        addContent(kartuLaporan);

        addContentStrut(16);

        kartuHakAkses = buatKartuHakAkses(appSession);
        addContent(kartuHakAkses);

        addContentStrut(32);

        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(24);
    }

    @Override
    protected void onContentWidthChanged(int contentWidth) {
        header.updateWidth(contentWidth);
        footerBar.updateResponsive(contentWidth);

        statRow.setMaximumSize(new Dimension(contentWidth, 120));
        kartuPengguna.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuStatus.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuKlaim.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuLaporan.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuHakAkses.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));

        revalidate();
        repaint();
    }

    private long hitungJenis(List<Item> items, JenisLaporan jenis) {
        return items.stream().filter(item -> item.getJenis() == jenis).count();
    }

    private int hitungTingkatPengembalian(List<Item> items) {
        long totalHilang = hitungJenis(items, JenisLaporan.HILANG);
        if (totalHilang == 0) {
            return 0;
        }
        long kembali = items.stream()
                .filter(item -> item.getStatus() == StatusItem.DIKEMBALIKAN
                        || item.getStatus() == StatusItem.DISERAHKAN)
                .count();
        return (int) Math.min(100, Math.round(kembali * 100.0 / totalHilang));
    }

    private Object[][] dataStatus(List<Item> items) {
        Map<StatusItem, Integer> jumlah = new EnumMap<>(StatusItem.class);
        for (StatusItem status : StatusItem.values()) {
            jumlah.put(status, 0);
        }
        for (Item item : items) {
            jumlah.merge(item.getStatus(), 1, Integer::sum);
        }

        Object[][] data = new Object[jumlah.size()][2];
        int baris = 0;
        for (Map.Entry<StatusItem, Integer> entry : jumlah.entrySet()) {
            data[baris][0] = entry.getKey().getLabel();
            data[baris][1] = entry.getValue();
            baris++;
        }
        return data;
    }

    private Object[][] dataKlaim(List<Klaim> daftarKlaim) {
        Map<StatusKlaim, Integer> jumlah = new EnumMap<>(StatusKlaim.class);
        for (StatusKlaim status : StatusKlaim.values()) {
            if ("MENUNGGU_VERIFIKASI".equals(status.name())) {
                continue; // status lama yang sudah dimigrasikan
            }
            jumlah.put(status, 0);
        }
        for (Klaim klaim : daftarKlaim) {
            jumlah.merge(klaim.getStatus(), 1, Integer::sum);
        }

        Object[][] data = new Object[jumlah.size()][2];
        int baris = 0;
        for (Map.Entry<StatusKlaim, Integer> entry : jumlah.entrySet()) {
            data[baris][0] = entry.getKey().getLabel();
            data[baris][1] = entry.getValue();
            baris++;
        }
        return data;
    }

    private Object[][] dataLaporanTerbaru(List<Item> terbaru) {
        Object[][] data = new Object[terbaru.size()][6];
        for (int i = 0; i < terbaru.size(); i++) {
            Item item = terbaru.get(i);
            data[i][0] = item.getId();
            data[i][1] = item.getJenis() == JenisLaporan.HILANG ? "Hilang" : "Temuan";
            data[i][2] = item.getNama();
            data[i][3] = item.getStatus().getLabel();
            data[i][4] = item.getPelapor() == null ? "-" : item.getPelapor().getNama();
            data[i][5] = item.getPelapor() == null ? "-" : item.getPelapor().getLabelRole();
        }
        return data;
    }

    private RoundedPanel statCard(String judul, String nilai, String keterangan) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 4),
                Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel lblJudul = new JLabel(judul.toUpperCase());
        lblJudul.setFont(Fonts.sans(10.5f, Font.BOLD));
        lblJudul.setForeground(Theme.TEXT_MUTED);

        JLabel lblNilai = new JLabel(nilai);
        lblNilai.setFont(Fonts.serif(30f, Font.BOLD));
        lblNilai.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblKet = new JLabel(keterangan);
        lblKet.setFont(Fonts.sans(11f, Font.PLAIN));
        lblKet.setForeground(Theme.TEXT_SECONDARY);

        card.add(lblJudul, BorderLayout.NORTH);
        card.add(lblNilai, BorderLayout.CENTER);
        card.add(lblKet, BorderLayout.SOUTH);
        return card;
    }

    private RoundedPanel buatTabel(String judul, String keterangan, String[] kolom, Object[][] data) {
        return buatTabel(judul, keterangan, kolom, data, null);
    }

    private RoundedPanel buatTabel(String judul, String keterangan, String[] kolom, Object[][] data,
                                   IntConsumer onRowActivated) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10),
                Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(Fonts.sans(14.5f, Font.BOLD));
        lblJudul.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblKet = new JLabel(keterangan);
        lblKet.setFont(Fonts.sans(11.5f, Font.PLAIN));
        lblKet.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(lblJudul);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblKet);

        JTable tabel = new JTable(new DefaultTableModel(data, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tabel.setFont(Fonts.sans(12f, Font.PLAIN));
        tabel.setRowHeight(26);
        tabel.setForeground(Theme.TEXT_PRIMARY);
        tabel.setBackground(Theme.INPUT_BACKGROUND);
        tabel.setGridColor(Theme.CARD_DIVIDER);
        tabel.setSelectionBackground(new Color(0xE6, 0xDF, 0xD2));
        tabel.setFillsViewportHeight(true);

        if (onRowActivated != null) {
            tabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = tabel.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tabel.setRowSelectionInterval(row, row);
                        onRowActivated.accept(row);
                    }
                }
            });
        }

        JTableHeader header = tabel.getTableHeader();
        header.setFont(Fonts.sans(11.5f, Font.BOLD));
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setBackground(Theme.CARD_BACKGROUND);

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setPreferredSize(new Dimension(100, Math.max(90, Math.min(260, 42 + (data.length * 26)))));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.INPUT_BACKGROUND);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel buatKartuHakAkses(AppSession appSession) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10),
                Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        String namaAdmin = appSession.isLoggedIn() ? appSession.getNama() : "Admin";
        JLabel lblJudul = new JLabel("Hak Akses Role Admin");
        lblJudul.setFont(Fonts.sans(14.5f, Font.BOLD));
        lblJudul.setForeground(Theme.TEXT_PRIMARY);

        StringBuilder isi = new StringBuilder("<html><body style='font-family:sans-serif;font-size:12px;color:#4A433C;'>");
        isi.append("Sesi aktif: <b>").append(escapeHtml(namaAdmin)).append("</b><br><br>");
        for (HakAkses hak : Role.ADMIN.getHakAkses()) {
            isi.append("&bull; ").append(escapeHtml(hak.getLabel())).append("<br>");
        }
        isi.append("</body></html>");

        JLabel lblIsi = new JLabel(isi.toString());

        JPanel panelJudul = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelJudul.setOpaque(false);
        panelJudul.add(lblJudul);

        card.add(panelJudul, BorderLayout.NORTH);
        card.add(lblIsi, BorderLayout.CENTER);
        return card;
    }

    private String escapeHtml(String teks) {
        return teks.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private RoundedPanel buatKartuPengguna() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10),
                Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblJudul = new JLabel("Kelola Pengguna & Akses Petugas");
        lblJudul.setFont(Fonts.sans(14.5f, Font.BOLD));
        lblJudul.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblKet = new JLabel("Petugas pos keamanan/satpam mendapatkan akses khusus langsung dari Administrator");
        lblKet.setFont(Fonts.sans(11.5f, Font.PLAIN));
        lblKet.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(lblJudul);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblKet);

        JButton btnTambahPetugas = new JButton("+ Beri Akses Petugas") {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0x43, 0x38, 0xCA) : new Color(0x4F, 0x46, 0xE5));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnTambahPetugas.setFont(Fonts.sans(12f, Font.BOLD));
        btnTambahPetugas.setForeground(Color.WHITE);
        btnTambahPetugas.setOpaque(false);
        btnTambahPetugas.setContentAreaFilled(false);
        btnTambahPetugas.setBorderPainted(false);
        btnTambahPetugas.setFocusPainted(false);
        btnTambahPetugas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTambahPetugas.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btnTambahPetugas.addActionListener(e -> bukaDialogTambahPetugas());

        topRow.add(headerPanel, BorderLayout.CENTER);
        topRow.add(btnTambahPetugas, BorderLayout.EAST);

        String[] kolom = {"Identitas (NIM/NIP)", "Nama", "Email", "Role", "Status"};
        userTableModel = new DefaultTableModel(dataPengguna(), kolom) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable tabel = new JTable(userTableModel);
        tabel.setFont(Fonts.sans(12f, Font.PLAIN));
        tabel.setRowHeight(26);
        tabel.setForeground(Theme.TEXT_PRIMARY);
        tabel.setBackground(Theme.INPUT_BACKGROUND);
        tabel.setGridColor(Theme.CARD_DIVIDER);
        tabel.setSelectionBackground(new Color(0xE6, 0xDF, 0xD2));
        tabel.setFillsViewportHeight(true);

        JTableHeader th = tabel.getTableHeader();
        th.setFont(Fonts.sans(11.5f, Font.BOLD));
        th.setForeground(Theme.TEXT_SECONDARY);
        th.setBackground(Theme.CARD_BACKGROUND);

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setPreferredSize(new Dimension(100, Math.max(90, Math.min(220, 42 + (userTableModel.getRowCount() * 26)))));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.INPUT_BACKGROUND);

        card.add(topRow, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private Object[][] dataPengguna() {
        if (controller == null || controller.getUserRepository() == null) {
            return new Object[0][5];
        }
        List<User> users = controller.getUserRepository().findAll();
        Object[][] data = new Object[users.size()][5];
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            data[i][0] = u.getNomorIdentitas();
            data[i][1] = u.getNama();
            data[i][2] = u.getEmail();
            data[i][3] = u.getLabelRole();
            data[i][4] = u.isAktif() ? "Aktif" : "Nonaktif";
        }
        return data;
    }

    private void bukaDialogTambahPetugas() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "Beri Akses Akun Petugas Baru", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(440, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Beri Akses Akun Petugas Baru");
        title.setFont(Fonts.sans(16f, Font.BOLD));
        title.setForeground(new Color(0x0F, 0x17, 0x2A));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);

        content.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("<html><body style='width: 340px; color: #64748B; font-size: 11px;'>"
                + "Petugas pos keamanan/satpam yang berwenang memverifikasi barang temuan dan serah terima. "
                + "Petugas mendapatkan akses resmi dari Admin.</body></html>");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sub);

        content.add(Box.createVerticalStrut(16));

        // Form fields
        JLabel lblNama = new JLabel("Nama Lengkap Petugas");
        lblNama.setFont(Fonts.sans(11.5f, Font.BOLD));
        lblNama.setForeground(new Color(0x33, 0x41, 0x55));
        lblNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblNama);
        content.add(Box.createVerticalStrut(4));
        JTextField txtNama = new JTextField();
        txtNama.setFont(Fonts.sans(12.5f, Font.PLAIN));
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(txtNama);

        content.add(Box.createVerticalStrut(10));

        JLabel lblNip = new JLabel("Nomor Induk Pegawai (NIP 18 Digit)");
        lblNip.setFont(Fonts.sans(11.5f, Font.BOLD));
        lblNip.setForeground(new Color(0x33, 0x41, 0x55));
        lblNip.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblNip);
        content.add(Box.createVerticalStrut(4));
        JTextField txtNip = new JTextField();
        txtNip.setFont(Fonts.sans(12.5f, Font.PLAIN));
        txtNip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtNip.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(txtNip);

        content.add(Box.createVerticalStrut(10));

        JLabel lblEmail = new JLabel("Email Dinas Petugas");
        lblEmail.setFont(Fonts.sans(11.5f, Font.BOLD));
        lblEmail.setForeground(new Color(0x33, 0x41, 0x55));
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblEmail);
        content.add(Box.createVerticalStrut(4));
        JTextField txtEmail = new JTextField();
        txtEmail.setFont(Fonts.sans(12.5f, Font.PLAIN));
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(txtEmail);

        content.add(Box.createVerticalStrut(10));

        JLabel lblPass = new JLabel("Kata Sandi Akun (min. 6 karakter)");
        lblPass.setFont(Fonts.sans(11.5f, Font.BOLD));
        lblPass.setForeground(new Color(0x33, 0x41, 0x55));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblPass);
        content.add(Box.createVerticalStrut(4));
        JPasswordField txtPass = new JPasswordField();
        txtPass.setFont(Fonts.sans(12.5f, Font.PLAIN));
        txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(txtPass);

        content.add(Box.createVerticalStrut(18));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnBatal = new JButton("Batal");
        btnBatal.setFont(Fonts.sans(12f, Font.PLAIN));
        btnBatal.addActionListener(e -> dialog.dispose());

        JButton btnSimpan = new JButton("Simpan & Beri Akses");
        btnSimpan.setFont(Fonts.sans(12f, Font.BOLD));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setBackground(new Color(0x4F, 0x46, 0xE5));
        btnSimpan.setOpaque(true);
        btnSimpan.setBorderPainted(false);
        btnSimpan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSimpan.addActionListener(e -> {
            String nama = txtNama.getText().trim();
            String nip = txtNip.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());

            try {
                if (controller != null && controller.getAuthService() != null) {
                    controller.getAuthService().tambahPetugasOlehAdmin(nama, nip, email, pass);
                }
                userTableModel.setDataVector(dataPengguna(), new String[]{"Identitas (NIM/NIP)", "Nama", "Email", "Role", "Status"});
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Akun Petugas " + nama + " (NIP: " + nip + ") berhasil dibuat.\nPetugas sekarang dapat masuk ke sistem.",
                        "Akses Petugas Berhasil Diberikan",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage(),
                        "Gagal Membuat Akun Petugas",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnBatal);
        btnRow.add(btnSimpan);
        content.add(btnRow);

        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
