package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.Klaim;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.StatusKlaim;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog untuk memantau status pengajuan klaim pengguna (US-027).
 * Dilengkapi dengan badge warna status, alasan penolakan, pemuatan data asinkron (SwingWorker),
 * dan pembatalan klaim aktif.
 */
public class KlaimSayaDialog extends JDialog {

    private final KlaimService klaimService;
    private final ItemService itemService;
    private final PosService posService;
    private final AppSession appSession;
    private final NavigationController controller;

    private JTable table;
    private DefaultTableModel tableModel;
    private FlatButton batalkanBtn;
    private FlatButton segarkanBtn;
    private JLabel lblStatusLoading;
    private JLabel lblEmpty;
    private List<KlaimDataRow> currentRows = new ArrayList<>();

    public KlaimSayaDialog(JFrame parent,
                           KlaimService klaimService,
                           ItemService itemService,
                           PosService posService,
                           AppSession appSession,
                           NavigationController controller) {
        super(parent, "Daftar Klaim Saya — CampusFind", true);
        this.klaimService = klaimService;
        this.itemService = itemService;
        this.posService = posService;
        this.appSession = appSession;
        this.controller = controller;

        setSize(960, 520);
        setMinimumSize(new Dimension(800, 400));
        setLocationRelativeTo(parent);
        initView();
        loadDataAsync();
    }

    public KlaimSayaDialog(JFrame parent,
                           KlaimService klaimService,
                           ItemService itemService,
                           PosService posService,
                           AppSession appSession) {
        this(parent, klaimService, itemService, posService, appSession, null);
    }

    public KlaimSayaDialog(JFrame parent,
                           KlaimService klaimService,
                           ItemService itemService,
                           AppSession appSession) {
        this(parent, klaimService, itemService, null, appSession, null);
    }

