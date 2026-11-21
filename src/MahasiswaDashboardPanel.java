import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MahasiswaDashboardPanel extends JPanel {

    // Palet Warna Modern
    private static final Color BG_MAIN = new Color(244, 246, 249);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_DARK = new Color(30, 64, 175);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_LIGHT = new Color(226, 232, 240);

    private final String namaMahasiswaAktif;
    private final List<LaporanMahasiswaItem> semuaLaporan = new ArrayList<>();
    private final List<LaporanMahasiswaItem> laporanTampil = new ArrayList<>();

    // Komponen KPI
    private final JLabel lblTotalLaporan = new JLabel("0");
    private final JLabel lblDiproses = new JLabel("0");
    private final JLabel lblCocok = new JLabel("0");
    private final JLabel lblSelesai = new JLabel("0");

    // Komponen Filter & Tabel
    private JTextField tfCari;
    private JComboBox<String> cbFilterStatus;
    private DefaultTableModel tableModel;
    private JTable tableLaporan;

    // Komponen Panel Detail Kanan
    private JPanel panelStepper;
    private JEditorPane epDetailBarang;

    public MahasiswaDashboardPanel(String namaMahasiswaAktif) {
        this.namaMahasiswaAktif = namaMahasiswaAktif;

        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(18, 20, 18, 20));
        setBackground(BG_MAIN);

        initDummyData();
        initHeaderAndKPI();
        initCenterSection();
        initRightDetailPanel();

        refreshData();
    }

    private void initHeaderAndKPI() {
        JPanel topWrapper = new JPanel(new BorderLayout(0, 14));
        topWrapper.setOpaque(false);

        // Header Bar
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 3));
        titleGroup.setOpaque(false);

        JLabel lblTitle = new JLabel("Halo, " + namaMahasiswaAktif + "!");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Pantau status laporan barang hilang & temuan milikmu secara real-time");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_MUTED);

        titleGroup.add(lblTitle);
        titleGroup.add(lblSubtitle);

        JButton btnBuatLaporan = new JButton("+ Buat Laporan Baru");
        btnBuatLaporan.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnBuatLaporan.setBackground(PRIMARY);
        btnBuatLaporan.setForeground(Color.WHITE);
        btnBuatLaporan.setFocusPainted(false);
        btnBuatLaporan.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnBuatLaporan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuatLaporan.addActionListener(e -> bukaFormPelaporan());

        headerBar.add(titleGroup, BorderLayout.WEST);
        headerBar.add(btnBuatLaporan, BorderLayout.EAST);

        // 4 Kartu KPI
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.add(buatKartuKPI("Total Laporan Saya", lblTotalLaporan, new Color(37, 99, 235)));
        kpiGrid.add(buatKartuKPI("Dilaporkan / Diverifikasi", lblDiproses, new Color(217, 119, 6)));
        kpiGrid.add(buatKartuKPI("Kandidat Cocok / Diklaim", lblCocok, new Color(124, 58, 237)));
        kpiGrid.add(buatKartuKPI("Sudah Dikembalikan", lblSelesai, new Color(22, 163, 74)));

        topWrapper.add(headerBar, BorderLayout.NORTH);
        topWrapper.add(kpiGrid, BorderLayout.CENTER);
        add(topWrapper, BorderLayout.NORTH);
    }

    private JPanel buatKartuKPI(String judul, JLabel angkaLabel, Color aksen) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new CompoundBorder(
                        new MatteBorder(0, 4, 0, 0, aksen),
                        new EmptyBorder(12, 14, 12, 14))));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblJudul.setForeground(TEXT_MUTED);

        angkaLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        angkaLabel.setForeground(TEXT_DARK);

        card.add(lblJudul, BorderLayout.NORTH);
        card.add(angkaLabel, BorderLayout.CENTER);
        return card;
    }

    private void initCenterSection() {
        JPanel cardTable = new JPanel(new BorderLayout(0, 12));
        cardTable.setBackground(BG_CARD);
        cardTable.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        // Toolbar Pencarian & Filter
        JPanel filterBar = new JPanel(new BorderLayout(10, 0));
        filterBar.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Daftar Laporan Saya");
        lblTableTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTableTitle.setForeground(TEXT_DARK);

        JPanel rightFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightFilter.setOpaque(false);

        tfCari = new JTextField(16);
        tfCari.setToolTipText("Cari nama barang atau lokasi...");
        tfCari.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        tfCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshData();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshData();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshData();
            }
        });

        cbFilterStatus = new JComboBox<>(new String[] {
                "Semua Status", "DILAPORKAN", "DIVERIFIKASI", "COCOK", "DIKLAIM", "DIKEMBALIKAN"
        });
        cbFilterStatus.setBackground(Color.WHITE);
        cbFilterStatus.addActionListener(e -> refreshData());

        rightFilter.add(new JLabel("Cari:"));
        rightFilter.add(tfCari);
        rightFilter.add(new JLabel("Status:"));
        rightFilter.add(cbFilterStatus);

        filterBar.add(lblTableTitle, BorderLayout.WEST);
        filterBar.add(rightFilter, BorderLayout.EAST);

        // Tabel Laporan
        String[] kolom = { "ID", "Jenis", "Nama Barang", "Kategori", "Lokasi", "Waktu", "Status" };
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLaporan = new JTable(tableModel);
        tableLaporan.setRowHeight(34);
        tableLaporan.setShowVerticalLines(false);
        tableLaporan.setGridColor(new Color(241, 245, 249));
        tableLaporan.setSelectionBackground(new Color(239, 246, 255));
        tableLaporan.setSelectionForeground(TEXT_DARK);
        tableLaporan.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tableLaporan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = tableLaporan.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 34));

        // Renderer Badge Status Berwarna
        tableLaporan.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
        tableLaporan.getColumnModel().getColumn(0).setPreferredWidth(65);
        tableLaporan.getColumnModel().getColumn(1).setPreferredWidth(70);
        tableLaporan.getColumnModel().getColumn(2).setPreferredWidth(150);

        tableLaporan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableLaporan.getSelectedRow();
                if (row >= 0 && row < laporanTampil.size()) {
                    tampilkanDetailModern(laporanTampil.get(row));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableLaporan);
        scrollPane.setBorder(new LineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        cardTable.add(filterBar, BorderLayout.NORTH);
        cardTable.add(scrollPane, BorderLayout.CENTER);
        add(cardTable, BorderLayout.CENTER);
    }

    private void initRightDetailPanel() {
        JPanel rightCard = new JPanel(new BorderLayout(0, 12));
        rightCard.setPreferredSize(new Dimension(370, 0));
        rightCard.setBackground(BG_CARD);
        rightCard.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel lblTitle = new JLabel("Rincian & Pelacakan Status");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_DARK);

        // Stepper Alur Status
        panelStepper = new JPanel(new GridLayout(1, 5, 4, 0));
        panelStepper.setOpaque(false);
        updateStepper("");

        JPanel topDetail = new JPanel(new BorderLayout(0, 10));
        topDetail.setOpaque(false);
        topDetail.add(lblTitle, BorderLayout.NORTH);
        topDetail.add(panelStepper, BorderLayout.CENTER);

        // Area Detail HTML Rapi
        epDetailBarang = new JEditorPane();
        epDetailBarang.setContentType("text/html");
        epDetailBarang.setEditable(false);
        epDetailBarang.setBackground(new Color(248, 250, 252));
        epDetailBarang.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollDetail = new JScrollPane(epDetailBarang);
        scrollDetail.setBorder(new LineBorder(BORDER_LIGHT, 1));

        rightCard.add(topDetail, BorderLayout.NORTH);
        rightCard.add(scrollDetail, BorderLayout.CENTER);

        add(rightCard, BorderLayout.EAST);
        resetDetailPanel();
    }

    private void updateStepper(String statusAktif) {
        panelStepper.removeAll();
        String[] tahapan = { "DILAPORKAN", "DIVERIFIKASI", "COCOK", "DIKLAIM", "DIKEMBALIKAN" };
        String[] labelPendek = { "1. Lapor", "2. Verif", "3. Cocok", "4. Klaim", "5. Selesai" };

        int indeksAktif = -1;
        for (int i = 0; i < tahapan.length; i++) {
            if (tahapan[i].equalsIgnoreCase(statusAktif)) {
                indeksAktif = i;
                break;
            }
        }

        for (int i = 0; i < tahapan.length; i++) {
            JLabel step = new JLabel(labelPendek[i], SwingConstants.CENTER);
            step.setFont(new Font("SansSerif", Font.BOLD, 10));
            step.setOpaque(true);
            step.setBorder(new EmptyBorder(6, 2, 6, 2));

            if (i <= indeksAktif && indeksAktif != -1) {
                step.setBackground(PRIMARY);
                step.setForeground(Color.WHITE);
            } else {
                step.setBackground(new Color(226, 232, 240));
                step.setForeground(TEXT_MUTED);
            }
            panelStepper.add(step);
        }

        panelStepper.revalidate();
        panelStepper.repaint();
    }

    private void bukaFormPelaporan() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        FormPelaporanMahasiswa form = new FormPelaporanMahasiswa(parentFrame, namaMahasiswaAktif, itemBaru -> {
            semuaLaporan.add(itemBaru);
            refreshData();
        });
        form.setVisible(true);
    }

    public void refreshData() {
        laporanTampil.clear();
        tableModel.setRowCount(0);

        String keyword = tfCari != null ? tfCari.getText().trim().toLowerCase() : "";
        String filterStatus = cbFilterStatus != null ? (String) cbFilterStatus.getSelectedItem() : "Semua Status";

        int total = 0, diproses = 0, cocok = 0, selesai = 0;

        for (LaporanMahasiswaItem item : semuaLaporan) {
            // Filter privasi: hanya milik mahasiswa yang login
            if (!namaMahasiswaAktif.equalsIgnoreCase(item.getNamaMahasiswa())) {
                continue;
            }

            total++;
            String st = item.getStatus().toUpperCase();
            if ("DILAPORKAN".equals(st) || "DIVERIFIKASI".equals(st))
                diproses++;
            else if ("COCOK".equals(st) || "DIKLAIM".equals(st))
                cocok++;
            else if ("DIKEMBALIKAN".equals(st))
                selesai++;

            boolean matchStatus = "Semua Status".equals(filterStatus) || st.equals(filterStatus);
            boolean matchKeyword = keyword.isEmpty()
                    || item.getNamaBarang().toLowerCase().contains(keyword)
                    || item.getLokasiKejadian().toLowerCase().contains(keyword)
                    || item.getIdLaporan().toLowerCase().contains(keyword);

            if (matchStatus && matchKeyword) {
                laporanTampil.add(item);
                tableModel.addRow(new Object[] {
                        item.getIdLaporan(),
                        item.getJenisLaporan(),
                        item.getNamaBarang(),
                        item.getKategori(),
                        item.getLokasiKejadian(),
                        item.getWaktuKejadianFormatted(),
                        item.getStatus()
                });
            }
        }

        lblTotalLaporan.setText(String.valueOf(total));
        lblDiproses.setText(String.valueOf(diproses));
        lblCocok.setText(String.valueOf(cocok));
        lblSelesai.setText(String.valueOf(selesai));

        resetDetailPanel();
    }

    private void tampilkanDetailModern(LaporanMahasiswaItem item) {
        updateStepper(item.getStatus());

        String atributKhusus;
        if ("Elektronik".equalsIgnoreCase(item.getKategori())) {
            atributKhusus = "<tr><td style='color:#64748b;'>Merk</td><td><b>" + item.getMerk() + "</b></td></tr>"
                    + "<tr><td style='color:#64748b;'>Nomor Seri</td><td><b>" + item.getNomorSeri() + "</b></td></tr>";
        } else {
            atributKhusus = "<tr><td style='color:#64748b;'>Bahan / Jenis</td><td><b>" + item.getBahanJenis()
                    + "</b></td></tr>"
                    + "<tr><td style='color:#64748b;'>Ciri Fisik</td><td><b>" + item.getCiriFisik() + "</b></td></tr>";
        }

        String fotoInfo = (item.getPathFoto() == null || item.getPathFoto().isBlank())
                ? "<span style='color:#94a3b8;'>Tidak ada lampiran foto</span>"
                : "<span style='color:#2563eb;'>" + item.getPathFoto() + "</span>";

        String html = "<html><body style='font-family:SansSerif; font-size:12px; color:#1e293b; margin:4px;'>"
                + "<div style='background:#ffffff; padding:10px; border:1px solid #e2e8f0; border-radius:6px;'>"
                + "<div style='font-size:14px; font-weight:bold; color:#0f172a; margin-bottom:4px;'>"
                + item.getNamaBarang() + "</div>"
                + "<div style='font-size:11px; color:#64748b; margin-bottom:8px;'>ID: " + item.getIdLaporan()
                + " &bull; Jenis: <b>" + item.getJenisLaporan() + "</b></div>"
                + "<table width='100%' cellpadding='4' cellspacing='0' style='font-size:11.5px;'>"
                + "<tr><td width='38%' style='color:#64748b;'>Kategori</td><td>" + item.getKategori() + "</td></tr>"
                + "<tr><td style='color:#64748b;'>Warna</td><td>" + item.getWarna() + "</td></tr>"
                + "<tr><td style='color:#64748b;'>Lokasi</td><td>" + item.getLokasiKejadian() + "</td></tr>"
                + "<tr><td style='color:#64748b;'>Ket. Lokasi</td><td>" + item.getKeteranganLokasi() + "</td></tr>"
                + "<tr><td style='color:#64748b;'>Waktu</td><td>" + item.getWaktuKejadianFormatted() + "</td></tr>"
                + atributKhusus
                + "</table>"
                + "<hr style='border:0; border-top:1px solid #e2e8f0; margin:8px 0;'>"
                + "<div style='color:#64748b; font-size:11px;'>Deskripsi:</div>"
                + "<div style='margin-top:2px; margin-bottom:8px;'>" + item.getDeskripsi() + "</div>"
                + "<div style='color:#64748b; font-size:11px;'>Lampiran Foto:</div>"
                + "<div style='margin-top:2px;'>" + fotoInfo + "</div>"
                + "</div></body></html>";

        epDetailBarang.setText(html);
        epDetailBarang.setCaretPosition(0);
    }

    private void resetDetailPanel() {
        updateStepper("");
        String html = "<html><body style='font-family:SansSerif; color:#64748b; text-align:center; padding-top:60px;'>"
                + "<p style='font-size:13px;'><b>Belum ada laporan dipilih</b></p>"
                + "<p style='font-size:11px;'>Klik salah satu baris pada tabel di sebelah kiri untuk melihat detail barang dan progres statusnya.</p>"
                + "</body></html>";
        epDetailBarang.setText(html);
    }

    // Renderer untuk membuat badge warna pada kolom Status
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString().toUpperCase() : "";

            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setOpaque(true);

            switch (status) {
                case "DILAPORKAN":
                    lbl.setBackground(new Color(254, 243, 199));
                    lbl.setForeground(new Color(180, 83, 9));
                    break;
                case "DIVERIFIKASI":
                    lbl.setBackground(new Color(219, 234, 254));
                    lbl.setForeground(new Color(29, 78, 216));
                    break;
                case "COCOK":
                    lbl.setBackground(new Color(243, 232, 255));
                    lbl.setForeground(new Color(126, 34, 206));
                    break;
                case "DIKLAIM":
                    lbl.setBackground(new Color(255, 237, 213));
                    lbl.setForeground(new Color(194, 65, 12));
                    break;
                case "DIKEMBALIKAN":
                    lbl.setBackground(new Color(220, 252, 231));
                    lbl.setForeground(new Color(21, 128, 61));
                    break;
                default:
                    lbl.setBackground(new Color(241, 245, 249));
                    lbl.setForeground(TEXT_DARK);
            }
            return lbl;
        }
    }

    private void initDummyData() {
        semuaLaporan.add(new LaporanMahasiswaItem(
                "H-2001", "Nadia", "Hilang", "Laptop ASUS Vivobook", "Elektronik",
                "Silver", "Gedung TI Lt. 2", "Meja dekat jendela ruang 203",
                LocalDateTime.now().minusDays(1),
                "Tertinggal di dalam tas abu-abu setelah praktikum.", "",
                "ASUS", "SN-AS8821", "", "", "DIVERIFIKASI"));

        semuaLaporan.add(new LaporanMahasiswaItem(
                "T-2002", "Nadia", "Temuan", "Botol Minum Corkcicle", "Non-Elektronik",
                "Hitam", "Perpustakaan Pusat", "Meja baca lantai 1",
                LocalDateTime.now().minusHours(5),
                "Ditemukan tergeletak di dekat colokan listrik.", "",
                "", "", "Stainless steel", "Ada stiker kecil di bagian bawah", "DILAPORKAN"));

        semuaLaporan.add(new LaporanMahasiswaItem(
                "H-2003", "Nadia", "Hilang", "Flashdisk Sandisk 32GB", "Elektronik",
                "Merah Hitam", "Lab Komputer 1", "PC nomor 12",
                LocalDateTime.now().minusDays(2),
                "Gantungan tali berwarna biru.", "",
                "Sandisk", "SN-FD9921", "", "", "COCOK"));

        // Data milik mahasiswa lain (tersembunyi karena filter privasi)
        semuaLaporan.add(new LaporanMahasiswaItem(
                "H-2099", "MahasiswaLain", "Hilang", "Dompet Kulit", "Non-Elektronik",
                "Coklat", "Kantin", "Meja tengah",
                LocalDateTime.now().minusHours(3),
                "Milik mahasiswa lain.", "",
                "", "", "Kulit sintetis", "Resleting gold", "DILAPORKAN"));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CampusFind — Dashboard Mahasiswa");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 640);
            frame.setLocationRelativeTo(null);
            frame.add(new MahasiswaDashboardPanel("Nadia"));
            frame.setVisible(true);
        });
    }
}