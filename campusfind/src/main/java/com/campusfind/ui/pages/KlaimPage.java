package com.campusfind.ui.pages;

import com.campusfind.service.BarangTidakTersediaException;
import com.campusfind.service.KlaimGandaException;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.DateField;
import com.campusfind.ui.components.ErrorLabel;
import com.campusfind.ui.components.FieldLabel;
import com.campusfind.ui.components.FilePickerField;
import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.components.FooterBar;
import com.campusfind.ui.components.ItemSummaryCard;
import com.campusfind.ui.components.PageHeader;
import com.campusfind.ui.components.PlaceholderTextArea;
import com.campusfind.ui.components.PlaceholderTextField;
import com.campusfind.ui.components.ResponsivePage;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.components.StyledCheckBox;
import com.campusfind.ui.components.StyledComboBox;
import com.campusfind.ui.icons.ArrowRightIcon;
import com.campusfind.ui.icons.IdCardIcon;
import com.campusfind.ui.icons.PinIcon;
import com.campusfind.ui.icons.ShieldIcon;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;
import com.campusfind.validation.KlaimValidator;
import com.campusfind.validation.ValidationResult;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class KlaimPage extends ResponsivePage {

    private final NavigationController controller;
    private String currentItemId;
    private final ItemService itemService;
    private final KlaimService klaimService;
    private final PosService posService;
    private final AppSession appSession;
    private final Clock clock;

    private Item item;
    private PosPenyimpanan pos;

    // View state
    private boolean isEmptyNotice = false;
    private PageHeader pageHeader;
    private JPanel emptyButtonsPanel;
    private FlatButton emptyLaporBtn;
    private FlatButton emptyTemuanBtn;

    // Main View Components
    private ItemSummaryCard summaryCard;
    private JPanel formCardsContainer;
    private JPanel leftCard;
    private JPanel rightCard;
    private boolean isCardsStacked = false;

    private RoundedPanel bottomBar;
    private JPanel bottomBarLeft;
    private JPanel bottomBarRight;
    private JLabel shieldText;
    private boolean isBottomBarStacked = false;

    private FooterBar footerBar;

    // Form Components
    private PlaceholderTextField identitasField;
    private ErrorLabel identitasError;

    private PlaceholderTextArea ciriKhususArea;
    private ErrorLabel ciriKhususError;

    private FilePickerField buktiPicker;
    private ErrorLabel buktiError;

    private DateField tanggalField;
    private ErrorLabel tanggalError;

    private StyledComboBox<String> sesiComboBox;
    private ErrorLabel sesiError;

    private StyledCheckBox cbIdentitas;
    private StyledCheckBox cbKepemilikan;
    private ErrorLabel checkboxError;

    private FlatButton submitButton;

    public KlaimPage(NavigationController controller,
                     String itemId,
                     ItemService itemService,
                     KlaimService klaimService,
                     PosService posService,
                     AppSession appSession) {
        super(Theme.MAX_CONTENT_WIDTH);
        this.controller = controller;
        this.currentItemId = itemId;
        this.itemService = itemService;
        this.klaimService = klaimService;
        this.posService = posService;
        this.appSession = appSession;
        this.clock = Clock.systemDefaultZone();

        initDataAndUI();
    }

    private void initDataAndUI() {
        // If itemId is null, find available found items
        if (currentItemId == null || currentItemId.isBlank()) {
            List<Item> availableItems = itemService.findByStatus(StatusItem.TERSEDIA).stream()
                    .filter(i -> i.getJenis() == JenisLaporan.TEMUAN)
                    .toList();

            if (availableItems.isEmpty()) {
                showEmptyAvailableNotice();
                return;
            }
            this.currentItemId = availableItems.get(0).getId();
        }

        Optional<Item> itemOpt = itemService.findById(currentItemId);
        if (itemOpt.isEmpty()) {
            showItemNotAvailable("Barang dengan kode " + currentItemId + " tidak ditemukan.");
            return;
        }

        this.item = itemOpt.get();
        if (item.getStatus() != StatusItem.TERSEDIA) {
            showItemNotAvailable("Barang ini sudah tidak tersedia untuk diklaim (" + item.getStatus().getLabel() + ").");
            return;
        }

        if (item.getPosId() != null) {
            this.pos = posService.findById(item.getPosId()).orElse(null);
        }

        buildMainView();
    }

    private void showEmptyAvailableNotice() {
        isEmptyNotice = true;
        clearContent();

        // 1. Centered Header
        pageHeader = new PageHeader(
                "PROSEDUR PENGAMBILAN TERAKREDITASI",
                "Klaim Barang",
                "Saat ini belum ada barang temuan yang berstatus <b>Tersedia</b> untuk diajukan klaim.<br><br>" +
                "Bagi Anda pemilik yang sedang kehilangan barang, Anda dapat membuat <b>Laporan Kehilangan</b> " +
                "agar dicatat di sistem dan segera dihubungi saat barang ditemukan oleh petugas satpam atau sivitas kampus.",
                580
        );
        pageHeader.updateWidth(getCurrentContentWidth());
        addContent(pageHeader);

        // 2. Responsive Centered Action Buttons
        addContentStrut(32);
        emptyButtonsPanel = new JPanel();
        emptyButtonsPanel.setOpaque(false);
        emptyButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyLaporBtn = new FlatButton("BUAT LAPORAN KEHILANGAN");
        emptyLaporBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyLaporBtn.addActionListener(e -> controller.navigateToLapor());

        emptyTemuanBtn = new FlatButton("LIHAT KATALOG ARSIP");
        emptyTemuanBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyTemuanBtn.addActionListener(e -> controller.navigateToTemuan());

        updateEmptyButtonsLayout(getCurrentContentWidth());
        addContent(emptyButtonsPanel);

        // 3. Footer
        addContentStrut(60);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);

        revalidate();
        repaint();
    }

    private void updateEmptyButtonsLayout(int contentWidth) {
        if (emptyButtonsPanel == null) return;
        emptyButtonsPanel.removeAll();

        if (contentWidth < 600) {
            emptyButtonsPanel.setLayout(new BoxLayout(emptyButtonsPanel, BoxLayout.Y_AXIS));
            emptyButtonsPanel.add(emptyLaporBtn);
            emptyButtonsPanel.add(Box.createVerticalStrut(12));
            emptyButtonsPanel.add(emptyTemuanBtn);
        } else {
            emptyButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 0));
            emptyButtonsPanel.add(emptyLaporBtn);
            emptyButtonsPanel.add(emptyTemuanBtn);
        }
        emptyButtonsPanel.revalidate();
        emptyButtonsPanel.repaint();
    }

    private void showItemNotAvailable(String message) {
        isEmptyNotice = true;
        clearContent();

        pageHeader = new PageHeader(
                "PEMBERITAHUAN KLAIM",
                "Status Barang",
                message,
                580
        );
        pageHeader.updateWidth(getCurrentContentWidth());
        addContent(pageHeader);

        addContentStrut(32);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        FlatButton backBtn = new FlatButton("KEMBALI KE BARANG TEMUAN");
        backBtn.addActionListener(e -> controller.navigateToTemuan());
        btnPanel.add(backBtn);
        addContent(btnPanel);

        addContentStrut(60);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);

        revalidate();
        repaint();
    }

    private void buildMainView() {
        isEmptyNotice = false;
        clearContent();

        // 1. Reusable Header (All strictly centered)
        pageHeader = new PageHeader(
                "PROSEDUR PENGAMBILAN TERAKREDITASI",
                "Klaim Barang",
                "Langkah verifikasi kepemilikan terpadu untuk memastikan barang kembali ke pemilik " +
                "sah tanpa risiko sengketa dengan tata kelola arsip kampus."
        );
        pageHeader.updateWidth(getCurrentContentWidth());
        addContent(pageHeader);

        // Optional: Item Selector if multiple found items available
        List<Item> allAvailable = itemService.findByStatus(StatusItem.TERSEDIA).stream()
                .filter(i -> i.getJenis() == JenisLaporan.TEMUAN)
                .toList();

        if (allAvailable.size() > 1) {
            addContentStrut(14);
            JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            selectorPanel.setOpaque(false);
            selectorPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel selLabel = new JLabel("PILIH BARANG YANG INGIN DIKLAIM:");
            selLabel.setFont(Fonts.trackedSans(10.5f, Font.BOLD, 0.10));
            selLabel.setForeground(Theme.TEXT_PRIMARY);

            String[] itemLabels = allAvailable.stream()
                    .map(i -> i.kode() + " — " + i.getNama() + " (" + i.getLokasiKejadian() + ")")
                    .toArray(String[]::new);

            StyledComboBox<String> itemCombo = new StyledComboBox<>(itemLabels);
            for (int i = 0; i < allAvailable.size(); i++) {
                if (allAvailable.get(i).getId().equals(currentItemId)) {
                    itemCombo.setSelectedIndex(i);
                    break;
                }
            }
            itemCombo.addActionListener(e -> {
                int idx = itemCombo.getSelectedIndex();
                if (idx >= 0 && idx < allAvailable.size()) {
                    String selectedId = allAvailable.get(idx).getId();
                    if (!selectedId.equals(currentItemId)) {
                        this.currentItemId = selectedId;
                        initDataAndUI();
                    }
                }
            });

            selectorPanel.add(selLabel);
            selectorPanel.add(itemCombo);
            addContent(selectorPanel);
        }

        // 2. ItemSummaryCard
        addContentStrut(18);
        String posNama = (pos != null) ? pos.getNama() : "Pos Satpam Rektorat";
        summaryCard = new ItemSummaryCard(item, posNama);
        summaryCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        summaryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
        addContent(summaryCard);

        // 3. Two Form Cards (Left: Verifikasi Rahasia, Right: Jadwal Pengambilan)
        addContentStrut(38);
        leftCard = createLeftCard();
        rightCard = createRightCard();

        formCardsContainer = new JPanel(new GridBagLayout());
        formCardsContainer.setOpaque(false);
        formCardsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(formCardsContainer);

        // 4. Bottom Action Bar (Responsive)
        addContentStrut(26);
        bottomBar = createBottomActionBar();
        bottomBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(bottomBar);

        // 5. FooterBar
        addContentStrut(36);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);

        updateResponsiveSubcomponents(getCurrentContentWidth());
    }

    @Override
    protected void onContentWidthChanged(int contentWidth) {
        if (pageHeader != null) {
            pageHeader.updateWidth(contentWidth);
        }
        if (footerBar != null) {
            footerBar.updateResponsive(contentWidth);
        }

        if (isEmptyNotice) {
            updateEmptyButtonsLayout(contentWidth);
            return;
        }

        updateResponsiveSubcomponents(contentWidth);
        revalidate();
        repaint();
    }

    private void updateResponsiveSubcomponents(int contentWidth) {
        if (formCardsContainer == null || leftCard == null || rightCard == null) return;

        // Form cards stacking: stack vertically if width < BREAKPOINT_LARGE (960)
        boolean shouldStackCards = contentWidth < Theme.BREAKPOINT_LARGE;
        if (shouldStackCards != isCardsStacked || formCardsContainer.getComponentCount() == 0) {
            isCardsStacked = shouldStackCards;
            formCardsContainer.removeAll();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;

            if (isCardsStacked) {
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1.0;
                gbc.insets = new Insets(0, 0, 16, 0);
                formCardsContainer.add(leftCard, gbc);

                gbc.gridy = 1;
                gbc.insets = new Insets(0, 0, 0, 0);
                formCardsContainer.add(rightCard, gbc);
            } else {
                gbc.gridy = 0;
                gbc.weighty = 1.0;

                gbc.gridx = 0;
                gbc.weightx = 0.58;
                gbc.insets = new Insets(0, 0, 0, 12);
                formCardsContainer.add(leftCard, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.42;
                gbc.insets = new Insets(0, 12, 0, 0);
                formCardsContainer.add(rightCard, gbc);
            }
            formCardsContainer.revalidate();
            formCardsContainer.repaint();
        }

        // Bottom action bar stacking: stack vertically if width < BREAKPOINT_NAV_COLLAPSE (860)
        boolean shouldStackBottom = contentWidth < Theme.BREAKPOINT_NAV_COLLAPSE;
        if (shouldStackBottom != isBottomBarStacked) {
            isBottomBarStacked = shouldStackBottom;
            bottomBar.removeAll();

            if (isBottomBarStacked) {
                bottomBar.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
                bottomBarLeft.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
                bottomBarRight.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 4));
                bottomBar.add(bottomBarLeft);
                bottomBar.add(bottomBarRight);
                bottomBar.setPreferredSize(new Dimension(contentWidth, 92));
            } else {
                bottomBar.setLayout(new BorderLayout());
                bottomBarLeft.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 18));
                bottomBarRight.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 10));
                bottomBar.add(bottomBarLeft, BorderLayout.WEST);
                bottomBar.add(bottomBarRight, BorderLayout.EAST);
                bottomBar.setPreferredSize(new Dimension(contentWidth, 58));
            }
            bottomBar.revalidate();
            bottomBar.repaint();
        } else {
            int bottomH = isBottomBarStacked ? 92 : 58;
            bottomBar.setPreferredSize(new Dimension(contentWidth, bottomH));
        }

        if (summaryCard != null) {
            summaryCard.setMaximumSize(new Dimension(contentWidth, 108));
        }
    }

    private JPanel createLeftCard() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel innerP = new JPanel();
        innerP.setLayout(new BoxLayout(innerP, BoxLayout.Y_AXIS));
        innerP.setOpaque(false);

        // Baris judul: Lingkaran "1" + "Verifikasi Kunci Rahasia" | kanan "WAJIB"
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(createNumberCircle("1"));
        JLabel titleLabel = new JLabel("Verifikasi Kunci Rahasia");
        titleLabel.setFont(Fonts.sans(14f, Font.BOLD));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);

        JLabel wajibLabel = new JLabel("WAJIB");
        wajibLabel.setFont(Fonts.trackedSans(9.5f, Font.BOLD, 0.12));
        wajibLabel.setForeground(Theme.TEXT_MUTED);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(wajibLabel, BorderLayout.EAST);
        innerP.add(header);

        // Field 1: NOMOR IDENTITAS (NIM / NIP)
        innerP.add(Box.createVerticalStrut(20));
        innerP.add(new FieldLabel("NOMOR IDENTITAS (NIM / NIP)"));
        innerP.add(Box.createVerticalStrut(8));

        identitasField = new PlaceholderTextField("NIM Mahasiswa atau NIP Staf Kampus", new IdCardIcon(16, Theme.TEXT_MUTED));
        if (appSession.isLoggedIn()) {
            identitasField.setText(appSession.getNomorIdentitas());
        }
        innerP.add(identitasField);

        identitasError = new ErrorLabel();
        innerP.add(Box.createVerticalStrut(4));
        innerP.add(identitasError);

        // Field 2: CIRI KHUSUS BARANG
        innerP.add(Box.createVerticalStrut(20));
        innerP.add(new FieldLabel("CIRI KHUSUS BARANG"));
        innerP.add(Box.createVerticalStrut(8));

        ciriKhususArea = new PlaceholderTextArea(
                "Sebutkan nama bluetooth AirPods, 3 digit nomor seri di tutup case, atau goresan identitas unik..."
        );
        ciriKhususArea.setPreferredSize(new Dimension(ciriKhususArea.getPreferredSize().width, 88));
        innerP.add(ciriKhususArea);

        ciriKhususError = new ErrorLabel();
        innerP.add(Box.createVerticalStrut(4));
        innerP.add(ciriKhususError);

        // Field 3: UNGGAH BUKTI PENDUKUNG (WAJIB)
        innerP.add(Box.createVerticalStrut(20));
        innerP.add(new FieldLabel("UNGGAH BUKTI PENDUKUNG (WAJIB)"));
        innerP.add(Box.createVerticalStrut(8));

        buktiPicker = new FilePickerField();
        innerP.add(buktiPicker);

        buktiError = new ErrorLabel();
        innerP.add(Box.createVerticalStrut(4));
        innerP.add(buktiError);

        card.add(innerP, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRightCard() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel innerP = new JPanel();
        innerP.setLayout(new BoxLayout(innerP, BoxLayout.Y_AXIS));
        innerP.setOpaque(false);

        // Baris judul: Lingkaran "2" + "Jadwal Pengambilan" | kanan labelPendek pos
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(createNumberCircle("2"));
        JLabel titleLabel = new JLabel("Jadwal Pengambilan");
        titleLabel.setFont(Fonts.sans(14f, Font.BOLD));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);

        String posLabel = (pos != null && pos.getLabelPendek() != null) ? pos.getLabelPendek() : "POS REKTORAT";
        JLabel rightLabel = new JLabel(posLabel);
        rightLabel.setFont(Fonts.trackedSans(9.5f, Font.BOLD, 0.12));
        rightLabel.setForeground(Theme.TEXT_MUTED);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightLabel, BorderLayout.EAST);
        innerP.add(header);

        // Field 1: TANGGAL SERAH TERIMA
        innerP.add(Box.createVerticalStrut(20));
        innerP.add(new FieldLabel("TANGGAL SERAH TERIMA"));
        innerP.add(Box.createVerticalStrut(8));

        tanggalField = new DateField();
        tanggalField.setDate(getInitialServiceDate());
        innerP.add(tanggalField);

        tanggalError = new ErrorLabel();
        innerP.add(Box.createVerticalStrut(4));
        innerP.add(tanggalError);

        // Field 2: SESI WAKTU LAYANAN
        innerP.add(Box.createVerticalStrut(20));
        innerP.add(new FieldLabel("SESI WAKTU LAYANAN"));
        innerP.add(Box.createVerticalStrut(8));

        String[] sesiOptions = {
                SesiLayanan.PAGI.getLabel(),
                SesiLayanan.SIANG.getLabel()
        };
        sesiComboBox = new StyledComboBox<>(sesiOptions);
        sesiComboBox.setSelectedItem(SesiLayanan.SIANG.getLabel()); // Default SIANG
        innerP.add(sesiComboBox);

        sesiError = new ErrorLabel();
        innerP.add(Box.createVerticalStrut(4));
        innerP.add(sesiError);

        // Panel Lokasi
        innerP.add(Box.createVerticalStrut(16));
        RoundedPanel lokasiPanel = new RoundedPanel(new BorderLayout(8, 4), Theme.INPUT_BACKGROUND, Theme.CARD_BORDER, 6);
        lokasiPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel lokasiHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        lokasiHeader.setOpaque(false);
        lokasiHeader.add(new JLabel(new PinIcon(14, Theme.TEXT_PRIMARY)));
        JLabel lokasiTitle = new JLabel("LOKASI PENYERAHAN");
        lokasiTitle.setFont(Fonts.trackedSans(11.5f, Font.BOLD, 0.10));
        lokasiTitle.setForeground(Theme.TEXT_PRIMARY);
        lokasiHeader.add(lokasiTitle);

        String lokasiStr = (pos != null) ? pos.getLokasiPenyerahan() : "Pos Kustodi Sentral - Rektorat Sayap Barat Lt. 1";
        JLabel lokasiDesc = new JLabel(lokasiStr);
        lokasiDesc.setFont(Fonts.sans(12.5f, Font.PLAIN));
        lokasiDesc.setForeground(Theme.TEXT_PRIMARY);

        lokasiPanel.add(lokasiHeader, BorderLayout.NORTH);
        lokasiPanel.add(lokasiDesc, BorderLayout.CENTER);
        innerP.add(lokasiPanel);

        // Dua Checkbox
        innerP.add(Box.createVerticalStrut(18));
        cbIdentitas = new StyledCheckBox("Saya membawa identitas asli (KTM/KTP) saat pengambilan fisik.");
        innerP.add(cbIdentitas);

        innerP.add(Box.createVerticalStrut(8));
        cbKepemilikan = new StyledCheckBox("Saya menyatakan bahwa barang ini sah milik saya pribadi.");
        innerP.add(cbKepemilikan);

        checkboxError = new ErrorLabel();
        innerP.add(Box.createVerticalStrut(4));
        innerP.add(checkboxError);

        card.add(innerP, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel createBottomActionBar() {
        RoundedPanel bar = new RoundedPanel(new BorderLayout(), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        bar.setPreferredSize(new Dimension(Theme.MAX_CONTENT_WIDTH, 58));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Kiri: ShieldIcon 14 + teks "Verifikasi tanpa sengketa dengan Berita Acara Serah Terima resmi." (12.5)
        bottomBarLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 18));
        bottomBarLeft.setOpaque(false);
        bottomBarLeft.add(new JLabel(new ShieldIcon(14, Theme.TEXT_SECONDARY)));
        shieldText = new JLabel("Verifikasi tanpa sengketa dengan Berita Acara Serah Terima resmi.");
        shieldText.setFont(Fonts.sans(12.5f, Font.PLAIN));
        shieldText.setForeground(Theme.TEXT_SECONDARY);
        bottomBarLeft.add(shieldText);

        // Kanan: Tombol "KEMBALI" + jarak 32 + FlatButton "KIRIM PENGAJUAN KLAIM"
        bottomBarRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        bottomBarRight.setOpaque(false);

        JLabel kembaliBtn = new JLabel("KEMBALI");
        kembaliBtn.setFont(Fonts.trackedSans(11.5f, Font.BOLD, 0.12));
        kembaliBtn.setForeground(Theme.TEXT_PRIMARY);
        kembaliBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        kembaliBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.navigateBack();
            }
        });
        bottomBarRight.add(kembaliBtn);

        JPanel gap = new JPanel();
        gap.setOpaque(false);
        gap.setPreferredSize(new Dimension(32, 1));
        bottomBarRight.add(gap);

        submitButton = new FlatButton("KIRIM PENGAJUAN KLAIM", new ArrowRightIcon(14, Color.WHITE));
        submitButton.setPreferredSize(new Dimension(228, 38));
        submitButton.setRadius(4);
        submitButton.addActionListener(e -> submitKlaim());
        bottomBarRight.add(submitButton);

        bar.add(bottomBarLeft, BorderLayout.WEST);
        bar.add(bottomBarRight, BorderLayout.EAST);
        return bar;
    }

    private JComponent createNumberCircle(String number) {
        return new JComponent() {
            {
                setPreferredSize(new Dimension(22, 22));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.NUMBER_CIRCLE_BG);
                g2.fillOval(0, 0, 22, 22);

                g2.setColor(Color.WHITE);
                g2.setFont(Fonts.sans(11f, Font.BOLD));
                int tw = g2.getFontMetrics().stringWidth(number);
                int th = g2.getFontMetrics().getAscent();
                g2.drawString(number, (22 - tw) / 2, (22 + th) / 2 - 2);
                g2.dispose();
            }
        };
    }

    private LocalDate getInitialServiceDate() {
        LocalDate date = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        if (now.isAfter(SesiLayanan.SIANG.getMulai())) {
            date = date.plusDays(1);
        }

        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            date = date.plusDays(2);
        } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private void clearAllErrors() {
        identitasField.setHasError(false);
        identitasError.clearError();

        ciriKhususArea.setHasError(false);
        ciriKhususError.clearError();

        buktiPicker.setHasError(false);
        buktiError.clearError();

        tanggalField.setHasError(false);
        tanggalError.clearError();

        sesiComboBox.setHasError(false);
        sesiError.clearError();

        cbIdentitas.setHasError(false);
        cbKepemilikan.setHasError(false);
        checkboxError.clearError();
    }

    private void submitKlaim() {
        clearAllErrors();

        String nomorIdentitas = identitasField.getText().trim();
        String ciriKhusus = ciriKhususArea.getText().trim();
        File buktiFile = buktiPicker.getSelectedFile();
        LocalDate tanggal = tanggalField.getDate();
        SesiLayanan sesi = (sesiComboBox.getSelectedIndex() == 0) ? SesiLayanan.PAGI : SesiLayanan.SIANG;
        boolean identitasChecked = cbIdentitas.isSelected();
        boolean kepemilikanChecked = cbKepemilikan.isSelected();

        ValidationResult vIdentitas = KlaimValidator.validateNomorIdentitas(nomorIdentitas);
        ValidationResult vCiri = KlaimValidator.validateCiriKhusus(ciriKhusus);
        ValidationResult vBukti = KlaimValidator.validateBukti(buktiFile);
        ValidationResult vTanggal = KlaimValidator.validateTanggal(tanggal, clock);
        ValidationResult vSesi = KlaimValidator.validateSesi(tanggal, sesi, clock);
        ValidationResult vCb = KlaimValidator.validateCheckbox(identitasChecked, kepemilikanChecked);

        JComponent firstInvalidField = null;

        if (!vIdentitas.isValid()) {
            identitasField.setHasError(true);
            identitasError.setError(vIdentitas.getFirstError());
            if (firstInvalidField == null) firstInvalidField = identitasField;
        }

        if (!vCiri.isValid()) {
            ciriKhususArea.setHasError(true);
            ciriKhususError.setError(vCiri.getFirstError());
            if (firstInvalidField == null) firstInvalidField = ciriKhususArea;
        }

        if (!vBukti.isValid()) {
            buktiPicker.setHasError(true);
            buktiError.setError(vBukti.getFirstError());
            if (firstInvalidField == null) firstInvalidField = buktiPicker;
        }

        if (!vTanggal.isValid()) {
            tanggalField.setHasError(true);
            tanggalError.setError(vTanggal.getFirstError());
            if (firstInvalidField == null) firstInvalidField = tanggalField;
        } else if (!vSesi.isValid()) {
            sesiComboBox.setHasError(true);
            sesiError.setError(vSesi.getFirstError());
            if (firstInvalidField == null) firstInvalidField = sesiComboBox;
        }

        if (!vCb.isValid()) {
            cbIdentitas.setHasError(!identitasChecked);
            cbKepemilikan.setHasError(!kepemilikanChecked);
            checkboxError.setError("Centang pernyataan ini untuk melanjutkan.");
            if (firstInvalidField == null) firstInvalidField = cbIdentitas;
        }

        if (firstInvalidField != null) {
            firstInvalidField.requestFocusInWindow();
            firstInvalidField.scrollRectToVisible(firstInvalidField.getBounds());
            return;
        }

        submitButton.setEnabled(false);

        String namaPengklaim = appSession.isLoggedIn() ? appSession.getNama() : "Pengklaim " + nomorIdentitas;
        appSession.setIdentitas(nomorIdentitas, namaPengklaim);

        SwingWorker<com.campusfind.model.Klaim, Void> worker = new SwingWorker<>() {
            @Override
            protected com.campusfind.model.Klaim doInBackground() {
                return klaimService.ajukan(
                        item.getId(),
                        nomorIdentitas,
                        namaPengklaim,
                        ciriKhusus,
                        buktiFile,
                        tanggal,
                        sesi,
                        identitasChecked,
                        kepemilikanChecked
                );
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                try {
                    com.campusfind.model.Klaim klaim = get();
                    controller.showSuccessDialog(
                            "Pengajuan klaim berhasil dikirim! Silakan datang sesuai jadwal yang telah ditentukan.",
                            klaim.getId()
                    );
                    controller.navigateToTemuan();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof KlaimGandaException || cause instanceof BarangTidakTersediaException) {
                        JOptionPane.showMessageDialog(
                                KlaimPage.this,
                                cause.getMessage() + "\n\nSistem akan mengalihkan ke Katalog Arsip untuk menyegarkan daftar barang.",
                                "Barang Tidak Tersedia",
                                JOptionPane.WARNING_MESSAGE
                        );
                        controller.navigateToTemuan();
                    } else {
                        checkboxError.setError(cause.getMessage());
                    }
                }
            }
        };

        worker.execute();
    }
}