    private void initView() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(Theme.PAGE_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel title = new JLabel("Riwayat & Status Klaim Saya");
        title.setFont(Fonts.sans(20f, Font.BOLD));
        title.setForeground(Theme.TEXT_PRIMARY);

        String idText = appSession.isLoggedIn() ? appSession.getNomorIdentitas() + " (" + appSession.getNama() + ")" : "-";
        JLabel sub = new JLabel("Pemilik Akun: " + idText);
        sub.setFont(Fonts.sans(13f, Font.PLAIN));
        sub.setForeground(Theme.TEXT_SECONDARY);

        headerLeft.add(title);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(sub);

        segarkanBtn = new FlatButton("SEGARKAN");
        segarkanBtn.setPreferredSize(new Dimension(100, 32));
        segarkanBtn.addActionListener(e -> loadDataAsync());

        header.add(headerLeft, BorderLayout.WEST);
        header.add(segarkanBtn, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        // Center Table Section
        String[] columns = {
                "Kode Klaim",
                "ID Barang",
                "Nama Barang",
                "Tanggal Diajukan",
                "Jadwal Serah Terima",
                "Status",
                "Alasan Penolakan"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(Fonts.sans(12f, Font.PLAIN));
        table.setRowHeight(32);
        table.getTableHeader().setFont(Fonts.sans(12f, Font.BOLD));
        table.getTableHeader().setReorderingAllowed(false);

        // Kolom lebarnya diatur proporsional
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(85);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(210);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(165);

        // Pasang Status Badge Renderer pada kolom Status (indeks 5)
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        table.getSelectionModel().addListSelectionListener(e -> updateBatalkanButtonState());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.CARD_BORDER, 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        lblStatusLoading = new JLabel("Memuat riwayat klaim...", SwingConstants.CENTER);
        lblStatusLoading.setFont(Fonts.sans(13f, Font.ITALIC));
        lblStatusLoading.setForeground(Theme.TEXT_SECONDARY);
        lblStatusLoading.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        centerPanel.add(lblStatusLoading, BorderLayout.NORTH);

        lblEmpty = new JLabel("Anda belum pernah mengajukan klaim barang.", SwingConstants.CENTER);
        lblEmpty.setFont(Fonts.sans(13.5f, Font.PLAIN));
        lblEmpty.setForeground(Theme.TEXT_MUTED);
        lblEmpty.setVisible(false);

        content.add(centerPanel, BorderLayout.CENTER);

        // Footer Buttons
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JLabel infoBatal = new JLabel("*) Hanya klaim berstatus 'Diajukan' yang dapat dibatalkan.");
        infoBatal.setFont(Fonts.sans(11.5f, Font.ITALIC));
        infoBatal.setForeground(Theme.TEXT_MUTED);
        footer.add(infoBatal, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBtns.setOpaque(false);

        batalkanBtn = new FlatButton("BATALKAN KLAIM TERPILIH");
        batalkanBtn.setEnabled(false);
        batalkanBtn.addActionListener(e -> batalkanTerpilih());

        FlatButton tutupBtn = new FlatButton("TUTUP");
        tutupBtn.addActionListener(e -> dispose());

        rightBtns.add(batalkanBtn);
        rightBtns.add(tutupBtn);
        footer.add(rightBtns, BorderLayout.EAST);

        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
    }

    public void loadDataAsync() {
        segarkanBtn.setEnabled(false);
        batalkanBtn.setEnabled(false);
        lblStatusLoading.setText("Memuat data klaim terbaru...");
        lblStatusLoading.setVisible(true);

        SwingWorker<List<KlaimDataRow>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<KlaimDataRow> doInBackground() {
                String nomorIdentitas = appSession.getNomorIdentitas();
                if (nomorIdentitas == null || nomorIdentitas.isBlank()) {
                    return List.of();
                }

                List<Klaim> list = klaimService.findByPengklaim(nomorIdentitas);
                DateTimeFormatter dtfTgl = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dtfWaktu = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                List<KlaimDataRow> rows = new ArrayList<>();
                for (Klaim k : list) {
                    String namaBarang = itemService.findById(k.getItemId()).map(Item::getNama).orElse("-");
                    String tglDiajukan = (k.getDiajukanPada() != null) ? k.getDiajukanPada().format(dtfWaktu) : "-";

                    String posNama = "-";
                    if (posService != null && k.getPosId() != null) {
                        posNama = posService.findById(k.getPosId()).map(PosPenyimpanan::getNama).orElse(k.getPosId());
                    } else if (k.getPosId() != null) {
                        posNama = k.getPosId();
                    }

                    String sesiLabel = (k.getSesi() != null)
                            ? k.getSesi().getLabel() + " (" + k.getSesi().getMulai() + " - " + k.getSesi().getSelesai() + ")"
                            : "-";
                    String tglAmbil = (k.getTanggalSerahTerima() != null) ? k.getTanggalSerahTerima().format(dtfTgl) : "-";
                    String jadwal = tglAmbil + ", " + sesiLabel + " @" + posNama;

                    String alasan = (k.getStatus() == StatusKlaim.DITOLAK)
                            ? (k.getAlasanPenolakan() != null && !k.getAlasanPenolakan().isBlank() ? k.getAlasanPenolakan() : "-")
                            : "-";

                    rows.add(new KlaimDataRow(k, k.getId(), "#" + k.getItemId(), namaBarang, tglDiajukan, jadwal, k.getStatus(), alasan));
                }
                return rows;
            }

            @Override
            protected void done() {
                segarkanBtn.setEnabled(true);
                lblStatusLoading.setVisible(false);
                try {
                    List<KlaimDataRow> rows = get();
                    currentRows = rows;
                    tableModel.setRowCount(0);

                    for (KlaimDataRow r : rows) {
                        tableModel.addRow(new Object[]{
                                r.klaimId,
                                r.itemKode,
                                r.namaBarang,
                                r.tglDiajukan,
                                r.jadwal,
                                r.status,
                                r.alasan
                        });
                    }

                    if (rows.isEmpty()) {
                        lblStatusLoading.setText("Belum ada riwayat klaim yang diajukan.");
                        lblStatusLoading.setVisible(true);
                    }
                } catch (Exception ex) {
                    tableModel.setRowCount(0);
                    lblStatusLoading.setText("Gagal memuat riwayat klaim: " + ex.getMessage());
                    lblStatusLoading.setVisible(true);
                }
                updateBatalkanButtonState();
            }
        };

        worker.execute();
    }

    @SuppressWarnings("deprecation")
    private void updateBatalkanButtonState() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentRows.size()) {
            batalkanBtn.setEnabled(false);
            return;
        }

        KlaimDataRow row = currentRows.get(selectedRow);
        // Tombol hanya aktif jika status DIAJUKAN (atau MENUNGGU_VERIFIKASI untuk backward compatibility)
        boolean bisaBatal = row.status == StatusKlaim.DIAJUKAN || row.status == StatusKlaim.MENUNGGU_VERIFIKASI;
        batalkanBtn.setEnabled(bisaBatal);
    }

