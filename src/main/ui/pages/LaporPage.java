package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.DateField;
import com.campusfind.ui.components.ErrorLabel;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.time.Clock;
import java.time.LocalDate;

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

    // Jenis laporan segmented toggle
    private boolean isTemuanSelected = false;
    private JButton btnJenisHilang;
    private JButton btnJenisTemuan;

    private PlaceholderTextField namaBarangField;
    private ErrorLabel namaErrorLabel;
    private StyledComboBox<String> kategoriComboBox;

    private PlaceholderTextField merekField;
    private PlaceholderTextField nomorSeriField;
    private PlaceholderTextField warnaField;

    private PlaceholderTextArea deskripsiArea;
    private PlaceholderTextField lokasiField;
    private DateField tanggalField;

    private JPanel temuanExtraPanel;
    private PlaceholderTextField tempatPenitipanField;
    private PlaceholderTextField ciriRahasiaField;

    private FilePickerField fotoPicker;

    private PlaceholderTextField namaPelaporField;
    private PlaceholderTextField identitasPelaporField;

    private ErrorLabel formError;
    private JButton simpanButton;
    private JButton bersihkanButton;

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

        // 1. Reusable Page Header
        header = new PageHeader(
                "FORMULIR PELAPORAN",
                "Lapor Barang",
                "Laporkan barang yang Anda temukan atau barang milik Anda yang hilang di lingkungan kampus."
        );
        addContent(header);

        // 2. Banner Pemisah Lapor vs Klaim
        addContentStrut(18);
        noticeBanner = new RoundedPanel(Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        noticeBanner.setLayout(new BoxLayout(noticeBanner, BoxLayout.Y_AXIS));
        noticeBanner.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
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

        // 3. Modern 2-Column Form Card
        addContentStrut(20);
        formCard = createFormPanel();
        formCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.setMaximumSize(new Dimension(getCurrentContentWidth(), Integer.MAX_VALUE));
        addContent(formCard);

        // 4. Footer
        addContentStrut(36);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(36);

        updateNoticeBannerWidth(getCurrentContentWidth());
    }

    private void updateNoticeBannerWidth(int contentWidth) {
        int textWrap = Math.min(800, Math.max(240, contentWidth - 64));
        noticeText.setText(String.format(
                "<html><body style='margin:0;padding:0;'><center><table width='%d' cellpadding='0' cellspacing='0'><tr><td align='center' style='font-family:sans-serif;font-size:13px;color:#0F172A;line-height:1.45;text-align:center;'>" +
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
    }

    private RoundedPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Card Title & Subtitle
        JLabel cardTitle = new JLabel("Buat Laporan Baru");
        cardTitle.setFont(Fonts.sans(20f, Font.BOLD));
        cardTitle.setForeground(Theme.TEXT_PRIMARY);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cardTitle);

        form.add(Box.createVerticalStrut(4));

        JLabel cardSub = new JLabel("Isi detail barang. Rata-rata hanya butuh kurang dari 2 menit.");
        cardSub.setFont(Fonts.sans(13f, Font.PLAIN));
        cardSub.setForeground(Theme.TEXT_MUTED);
        cardSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(cardSub);

        form.add(Box.createVerticalStrut(22));

        // -------------------------------------------------------------
        // ROW 1: Jenis laporan | Kategori
        // -------------------------------------------------------------
        JPanel jenisSegmented = createJenisSegmentedControl();

        KategoriBarang[] cats = KategoriBarang.values();
        String[] catLabels = new String[cats.length];
        for (int i = 0; i < cats.length; i++) {
            catLabels[i] = cats[i].getLabel();
        }
        kategoriComboBox = new StyledComboBox<>(catLabels);
        kategoriComboBox.addActionListener(e -> updateCategoryFields());

        form.add(createTwoColRow(
                createFieldCol("Jenis laporan", null, jenisSegmented, null),
                createFieldCol("Kategori", null, kategoriComboBox, null)
        ));
        form.add(Box.createVerticalStrut(16));

        // -------------------------------------------------------------
        // ROW 2: Nama barang | Warna
        // -------------------------------------------------------------
        namaBarangField = new PlaceholderTextField("mis. Laptop ASUS Vivobook");
        namaErrorLabel = new ErrorLabel();
        warnaField = new PlaceholderTextField("mis. Hitam, Silver, Coklat");

        form.add(createTwoColRow(
                createFieldCol("Nama barang", null, namaBarangField, namaErrorLabel),
                createFieldCol("Warna", null, warnaField, null)
        ));
        form.add(Box.createVerticalStrut(16));

        // -------------------------------------------------------------
        // ROW 3: Merk · khusus elektronik | Nomor seri · khusus elektronik
        // -------------------------------------------------------------
        merekField = new PlaceholderTextField("mis. ASUS, Apple, Samsung");
        nomorSeriField = new PlaceholderTextField("Opsional");

        form.add(createTwoColRow(
                createFieldCol("Merk", "· khusus elektronik", merekField, null, Theme.EYEBROW_TEXT),
                createFieldCol("Nomor seri", "· khusus elektronik", nomorSeriField, null, Theme.EYEBROW_TEXT)
        ));
        form.add(Box.createVerticalStrut(16));

        // -------------------------------------------------------------
        // ROW 4: Lokasi kejadian | Tanggal kejadian
        // -------------------------------------------------------------
        lokasiField = new PlaceholderTextField("mis. Perpustakaan Lt. 2");
        tanggalField = new DateField();
        tanggalField.setDate(LocalDate.now(clock));

        form.add(createTwoColRow(
                createFieldCol("Lokasi kejadian", null, lokasiField, null),
                createFieldCol("Tanggal kejadian", "(dd/mm/yyyy)", tanggalField, null, Theme.TEXT_MUTED)
        ));
        form.add(Box.createVerticalStrut(16));

        // -------------------------------------------------------------
        // ROW 5 (Conditional): Tempat Penitipan | Ciri Rahasia (Hanya TEMUAN)
        // -------------------------------------------------------------
        temuanExtraPanel = new JPanel();
        temuanExtraPanel.setLayout(new BoxLayout(temuanExtraPanel, BoxLayout.Y_AXIS));
        temuanExtraPanel.setOpaque(false);
        temuanExtraPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tempatPenitipanField = new PlaceholderTextField("mis. Pos Satpam Gerbang Utama");
        ciriRahasiaField = new PlaceholderTextField("Opsional · untuk verifikasi klaim");

        temuanExtraPanel.add(createTwoColRow(
                createFieldCol("Tempat penitipan barang", null, tempatPenitipanField, null),
                createFieldCol("Ciri rahasia", "(opsional)", ciriRahasiaField, null, Theme.TEXT_MUTED)
        ));
        temuanExtraPanel.add(Box.createVerticalStrut(16));
        form.add(temuanExtraPanel);

        // -------------------------------------------------------------
        // ROW 6: Deskripsi (Full Width)
        // -------------------------------------------------------------
        deskripsiArea = new PlaceholderTextArea("Jelaskan detail barang (mis. Laptop hitam dengan stiker bintang di sisi kiri, tertinggal di meja dekat jendela).");
        deskripsiArea.setPreferredSize(new Dimension(0, 90));
        deskripsiArea.setMinimumSize(new Dimension(0, 90));
        form.add(createFieldCol("Deskripsi", null, deskripsiArea, null));
        form.add(Box.createVerticalStrut(16));

        // -------------------------------------------------------------
        // ROW 7: Lampiran foto (opsional) - Dashed Dropzone
        // -------------------------------------------------------------
        fotoPicker = new FilePickerField();
        fotoPicker.setDashedDropzone(true, "Pilih berkas...");
        form.add(createFieldCol("Lampiran foto", "(opsional)", fotoPicker, null, Theme.TEXT_MUTED));
        form.add(Box.createVerticalStrut(16));

        // -------------------------------------------------------------
        // ROW 8: Identitas Pelapor (2 Columns, auto-filled from session)
        // -------------------------------------------------------------
        namaPelaporField = new PlaceholderTextField("Nama lengkap Anda");
        identitasPelaporField = new PlaceholderTextField("NIM 9 digit atau NIP 18 digit");
        if (appSession.isLoggedIn()) {
            if (appSession.getNama() != null) namaPelaporField.setText(appSession.getNama());
            if (appSession.getNomorIdentitas() != null) identitasPelaporField.setText(appSession.getNomorIdentitas());
        }
        form.add(createTwoColRow(
                createFieldCol("Nama pelapor", null, namaPelaporField, null),
                createFieldCol("NIM / NIP pelapor", null, identitasPelaporField, null)
        ));

        // General Error Message
        form.add(Box.createVerticalStrut(12));
        formError = new ErrorLabel();
        form.add(formError);

        // -------------------------------------------------------------
        // ROW 9: Footer Action Bar (Right-aligned: Bersihkan & Kirim Laporan)
        // -------------------------------------------------------------
        form.add(Box.createVerticalStrut(20));
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        bersihkanButton = createSecondaryButton("Bersihkan");
        bersihkanButton.addActionListener(e -> resetForm());

        simpanButton = createPrimaryButton("Kirim Laporan");
        simpanButton.addActionListener(e -> simpanLaporan());

        actionRow.add(bersihkanButton);
        actionRow.add(simpanButton);
        form.add(actionRow);

        card.add(form, BorderLayout.CENTER);
        updateCategoryFields();
        updateFormVisibility();
        return card;
    }

    private JPanel createJenisSegmentedControl() {
        JPanel segContainer = new JPanel(new GridLayout(1, 2, 4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF1, 0xF5, 0xF9)); // Slate 100
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.INPUT_RADIUS + 2, Theme.INPUT_RADIUS + 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        segContainer.setOpaque(false);
        segContainer.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        segContainer.setPreferredSize(new Dimension(0, Theme.INPUT_HEIGHT));
        segContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.INPUT_HEIGHT));
        segContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnJenisHilang = createSegmentButton("?  Hilang", true);
        btnJenisTemuan = createSegmentButton("🤲  Temuan", false);

        btnJenisHilang.addActionListener(e -> {
            isTemuanSelected = false;
            updateSegmentStyles();
            updateFormVisibility();
        });

        btnJenisTemuan.addActionListener(e -> {
            isTemuanSelected = true;
            updateSegmentStyles();
            updateFormVisibility();
        });

        segContainer.add(btnJenisHilang);
        segContainer.add(btnJenisTemuan);
        updateSegmentStyles();
        return segContainer;
    }

    private JButton createSegmentButton(String text, boolean isHilangBtn) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                boolean active = (isHilangBtn && !isTemuanSelected) || (!isHilangBtn && isTemuanSelected);
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), Theme.INPUT_RADIUS, Theme.INPUT_RADIUS));
                    g2.setColor(new Color(0xE2, 0xE8, 0xF0));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(Fonts.sans(13f, Font.BOLD));
        return btn;
    }

    private void updateSegmentStyles() {
        if (btnJenisHilang != null && btnJenisTemuan != null) {
            if (!isTemuanSelected) {
                btnJenisHilang.setForeground(new Color(0xDC, 0x26, 0x26)); // Red 600
                btnJenisTemuan.setForeground(Theme.TEXT_SECONDARY);
            } else {
                btnJenisHilang.setForeground(Theme.TEXT_SECONDARY);
                btnJenisTemuan.setForeground(Theme.BUTTON_DARK_BG); // Indigo 600
            }
            btnJenisHilang.repaint();
            btnJenisTemuan.repaint();
        }
    }

    private JPanel createTwoColRow(JPanel left, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.add(left);
        row.add(right);
        return row;
    }

    private JPanel createFieldCol(String labelText, String badgeText, JComponent field, JComponent errorBelow) {
        return createFieldCol(labelText, badgeText, field, errorBelow, Theme.TEXT_MUTED);
    }

    private JPanel createFieldCol(String labelText, String badgeText, JComponent field, JComponent errorBelow, Color badgeColor) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        col.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel lblRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lblRow.setOpaque(false);
        lblRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Fonts.sans(12.5f, Font.BOLD));
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lblRow.add(lbl);

        if (badgeText != null && !badgeText.isBlank()) {
            JLabel badge = new JLabel(" " + badgeText);
            badge.setFont(Fonts.sans(11.5f, Font.PLAIN));
            badge.setForeground(badgeColor);
            lblRow.add(badge);
        }

        col.add(lblRow);
        col.add(Box.createVerticalStrut(6));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(field);

        if (errorBelow != null) {
            col.add(Box.createVerticalStrut(4));
            errorBelow.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(errorBelow);
        }

        return col;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton("✈  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(Theme.BUTTON_DARK_DISABLED);
                } else if (getModel().isPressed()) {
                    g2.setColor(Theme.BUTTON_DARK_PRESSED);
                } else if (getModel().isRollover()) {
                    g2.setColor(Theme.BUTTON_DARK_HOVER);
                } else {
                    g2.setColor(Theme.BUTTON_DARK_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Fonts.sans(13f, Font.BOLD));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xF8, 0xFA, 0xFC) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
                g2.setColor(Theme.INPUT_BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Fonts.sans(13f, Font.BOLD));
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void resetForm() {
        namaBarangField.setText("");
        namaBarangField.setHasError(false);
        namaErrorLabel.clearError();
        warnaField.setText("");
        merekField.setText("");
        nomorSeriField.setText("");
        lokasiField.setText("");
        tanggalField.setDate(LocalDate.now(clock));
        tempatPenitipanField.setText("");
        ciriRahasiaField.setText("");
        deskripsiArea.setText("");
        fotoPicker.clearFile();
        formError.clearError();
    }

    private void updateCategoryFields() {
        boolean isElektronik = (kategoriComboBox.getSelectedIndex() == 0); // ELEKTRONIK
        merekField.setEnabled(isElektronik);
        nomorSeriField.setEnabled(isElektronik);
    }

    private void updateFormVisibility() {
        temuanExtraPanel.setVisible(isTemuanSelected);
        revalidate();
        repaint();
    }

    private void simpanLaporan() {
        formError.clearError();
        namaErrorLabel.clearError();
        namaBarangField.setHasError(false);

        JenisLaporan jenis = isTemuanSelected ? JenisLaporan.TEMUAN : JenisLaporan.HILANG;
        String nama = namaBarangField.getText().trim();
        KategoriBarang kategori = KategoriBarang.values()[kategoriComboBox.getSelectedIndex()];
        String deskripsi = deskripsiArea.getText().trim();
        String lokasi = lokasiField.getText().trim();
        LocalDate tanggal = tanggalField.getDate();
        String namaPelapor = namaPelaporField.getText().trim();
        String nomorIdentitas = identitasPelaporField.getText().trim();

        if (nama.isEmpty()) {
            namaBarangField.setHasError(true);
            namaErrorLabel.setError("Nama barang wajib diisi.");
            namaBarangField.requestFocusInWindow();
            return;
        }

        String tempatPenitipan = (jenis == JenisLaporan.TEMUAN && tempatPenitipanField != null)
                ? tempatPenitipanField.getText().trim()
                : null;
        if (tempatPenitipan != null && tempatPenitipan.isEmpty()) tempatPenitipan = null;
        String posId = tempatPenitipan;

        String ciriRahasia = (jenis == JenisLaporan.TEMUAN && ciriRahasiaField != null)
                ? ciriRahasiaField.getText().trim()
                : null;
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
                    appSession.setIdentitas(nomorIdentitas, namaPelapor);
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
