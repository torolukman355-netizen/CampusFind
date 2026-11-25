package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.JenisBukti;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.User;
import com.campusfind.model.UserAccount;
import com.campusfind.model.UserAccount.RoleType;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.service.AdminService;
import com.campusfind.service.PetugasService;
import com.campusfind.ui.components.BarChartPanel;
import com.campusfind.ui.components.ModernSwingKit;
import com.campusfind.ui.theme.Fonts;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Antarmuka lengkap Role Admin (Lukman):
 * - US-010: Dashboard Ringkasan Statistik (via Admin.tampilDashboard())
 * - US-022: Manajemen Akun Pengguna (Tambah, Ubah Role, Aktif/Nonaktif, Proteksi Diri)
 * - US-023 & US-031: Grafik Batang Rekap Kategori/Bulan/Jenis, Return Rate, & Rata-rata Waktu Penyelesaian
 * - US-003, US-007, US-008: Kelola Semua Barang (ItemRepository.findAll() & item.tampilkanDetail())
 * - v2.0: Pengarsipan Barang Lama (> 60 Hari), Master Data Pos & Audit Log
 */
public class AdminDesktopPage extends JPanel {

    private final AdminService adminService;
    private final ItemRepository itemRepository;
    private final PosRepository posRepository;
    private final PetugasService petugasService;
    private User activeAdmin;
    private Runnable onDataChanged;

    private final JLabel lblKpiTotal = new JLabel("0");
    private final JLabel lblKpiReturnRate = new JLabel("0%");
    private final JLabel lblKpiAvgJam = new JLabel("0 jam");
    private final JLabel lblKpiArsip = new JLabel("0");

    private BarChartPanel chartKategori, chartJenis, chartBulan;
    private DefaultTableModel modelUsers, modelItems, modelPos, modelAudit;
    private JTable tblUsers, tblItems;
    private JTextArea txtItemDetail;

    public AdminDesktopPage(AdminService adminService,
                            ItemRepository itemRepository,
                            PosRepository posRepository,
                            PetugasService petugasService,
                            User activeAdmin,
                            Runnable onDataChanged) {
        this.adminService = adminService;
        this.itemRepository = itemRepository;
        this.posRepository = posRepository;
        this.petugasService = petugasService;
        this.activeAdmin = activeAdmin;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(14, 14));
        setBackground(ModernSwingKit.PAGE_BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        initKpiRow();
        initTabs();
        refreshData();
    }

    public void setActiveAdmin(User admin) {
        this.activeAdmin = admin;
        refreshData();
    }

    private void initKpiRow() {
        JPanel kpi = new JPanel(new GridLayout(1, 4, 12, 0));
        kpi.setOpaque(false);
        kpi.add(ModernSwingKit.createKpiCard("TOTAL LAPORAN SISTEM", lblKpiTotal, "Seluruh laporan Hilang & Temuan", ModernSwingKit.BLUE_600));
        kpi.add(ModernSwingKit.createKpiCard("RETURN RATE (KEBERHASILAN)", lblKpiReturnRate, "Dikembalikan / Hilang terverifikasi", ModernSwingKit.EMERALD_600));
        kpi.add(ModernSwingKit.createKpiCard("RATA-RATA PENYELESAIAN", lblKpiAvgJam, "Durasi lapor hingga dikembalikan", ModernSwingKit.PURPLE_600));
        kpi.add(ModernSwingKit.createKpiCard("BARANG DIARSIPKAN (> 60 HR)", lblKpiArsip, "Mitigasi penumpukan pos & DB", ModernSwingKit.AMBER_600));
        add(kpi, BorderLayout.NORTH);
    }

    private void initTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(Color.WHITE);
        tabs.setFont(Fonts.sans(12.5f, Font.BOLD));

        // Tab 1: Grafik Statistik
        JPanel chartGrid = new JPanel(new GridLayout(1, 3, 14, 14));
        chartGrid.setOpaque(false);
        chartGrid.setBorder(new EmptyBorder(12, 6, 12, 6));
        chartKategori = new BarChartPanel("Rekap Laporan per Kategori");
        chartJenis = new BarChartPanel("Perbandingan Hilang vs Temuan");
        chartBulan = new BarChartPanel("Rekap Laporan per Periode Bulan");
        chartGrid.add(chartKategori);
        chartGrid.add(chartJenis);
        chartGrid.add(chartBulan);
        tabs.addTab("1. Visualisasi Statistik", chartGrid);

        // Tab 2: Manajemen Akun Pengguna (US-022)
        JPanel userPanel = ModernSwingKit.createRoundedCard(12);
        modelUsers = ModernSwingKit.createReadOnlyModel(new String[]{"ID", "Nama Lengkap", "NIM / NIP", "Email", "Role", "Status"});
        tblUsers = ModernSwingKit.createModernTable(modelUsers);

