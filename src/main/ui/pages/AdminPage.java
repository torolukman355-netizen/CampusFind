package com.campusfind.ui.pages;

import com.campusfind.model.HakAkses;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Role;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.User;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.StatistikService;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Dashboard khusus role Admin: statistik layanan + grafik batang Graphics2D (US-031),
 * manajemen pengguna lengkap (US-030), ringkasan klaim, dan hak akses role Admin.
 */
public class AdminPage extends ResponsivePage {

    private final NavigationController controller;
    private final StatistikService statistikService = new StatistikService();
    private final PageHeader header;
    private final JPanel statRow;
    private final JPanel chartRow;
    private final RoundedPanel kartuPengguna;
    private final RoundedPanel kartuStatus;
    private final RoundedPanel kartuKlaim;
    private final RoundedPanel kartuLaporan;
    private final RoundedPanel kartuHakAkses;
    private final FooterBar footerBar;
    private DefaultTableModel userTableModel;
    private JTable tabelPengguna;

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
                "Statistik kehilangan & pengembalian, grafik laporan, manajemen pengguna, dan hak akses."
        );
        addContent(header);

        addContentStrut(24);

        long totalHilang = statistikService.hitungPerJenis(items, JenisLaporan.HILANG);
        long totalTemuan = statistikService.hitungPerJenis(items, JenisLaporan.TEMUAN);
        int persenKembali = (int) Math.round(statistikService.hitungTingkatPengembalianPersen(items));
        String rataWaktu = statistikService.formatRataRataWaktuPengembalian(items);

        statRow = new JPanel(new GridLayout(1, 5, 14, 0));
        statRow.setOpaque(false);
        statRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        statRow.add(statCard("Total Laporan", String.valueOf(items.size()), "seluruh laporan masuk"));
        statRow.add(statCard("Barang Hilang", String.valueOf(totalHilang), "laporan kehilangan"));
        statRow.add(statCard("Barang Temuan", String.valueOf(totalTemuan), "barang dititipkan"));
        statRow.add(statCard("Pengembalian", persenKembali + "%", "tingkat kembali"));
        statRow.add(statCard("Rata-rata Waktu", rataWaktu, "laporan ke kembali"));
        addContent(statRow);

        addContentStrut(18);

        // US-031: Grafik Batang Graphics2D per Kategori dan per Bulan
        chartRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartRow.setOpaque(false);
        chartRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        chartRow.add(buatKartuGrafik(
                "Grafik Laporan per Kategori",
                "Distribusi barang hilang & temuan berdasarkan kategori",
                konversiDataKategori(statistikService.hitungPerKategori(items)),
                new Color(0x4F, 0x46, 0xE5)
        ));
        chartRow.add(buatKartuGrafik(
                "Grafik Laporan per Periode (Bulan)",
                "Tren jumlah laporan yang masuk setiap bulan",
                statistikService.hitungPerBulan(items),
                new Color(0x0D, 0x94, 0x88)
        ));
        addContent(chartRow);

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
        chartRow.setMaximumSize(new Dimension(contentWidth, 270));
        kartuPengguna.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuStatus.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuKlaim.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuLaporan.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        kartuHakAkses.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));

        revalidate();
        repaint();
    }

    private Map<String, Long> konversiDataKategori(Map<KategoriBarang, Long> perKategori) {
        Map<String, Long> hasil = new LinkedHashMap<>();
        for (Map.Entry<KategoriBarang, Long> entry : perKategori.entrySet()) {
            hasil.put(entry.getKey().getLabel(), entry.getValue());
        }
        return hasil;
    }

    private RoundedPanel buatKartuGrafik(String judul, String keterangan, Map<String, Long> data, Color barColor) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10),
                Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(Fonts.sans(14f, Font.BOLD));
        lblJudul.setForeground(Theme.TEXT_PRIMARY);

        JLabel lblKet = new JLabel(keterangan);
        lblKet.setFont(Fonts.sans(11.5f, Font.PLAIN));
        lblKet.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(lblJudul);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblKet);

        BarChartPanel chartPanel = new BarChartPanel(data, barColor);
        chartPanel.setPreferredSize(new Dimension(360, 185));

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Komponen grafik batang kustom menggunakan Java Graphics2D (US-031).
     * Otomatis menyesuaikan lebar/tinggi saat jendela diubah ukurannya dan menampilkan
     * pesan kosong bila belum ada data.
     */
    private static class BarChartPanel extends JPanel {
        private final Map<String, Long> data;
        private final Color barColor;

        BarChartPanel(Map<String, Long> data, Color barColor) {
            this.data = data == null ? Map.of() : data;
            this.barColor = barColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            long maxVal = data.values().stream().mapToLong(Long::longValue).max().orElse(0L);
            if (data.isEmpty() || maxVal == 0L) {
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Fonts.sans(12f, Font.ITALIC));
                String emptyMsg = "Belum ada data laporan untuk ditampilkan";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(emptyMsg, (w - fm.stringWidth(emptyMsg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            int padLeft = 34;
            int padRight = 14;
            int padTop = 22;
            int padBottom = 34;

            int plotW = Math.max(40, w - padLeft - padRight);
            int plotH = Math.max(40, h - padTop - padBottom);
            int baseLineY = padTop + plotH;

            // Garis bantu horizontal (grid)
            g2.setColor(Theme.CARD_DIVIDER);
            g2.drawLine(padLeft, baseLineY, padLeft + plotW, baseLineY);
            g2.drawLine(padLeft, padTop + plotH / 2, padLeft + plotW, padTop + plotH / 2);

            g2.setFont(Fonts.sans(10f, Font.PLAIN));
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString("0", 10, baseLineY + 3);
            g2.drawString(String.valueOf(maxVal), 10, padTop + 4);

            int count = data.size();
            int slotW = plotW / count;
            int barW = Math.max(16, Math.min(48, (int) (slotW * 0.52)));

            int idx = 0;
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                long val = entry.getValue();
                int barH = (int) Math.round((val * 1.0 / maxVal) * (plotH - 6));
                int centerX = padLeft + (idx * slotW) + (slotW / 2);
                int barX = centerX - (barW / 2);
                int barY = baseLineY - barH;

                g2.setColor(barColor);
                g2.fillRoundRect(barX, barY, barW, Math.max(2, barH), 8, 8);

                // Nilai di atas batang
                String valStr = String.valueOf(val);
                g2.setFont(Fonts.sans(11f, Font.BOLD));
                g2.setColor(Theme.TEXT_PRIMARY);
                FontMetrics fmVal = g2.getFontMetrics();
                g2.drawString(valStr, centerX - (fmVal.stringWidth(valStr) / 2), barY - 5);

                // Label sumbu X
                String label = entry.getKey();
                g2.setFont(Fonts.sans(10.5f, Font.PLAIN));
                g2.setColor(Theme.TEXT_SECONDARY);
                FontMetrics fmLbl = g2.getFontMetrics();
                int maxLabelW = Math.max(24, slotW - 6);
                while (fmLbl.stringWidth(label) > maxLabelW && label.length() > 3) {
                    label = label.substring(0, label.length() - 2) + ".";
                }
                g2.drawString(label, centerX - (fmLbl.stringWidth(label) / 2), baseLineY + 17);

                idx++;
            }

            g2.dispose();
        }
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
                continue;
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
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel lblJudul = new JLabel(judul.toUpperCase(Locale.ROOT));
        lblJudul.setFont(Fonts.sans(10.5f, Font.BOLD));
        lblJudul.setForeground(Theme.TEXT_MUTED);

        JLabel lblNilai = new JLabel(nilai);
        lblNilai.setFont(Fonts.sans(26f, Font.BOLD));
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
        tabel.setRowHeight(34);
        tabel.setForeground(Theme.TEXT_PRIMARY);
        tabel.setBackground(Theme.INPUT_BACKGROUND);
        tabel.setGridColor(Theme.CARD_DIVIDER);
        tabel.setSelectionBackground(new Color(0xEE, 0xF2, 0xFF));
        tabel.setSelectionForeground(new Color(0x1E, 0x1B, 0x4B));
        tabel.setShowVerticalLines(false);
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

        JLabel lblKet = new JLabel("Tambah petugas baru, ubah role, atau aktifkan/nonaktifkan akun pengguna (US-030)");
        lblKet.setFont(Fonts.sans(11.5f, Font.PLAIN));
        lblKet.setForeground(Theme.TEXT_MUTED);

        headerPanel.add(lblJudul);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblKet);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionGroup.setOpaque(false);

        JButton btnUbahRole = buatTombolSekunder("Ubah Role");
        btnUbahRole.addActionListener(e -> aksiUbahRolePengguna());

        JButton btnToggleAktif = buatTombolSekunder("Aktifkan / Nonaktifkan");
        btnToggleAktif.addActionListener(e -> aksiToggleAktifPengguna());

        JButton btnTambahPetugas = new JButton("+ Beri Akses Petugas") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        actionGroup.add(btnUbahRole);
        actionGroup.add(btnToggleAktif);
        actionGroup.add(btnTambahPetugas);

        topRow.add(headerPanel, BorderLayout.CENTER);
        topRow.add(actionGroup, BorderLayout.EAST);

        String[] kolom = {"Identitas (NIM/NIP)", "Nama", "Email", "Role", "Status"};
        userTableModel = new DefaultTableModel(dataPengguna(), kolom) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabelPengguna = new JTable(userTableModel);
        tabelPengguna.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelPengguna.setFont(Fonts.sans(12f, Font.PLAIN));
        tabelPengguna.setRowHeight(34);
        tabelPengguna.setForeground(Theme.TEXT_PRIMARY);
        tabelPengguna.setBackground(Theme.INPUT_BACKGROUND);
        tabelPengguna.setGridColor(Theme.CARD_DIVIDER);
        tabelPengguna.setSelectionBackground(new Color(0xEE, 0xF2, 0xFF));
        tabelPengguna.setSelectionForeground(new Color(0x1E, 0x1B, 0x4B));
        tabelPengguna.setShowVerticalLines(false);
        tabelPengguna.setFillsViewportHeight(true);

        JTableHeader th = tabelPengguna.getTableHeader();
        th.setFont(Fonts.sans(11.5f, Font.BOLD));
        th.setForeground(Theme.TEXT_SECONDARY);
        th.setBackground(Theme.CARD_BACKGROUND);

        JScrollPane scroll = new JScrollPane(tabelPengguna);
        scroll.setPreferredSize(new Dimension(100, Math.max(90, Math.min(220, 42 + (userTableModel.getRowCount() * 26)))));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.INPUT_BACKGROUND);

        card.add(topRow, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JButton buatTombolSekunder(String teks) {
        JButton btn = new JButton(teks);
        btn.setFont(Fonts.sans(11.5f, Font.BOLD));
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBackground(Theme.INPUT_BACKGROUND);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return btn;
    }

    private void refreshTabelPengguna() {
        userTableModel.setDataVector(dataPengguna(), new String[]{"Identitas (NIM/NIP)", "Nama", "Email", "Role", "Status"});
    }

    private void aksiToggleAktifPengguna() {
        int row = tabelPengguna.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu pengguna pada tabel terlebih dahulu.",
                    "Pilih Pengguna", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = String.valueOf(userTableModel.getValueAt(row, 0));
        String nama = String.valueOf(userTableModel.getValueAt(row, 1));
        String statusSaatIni = String.valueOf(userTableModel.getValueAt(row, 4));
        boolean targetAktif = !"Aktif".equalsIgnoreCase(statusSaatIni);
        String aksiLabel = targetAktif ? "mengaktifkan" : "menonaktifkan";

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Yakin ingin " + aksiLabel + " akun " + nama + " (" + id + ")?",
                "Konfirmasi Status Akun",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (controller != null && controller.getAuthService() != null) {
                controller.getAuthService().ubahStatusAktifUser(id, targetAktif);
            }
            refreshTabelPengguna();
            JOptionPane.showMessageDialog(this,
                    "Status akun " + nama + " berhasil diperbarui menjadi " + (targetAktif ? "Aktif" : "Nonaktif") + ".",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Gagal Mengubah Status Akun", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiUbahRolePengguna() {
        int row = tabelPengguna.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu pengguna pada tabel terlebih dahulu.",
                    "Pilih Pengguna", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = String.valueOf(userTableModel.getValueAt(row, 0));
        String nama = String.valueOf(userTableModel.getValueAt(row, 1));

        Role[] pilihan = Role.values();
        Role roleDipilih = (Role) JOptionPane.showInputDialog(
                this,
                "Pilih role baru untuk " + nama + " (" + id + "):",
                "Ubah Role Pengguna",
                JOptionPane.QUESTION_MESSAGE,
                null,
                pilihan,
                pilihan[0]
        );
        if (roleDipilih == null) {
            return;
        }

        try {
            if (controller != null && controller.getAuthService() != null) {
                controller.getAuthService().ubahRoleUser(id, roleDipilih);
            }
            refreshTabelPengguna();
            JOptionPane.showMessageDialog(this,
                    "Role " + nama + " berhasil diubah menjadi " + roleDipilih.getLabel() + ".",
                    "Role Diperbarui",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Gagal Mengubah Role", JOptionPane.ERROR_MESSAGE);
        }
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
                refreshTabelPengguna();
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