    @SuppressWarnings("deprecation")
    private void batalkanTerpilih() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRows.size()) {
            JOptionPane.showMessageDialog(this, "Pilih baris klaim yang ingin dibatalkan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KlaimDataRow selected = currentRows.get(row);
        Klaim klaim = selected.klaim;

        if (klaim.getStatus() != StatusKlaim.DIAJUKAN && klaim.getStatus() != StatusKlaim.MENUNGGU_VERIFIKASI) {
            JOptionPane.showMessageDialog(this, "Hanya klaim berstatus Diajukan yang dapat dibatalkan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin membatalkan pengajuan klaim " + klaim.getId() + "?\nBarang akan otomatis kembali tersedia untuk umum.",
                "Konfirmasi Pembatalan Klaim",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                klaimService.batalkan(klaim.getId(), appSession.getNomorIdentitas());
                loadDataAsync();
                if (controller != null) {
                    controller.updateNotificationCount();
                }
                JOptionPane.showMessageDialog(this, "Klaim berhasil dibatalkan. Status barang kembali Tersedia.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class KlaimDataRow {
        final Klaim klaim;
        final String klaimId;
        final String itemKode;
        final String namaBarang;
        final String tglDiajukan;
        final String jadwal;
        final StatusKlaim status;
        final String alasan;

        KlaimDataRow(Klaim klaim, String klaimId, String itemKode, String namaBarang,
                     String tglDiajukan, String jadwal, StatusKlaim status, String alasan) {
            this.klaim = klaim;
            this.klaimId = klaimId;
            this.itemKode = itemKode;
            this.namaBarang = namaBarang;
            this.tglDiajukan = tglDiajukan;
            this.jadwal = jadwal;
            this.status = status;
            this.alasan = alasan;
        }
    }

    /**
     * Renderer badge status warna pada tabel:
     * - Diajukan: Kuning
     * - Disetujui: Hijau
     * - Ditolak: Merah
     * - Dibatalkan: Abu-abu
     */
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        @SuppressWarnings("deprecation")
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(Fonts.sans(11.5f, Font.BOLD));
            label.setOpaque(true);

            String statusLabel = "-";
            Color bg = new Color(243, 244, 246);
            Color fg = new Color(75, 85, 99);

            if (value instanceof StatusKlaim status) {
                statusLabel = status.getLabel();
                switch (status) {
                    case DIAJUKAN, MENUNGGU_VERIFIKASI -> {
                        bg = new Color(254, 243, 199); // Kuning / Amber
                        fg = new Color(146, 64, 14);
                    }
                    case DISETUJUI -> {
                        bg = new Color(220, 252, 231); // Hijau
                        fg = new Color(22, 101, 52);
                    }
                    case DITOLAK -> {
                        bg = new Color(254, 226, 226); // Merah
                        fg = new Color(153, 27, 27);
                    }
                    case DIBATALKAN -> {
                        bg = new Color(243, 244, 246); // Abu-abu
                        fg = new Color(75, 85, 99);
                    }
                    default -> {
                        bg = new Color(240, 240, 240);
                        fg = Theme.TEXT_SECONDARY;
                    }
                }
            } else if (value != null) {
                statusLabel = value.toString();
            }

            label.setText("  " + statusLabel + "  ");
            label.setBackground(bg);
            label.setForeground(fg);

            if (isSelected) {
                label.setBorder(BorderFactory.createLineBorder(new Color(180, 140, 100), 1));
            } else {
                label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            }

            return label;
        }
    }
}