        JButton btnAddUser = ModernSwingKit.createButton("+ Tambah Akun", ModernSwingKit.BLUE_600, Color.WHITE);
        btnAddUser.addActionListener(e -> aksiTambahUser());
        JButton btnUbahRole = ModernSwingKit.createButton("Ubah Role", ModernSwingKit.PURPLE_600, Color.WHITE);
        btnUbahRole.addActionListener(e -> aksiUbahRole());
        JButton btnToggleUser = ModernSwingKit.createButton("Aktif / Nonaktifkan", ModernSwingKit.ROSE_600, Color.WHITE);
        btnToggleUser.addActionListener(e -> aksiToggleUser());

        JPanel userBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        userBot.setOpaque(false);
        userBot.add(btnAddUser);
        userBot.add(btnUbahRole);
        userBot.add(btnToggleUser);
        userPanel.add(ModernSwingKit.wrapScroll(tblUsers), BorderLayout.CENTER);
        userPanel.add(userBot, BorderLayout.SOUTH);
        tabs.addTab("2. Manajemen Pengguna", userPanel);

        // Tab 3: Semua Barang (Polimorfisme US-007 & US-008) & Pengarsipan
        JPanel itemSplit = new JPanel(new BorderLayout(12, 12));
        itemSplit.setOpaque(false);

