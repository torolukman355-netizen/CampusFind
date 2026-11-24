package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.DateField;
import com.campusfind.ui.components.ErrorLabel;
import com.campusfind.ui.components.FieldLabel;
import com.campusfind.ui.components.FilePickerField;
import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.components.FooterBar;
import com.campusfind.ui.components.PageHeader;
import com.campusfind.ui.components.PlaceholderTextArea;
import com.campusfind.ui.components.PlaceholderTextField;
import com.campusfind.ui.components.ResponsivePage;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.components.StyledComboBox;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;
import com.campusfind.validation.LaporanValidator;
import com.campusfind.validation.ValidationResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class LaporPage extends ResponsivePage {

    private final NavigationController controller;
    private final ItemService itemService;
    private final PosService posService;
    private final AppSession appSession;
    private final Clock clock;

    private final PageHeader header;
    private final RoundedPanel noticeBanner;
    private final JLabel noticeText;
    private final FlatButton openKlaimBtn;
    private final RoundedPanel formCard;
    private final FooterBar footerBar;

    private JRadioButton rbTemuan;
    private JRadioButton rbHilang;
    private JPanel radioPanel;

    private PlaceholderTextField namaBarangField;
    private StyledComboBox<String> kategoriComboBox;

    private PlaceholderTextField merekField;
    private PlaceholderTextField nomorSeriField;
    private PlaceholderTextField warnaField;

    private PlaceholderTextArea deskripsiArea;
    private PlaceholderTextField lokasiField;
    private DateField tanggalField;

    private JPanel posPanel;
    private StyledComboBox<String> posComboBox;
    private List<PosPenyimpanan> posList;

    private JPanel ciriRahasiaPanel;
    private PlaceholderTextArea ciriRahasiaArea;

    private FilePickerField fotoPicker;

    private PlaceholderTextField namaPelaporField;
    private PlaceholderTextField identitasPelaporField;

    private ErrorLabel formError;
    private FlatButton simpanButton;

    public LaporPage(NavigationController controller,
                     ItemService itemService,
                     PosService posService,
                     AppSession appSession) {
        super(860);
        this.controller = controller;
        this.itemService = itemService;
        this.posService = posService;
        this.appSession = appSession;
        this.clock = Clock.systemDefaultZone();

        // 1. Reusable Page Header (Centered eyebrow, title, and wrapping subtitle)
        header = new PageHeader(
                "FORMULIR PELAPORAN",
                "Lapor Barang",
                "Laporkan barang yang Anda temukan atau barang milik Anda yang hilang di lingkungan kampus."
        );
        addContent(header);

        // 2. Banner Pemisah Lapor vs Klaim (Text on top centered, Button below centered)
        addContentStrut(18);
        noticeBanner = new RoundedPanel(Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        noticeBanner.setLayout(new BoxLayout(noticeBanner, BoxLayout.Y_AXIS));
        noticeBanner.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        noticeBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        noticeText = new JLabel();
        noticeText.setAlignmentX(Component.CENTER_ALIGNMENT);
        noticeText.setHorizontalAlignment(SwingConstants.CENTER);

        openKlaimBtn = new FlatButton("MENU KLAIM BARANG");
        openKlaimBtn.setFont(Fonts.sans(11.5f, Font.BOLD));
        openKlaimBtn.addActionListener(e -> controller.navigateToKlaim(null));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRow.add(openKlaimBtn);

        noticeBanner.add(noticeText);
        noticeBanner.add(Box.createVerticalStrut(12));
        noticeBanner.add(btnRow);
        addContent(noticeBanner);

        // 3. Form Card
        addContentStrut(20);
        formCard = createFormPanel();
        formCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(formCard);

        // 4. Footer
        addContentStrut(40);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);

        updateNoticeBannerWidth(getCurrentContentWidth());
    }

    private void updateNoticeBannerWidth(int contentWidth) {
        int textWrap = Math.min(800, Math.max(240, contentWidth - 64));
        noticeText.setText(String.format(
                "<html><body style='margin:0;padding:0;'><center><table width='%d' cellpadding='0' cellspacing='0'><tr><td align='center' style='font-family:sans-serif;font-size:13px;color:#2A2420;line-height:1.45;text-align:center;'>" +
                        "Barang Anda hilang dan sudah ditemukan orang lain di katalog? " +
                        "<b>Gunakan fitur Klaim Barang untuk verifikasi kepemilikan.</b>" +
                        "</td></tr></table></center></body></html>",
                textWrap
        ));
        noticeBanner.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
    }

    @Override
    protected void onContentWidthChanged(int contentWidth) {
        header.updateWidth(contentWidth);
        updateNoticeBannerWidth(contentWidth);
        footerBar.updateResponsive(contentWidth);

        formCard.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));

        if (contentWidth < Theme.BREAKPOINT_COMPACT) {
            radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
            rbTemuan.setAlignmentX(Component.CENTER_ALIGNMENT);
            rbHilang.setAlignmentX(Component.CENTER_ALIGNMENT);
        } else {
            radioPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 24, 0));
        }

        revalidate();
        repaint();
    }

    private RoundedPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // 1. Jenis Laporan Radio
        form.add(new FieldLabel("JENIS LAPORAN"));
        form.add(Box.createVerticalStrut(8));
        radioPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        radioPanel.setOpaque(false);
        radioPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rbTemuan = new JRadioButton("Barang Temuan (Saya menemukan barang)", true);
        rbHilang = new JRadioButton("Barang Hilang (Barang saya hilang)");
        for (JRadioButton rb : List.of(rbTemuan, rbHilang)) {
            rb.setFont(Fonts.sans(13f, Font.PLAIN));
            rb.setForeground(Theme.TEXT_PRIMARY);
            rb.setOpaque(false);
            rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rb.addActionListener(e -> updateFormVisibility());
        }
        ButtonGroup group = new ButtonGroup();
        group.add(rbTemuan);
        group.add(rbHilang);
        radioPanel.add(rbTemuan);
        radioPanel.add(rbHilang);
        form.add(radioPanel);

        // 2. Nama Barang
        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("NAMA BARANG"));
        form.add(Box.createVerticalStrut(8));
        namaBarangField = new PlaceholderTextField("Contoh: Kunci Motor Vario, MacBook Air, Dompet Kulit");
        form.add(namaBarangField);

        // 3. Kategori
        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("KATEGORI BARANG"));
        form.add(Box.createVerticalStrut(8));
        KategoriBarang[] cats = KategoriBarang.values();
        String[] catLabels = new String[cats.length];
        for (int i = 0; i < cats.length; i++) {
            catLabels[i] = cats[i].getLabel();
        }
        kategoriComboBox = new StyledComboBox<>(catLabels);
        kategoriComboBox.addActionListener(e -> updateCategoryFields());
        form.add(kategoriComboBox);

        // 4. Spesifik Elektronik / Non-Elektronik
        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("MEREK"));
        form.add(Box.createVerticalStrut(8));
        merekField = new PlaceholderTextField("Merek barang (wajib untuk barang elektronik)");
        form.add(merekField);

        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("NOMOR SERI (OPSIONAL, ELEKTRONIK)"));
        form.add(Box.createVerticalStrut(8));
        nomorSeriField = new PlaceholderTextField("Nomor seri perangkat (hanya disimpan secara privat)");
        form.add(nomorSeriField);

        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("WARNA (NON-ELEKTRONIK)"));
        form.add(Box.createVerticalStrut(8));
        warnaField = new PlaceholderTextField("Warna utama barang");
        form.add(warnaField);

        // 5. Deskripsi
        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("DESKRIPSI BARANG"));
        form.add(Box.createVerticalStrut(8));
        deskripsiArea = new PlaceholderTextArea("Jelaskan detail barang secara umum...");
        deskripsiArea.setPreferredSize(new Dimension(deskripsiArea.getPreferredSize().width, 70));
        form.add(deskripsiArea);

        // 6. Lokasi & Tanggal
        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("LOKASI KEJADIAN"));
        form.add(Box.createVerticalStrut(8));
        lokasiField = new PlaceholderTextField("Contoh: Ruang Baca Perpustakaan Pusat Lt. 2");
        form.add(lokasiField);

        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("TANGGAL KEJADIAN"));
        form.add(Box.createVerticalStrut(8));
        tanggalField = new DateField();
        tanggalField.setDate(LocalDate.now(clock));
        form.add(tanggalField);

        // 7. Pos Penyimpanan (Hanya TEMUAN)
        posPanel = new JPanel();
        posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.Y_AXIS));
        posPanel.setOpaque(false);
        posPanel.add(Box.createVerticalStrut(20));
        posPanel.add(new FieldLabel("POS PENYIMPANAN"));
        posPanel.add(Box.createVerticalStrut(8));

        posList = posService.findAll();
        String[] posNames = posList.stream().map(PosPenyimpanan::getNama).toArray(String[]::new);
        posComboBox = new StyledComboBox<>(posNames);
        posPanel.add(posComboBox);
        form.add(posPanel);

        // 8. Ciri Rahasia Verifikasi (Hanya TEMUAN)
        ciriRahasiaPanel = new JPanel();
        ciriRahasiaPanel.setLayout(new BoxLayout(ciriRahasiaPanel, BoxLayout.Y_AXIS));
        ciriRahasiaPanel.setOpaque(false);
        ciriRahasiaPanel.add(Box.createVerticalStrut(20));
        ciriRahasiaPanel.add(new FieldLabel("CIRI RAHASIA VERIFIKASI (OPSIONAL, TIDAK DITAMPILKAN DI PUBLIK)"));
        ciriRahasiaPanel.add(Box.createVerticalStrut(8));
        ciriRahasiaArea = new PlaceholderTextArea("Ciri yang hanya diketahui oleh pemilik asli barang untuk mencocokkan klaim...");
        ciriRahasiaArea.setPreferredSize(new Dimension(ciriRahasiaArea.getPreferredSize().width, 60));
        ciriRahasiaPanel.add(ciriRahasiaArea);
        form.add(ciriRahasiaPanel);

        // 9. Foto
        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("FOTO BARANG (OPSIONAL)"));
        form.add(Box.createVerticalStrut(8));
        fotoPicker = new FilePickerField();
        form.add(fotoPicker);

        // 10. Identitas Pelapor
        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("NAMA PELAPOR"));
        form.add(Box.createVerticalStrut(8));
        namaPelaporField = new PlaceholderTextField("Nama lengkap Anda");
        if (appSession.isLoggedIn() && appSession.getNama() != null) {
            namaPelaporField.setText(appSession.getNama());
        }
        form.add(namaPelaporField);

        form.add(Box.createVerticalStrut(20));
        form.add(new FieldLabel("NOMOR IDENTITAS PELAPOR (NIM / NIP)"));
        form.add(Box.createVerticalStrut(8));
        identitasPelaporField = new PlaceholderTextField("NIM 9 digit atau NIP 18 digit");
        if (appSession.isLoggedIn() && appSession.getNomorIdentitas() != null) {
            identitasPelaporField.setText(appSession.getNomorIdentitas());
        }
        form.add(identitasPelaporField);

        // Error message
        form.add(Box.createVerticalStrut(12));
        formError = new ErrorLabel();
        form.add(formError);

        // Tombol Simpan Laporan (Besar, menonjol, dan tidak pernah terpotong)
        form.add(Box.createVerticalStrut(28));
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        simpanButton = new FlatButton("SIMPAN LAPORAN");
        simpanButton.setPreferredSize(new Dimension(240, 44));
        simpanButton.setFont(Fonts.sans(12f, Font.BOLD));
        simpanButton.setRadius(4);
        simpanButton.addActionListener(e -> simpanLaporan());
        btnWrapper.add(simpanButton);
        form.add(btnWrapper);
        form.add(Box.createVerticalStrut(14));

        card.add(form, BorderLayout.CENTER);
        updateCategoryFields();
        return card;
    }

    private void updateCategoryFields() {
        boolean isElektronik = (kategoriComboBox.getSelectedIndex() == 0); // ELEKTRONIK
        merekField.setEnabled(isElektronik);
        nomorSeriField.setEnabled(isElektronik);
        warnaField.setEnabled(!isElektronik);
    }

    private void updateFormVisibility() {
        boolean isTemuan = rbTemuan.isSelected();
        posPanel.setVisible(isTemuan);
        ciriRahasiaPanel.setVisible(isTemuan);
        revalidate();
        repaint();
    }

    private void simpanLaporan() {
        formError.clearError();

        JenisLaporan jenis = rbTemuan.isSelected() ? JenisLaporan.TEMUAN : JenisLaporan.HILANG;
        String nama = namaBarangField.getText().trim();
        KategoriBarang kategori = KategoriBarang.values()[kategoriComboBox.getSelectedIndex()];
        String deskripsi = deskripsiArea.getText().trim();
        String lokasi = lokasiField.getText().trim();
        LocalDate tanggal = tanggalField.getDate();
        String namaPelapor = namaPelaporField.getText().trim();
        String nomorIdentitas = identitasPelaporField.getText().trim();

        String posId = null;
        if (jenis == JenisLaporan.TEMUAN && !posList.isEmpty()) {
            posId = posList.get(posComboBox.getSelectedIndex()).getId();
        }

        String ciriRahasia = (jenis == JenisLaporan.TEMUAN) ? ciriRahasiaArea.getText().trim() : null;
        if (ciriRahasia != null && ciriRahasia.isEmpty()) ciriRahasia = null;

        File fotoFile = fotoPicker.getSelectedFile();

        String merekAtauWarna = (kategori == KategoriBarang.ELEKTRONIK) ? merekField.getText().trim() : warnaField.getText().trim();
        String nomorSeriAtauBahan = (kategori == KategoriBarang.ELEKTRONIK) ? nomorSeriField.getText().trim() : "";

        // Validasi
        ValidationResult val = LaporanValidator.validateAll(
                nama, lokasi, deskripsi, tanggal, posId, jenis, ciriRahasia,
                fotoFile, nomorIdentitas, namaPelapor, clock
        );

        if (!val.isValid()) {
            formError.setError(val.getFirstError());
            return;
        }

        simpanButton.setEnabled(false);

        // Async submit
        String finalPosId = posId;
        String finalCiriRahasia = ciriRahasia;
        SwingWorker<Item, Void> worker = new SwingWorker<>() {
            @Override
            protected Item doInBackground() {
                return itemService.laporkan(
                        jenis, nama, kategori, deskripsi, lokasi, tanggal,
                        finalPosId, fotoFile, finalCiriRahasia,
                        namaPelapor, nomorIdentitas,
                        merekAtauWarna, nomorSeriAtauBahan
                );
            }

            @Override
            protected void done() {
                simpanButton.setEnabled(true);
                try {
                    Item created = get();
                    // Simpan sesi pelapor jika belum ada
                    appSession.setIdentitas(nomorIdentitas, namaPelapor);

                    // Dialog sukses menampilkan kode #ARC-xxxx
                    controller.showSuccessDialog(
                            "Laporan barang berhasil disimpan!",
                            created.kode()
                    );
                    controller.navigateToTemuan();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    formError.setError(cause.getMessage());
                }
            }
        };

        worker.execute();
    }
}