        JPanel itemsCard = ModernSwingKit.createRoundedCard(12);
        modelItems = ModernSwingKit.createReadOnlyModel(new String[]{"ID", "Jenis", "Nama Barang", "Kategori", "Pelapor", "Status"});
        tblItems = ModernSwingKit.createModernTable(modelItems);
        tblItems.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblItems.getSelectedRow() != -1) {
                String id = (String) modelItems.getValueAt(tblItems.getSelectedRow(), 0);
                itemRepository.findById(id).ifPresent(item -> {
                    String syarat = Arrays.stream(item.getSyaratBuktiUtama()).map(JenisBukti::getLabel).collect(Collectors.joining(", "));
                    txtItemDetail.setText(item.tampilkanDetail() + "\n\nSyarat Bukti v2.0: " + syarat);
                });
            }
        });

        JButton btnArsip = ModernSwingKit.createButton("Arsipkan Barang Lama (> 60 Hari)", ModernSwingKit.AMBER_600, Color.WHITE);
        btnArsip.addActionListener(e -> aksiArsipkanLama());
        JPanel itemBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        itemBot.setOpaque(false);
        itemBot.add(btnArsip);
        itemsCard.add(ModernSwingKit.wrapScroll(tblItems), BorderLayout.CENTER);
        itemsCard.add(itemBot, BorderLayout.SOUTH);

        JPanel detailCard = ModernSwingKit.createRoundedCard(14);
        detailCard.setPreferredSize(new Dimension(380, 0));
        txtItemDetail = new JTextArea("Klik baris barang untuk memanggil method polimorfik item.tampilkanDetail().");
        txtItemDetail.setEditable(false);
        txtItemDetail.setLineWrap(true);
        txtItemDetail.setWrapStyleWord(true);
        txtItemDetail.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtItemDetail.setBackground(ModernSwingKit.SLATE_50);
        detailCard.add(new JLabel("Inspector Polimorfisme Barang (US-008)"), BorderLayout.NORTH);
        detailCard.add(ModernSwingKit.wrapScroll(txtItemDetail), BorderLayout.CENTER);

        itemSplit.add(itemsCard, BorderLayout.CENTER);
        itemSplit.add(detailCard, BorderLayout.EAST);
        tabs.addTab("3. Semua Barang & Arsip", itemSplit);

        // Tab 4: Master Data Pos & Audit Log
        JPanel masterSplit = new JPanel(new GridLayout(1, 2, 12, 0));
        masterSplit.setOpaque(false);

        JPanel posCard = ModernSwingKit.createRoundedCard(12);
        modelPos = ModernSwingKit.createReadOnlyModel(new String[]{"ID Pos", "Nama Pos", "Lokasi Penyerahan", "Label Pendek"});
        JTable tblPos = ModernSwingKit.createModernTable(modelPos);
        JButton btnAddPos = ModernSwingKit.createButton("+ Tambah Pos Baru", ModernSwingKit.BLUE_600, Color.WHITE);
        btnAddPos.addActionListener(e -> aksiTambahPos());
        JPanel posBot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        posBot.setOpaque(false);
        posBot.add(btnAddPos);
        posCard.add(new JLabel("Master Data Pos Penyimpanan"), BorderLayout.NORTH);
        posCard.add(ModernSwingKit.wrapScroll(tblPos), BorderLayout.CENTER);
        posCard.add(posBot, BorderLayout.SOUTH);

        JPanel auditCard = ModernSwingKit.createRoundedCard(12);
        modelAudit = ModernSwingKit.createReadOnlyModel(new String[]{"Catatan Audit Aktivitas Sistem"});
        JTable tblAudit = ModernSwingKit.createModernTable(modelAudit);
        auditCard.add(new JLabel("Audit Log Sistem (US-012 & v2.0)"), BorderLayout.NORTH);
        auditCard.add(ModernSwingKit.wrapScroll(tblAudit), BorderLayout.CENTER);

        masterSplit.add(posCard);
        masterSplit.add(auditCard);
        tabs.addTab("4. Master Pos & Audit Log", masterSplit);

        add(tabs, BorderLayout.CENTER);
    }

    private void aksiTambahUser() {
        JTextField txtNama = new JTextField();
        JTextField txtNim = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<RoleType> cmbRole = new JComboBox<>(RoleType.values());

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Nama Lengkap *:"));
        form.add(txtNama);
        form.add(new JLabel("NIM / NIP *:"));
        form.add(txtNim);
        form.add(new JLabel("Email Unik *:"));
        form.add(txtEmail);
        form.add(new JLabel("Kata Sandi (Min. 8 Karakter) *:"));
        form.add(txtPass);
        form.add(new JLabel("Role Pengguna:"));
        form.add(cmbRole);

        if (JOptionPane.showConfirmDialog(this, form, "Tambah Akun Pengguna Baru (US-022)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                adminService.tambahPengguna(
                        txtNama.getText(), txtNim.getText(), txtEmail.getText(),
                        new String(txtPass.getPassword()), (RoleType) cmbRole.getSelectedItem(), activeAdmin
                );
                JOptionPane.showMessageDialog(this, "Akun pengguna baru berhasil dibuat!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validasi Gagal", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aksiUbahRole() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) return;
        String userId = (String) modelUsers.getValueAt(row, 0);
        RoleType roleBaru = (RoleType) JOptionPane.showInputDialog(this, "Pilih role baru:",
                "Ubah Role Pengguna", JOptionPane.QUESTION_MESSAGE, null, RoleType.values(), RoleType.SECURITY);
        if (roleBaru == null) return;
        try {
            adminService.ubahRolePengguna(userId, roleBaru, activeAdmin);
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiToggleUser() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) return;
        String userId = (String) modelUsers.getValueAt(row, 0);
        String statusStr = (String) modelUsers.getValueAt(row, 5);
        boolean aktifBaru = !"AKTIF".equalsIgnoreCase(statusStr);
        try {
            adminService.toggleAktifPengguna(userId, aktifBaru, activeAdmin);
            refreshData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Proteksi Diri (US-022)", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiArsipkanLama() {
        try {
            int jml = petugasService.arsipkanBarangLama(60, activeAdmin);
            JOptionPane.showMessageDialog(this,
                    jml + " barang lama (> 60 hari) berhasil diarsipkan menjadi status DIARSIPKAN.",
                    "Pengarsipan (v2.0)", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
            if (onDataChanged != null) onDataChanged.run();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aksiTambahPos() {
        JTextField txtNama = new JTextField();
        JTextField txtLok = new JTextField();
        JTextField txtJam = new JTextField("08:00 - 17:00");
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Nama Pos *:"));
        form.add(txtNama);
        form.add(new JLabel("Lokasi Penyerahan *:"));
        form.add(txtLok);
        form.add(new JLabel("Jam Layanan:"));
        form.add(txtJam);

        if (JOptionPane.showConfirmDialog(this, form, "Tambah Pos Penyimpanan", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                adminService.tambahPos(txtNama.getText(), txtLok.getText(), txtJam.getText(), activeAdmin);
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        if (activeAdmin == null) return;
        AdminService.StatistikRingkasan st = adminService.hitungStatistik();
        lblKpiTotal.setText(String.valueOf(st.totalLaporan()));
        lblKpiReturnRate.setText(st.returnRatePersen() + "%");
        lblKpiAvgJam.setText(st.rataRataJamPenyelesaian() + " jam");
        lblKpiArsip.setText(String.valueOf(st.totalDiarsipkan()));

        chartKategori.setData(st.perKategori());
        chartJenis.setData(st.perJenis());
        chartBulan.setData(st.perBulan());

        modelUsers.setRowCount(0);
        try {
            for (UserAccount u : adminService.getAllUsers(activeAdmin)) {
                modelUsers.addRow(new Object[]{
                        u.getId(), u.getNama(), u.getNomorIdentitas(),
                        u.getEmail(), u.getRole().getLabel(), u.isAktif() ? "AKTIF" : "NONAKTIF"
                });
            }
        } catch (SecurityException ignored) {
        }

        modelItems.setRowCount(0);
        for (Item item : itemRepository.findAll()) {
            String pelapor = item.getPelapor() != null ? item.getPelapor().getNama() : "-";
            modelItems.addRow(new Object[]{
                    item.getId(), item.getJenis().name(), item.getNama(),
                    item.getKategori().getLabel(), pelapor, item.getStatus().name()
            });
        }

        modelPos.setRowCount(0);
        for (PosPenyimpanan p : posRepository.findAll()) {
            modelPos.addRow(new Object[]{p.getId(), p.getNama(), p.getLokasiPenyerahan(), p.getLabelPendek()});
        }

        modelAudit.setRowCount(0);
        for (String log : petugasService.getAuditLogGlobal()) {
            modelAudit.addRow(new Object[]{log});
        }
    }
}
