package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.StatusItem;
import com.campusfind.service.ItemService;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.components.FooterBar;
import com.campusfind.ui.components.ItemSummaryCard;
import com.campusfind.ui.components.PageHeader;
import com.campusfind.ui.components.PlaceholderTextField;
import com.campusfind.ui.components.ResponsivePage;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemuanPage extends ResponsivePage {

    private final NavigationController controller;
    private final ItemService itemService;
    private final PosService posService;

    private final PageHeader header;
    private final RoundedPanel searchFilterCard;
    private PlaceholderTextField searchField;
    private JPanel filterPanel;
    private JRadioButton rbSemua;
    private JRadioButton rbTemuan;
    private JRadioButton rbHilang;
    private final JPanel listContainer;
    private final FooterBar footerBar;
    private boolean isSearchStacked = false;

    private Map<String, String> posNameMap;
    private List<Item> currentRenderedItems = new ArrayList<>();

    public TemuanPage(NavigationController controller, ItemService itemService, PosService posService) {
        super(Theme.MAX_CONTENT_WIDTH);
        this.controller = controller;
        this.itemService = itemService;
        this.posService = posService;

        // 1. Reusable Header (Eyebrow, Title, Subtitle all strictly centered)
        header = new PageHeader(
                "KATALOG ARSIP",
                "Barang Temuan",
                "Daftar arsip barang temuan dan kehilangan di kawasan kampus yang terdata di sistem rekonsiliasi."
        );
        addContent(header);

        // 2. Filter & Search Bar
        addContentStrut(24);
        searchFilterCard = createSearchAndFilterBar();
        searchFilterCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(searchFilterCard);

        // 3. List Container
        addContentStrut(24);
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(listContainer);

        // 4. Footer
        addContentStrut(40);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);

        loadDataAsync();
    }

    private RoundedPanel createSearchAndFilterBar() {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(16, 0), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        panel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // Search Input
        searchField = new PlaceholderTextField("Cari berdasarkan nama, kode (#ARC-xxxx), atau lokasi...");
        searchField.setPreferredSize(new Dimension(500, Theme.INPUT_HEIGHT));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilterAndSearch();
            }
        });

        // Filter Radio Buttons: "Semua | Temuan | Hilang"
        filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        filterPanel.setOpaque(false);

        rbSemua = new JRadioButton("Semua", true);
        rbTemuan = new JRadioButton("Temuan");
        rbHilang = new JRadioButton("Hilang");

        Font f = Fonts.sans(13f, Font.PLAIN);
        for (JRadioButton rb : List.of(rbSemua, rbTemuan, rbHilang)) {
            rb.setFont(f);
            rb.setForeground(Theme.TEXT_PRIMARY);
            rb.setOpaque(false);
            rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rb.addActionListener(e -> applyFilterAndSearch());
        }

        ButtonGroup group = new ButtonGroup();
        group.add(rbSemua);
        group.add(rbTemuan);
        group.add(rbHilang);

        filterPanel.add(rbSemua);
        filterPanel.add(new JLabel("|"));
        filterPanel.add(rbTemuan);
        filterPanel.add(new JLabel("|"));
        filterPanel.add(rbHilang);

        panel.add(searchField, BorderLayout.CENTER);
        panel.add(filterPanel, BorderLayout.EAST);
        return panel;
    }

    @Override
    protected void onContentWidthChanged(int contentWidth) {
        header.updateWidth(contentWidth);
        footerBar.updateResponsive(contentWidth);

        // Search & Filter stacking based on width
        boolean shouldStackSearch = contentWidth < Theme.BREAKPOINT_NAV_COLLAPSE;
        if (shouldStackSearch != isSearchStacked) {
            isSearchStacked = shouldStackSearch;
            searchFilterCard.removeAll();

            if (isSearchStacked) {
                searchFilterCard.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
                filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
                searchFilterCard.add(searchField);
                searchFilterCard.add(filterPanel);
                searchFilterCard.setMaximumSize(new Dimension(contentWidth, 96));
            } else {
                searchFilterCard.setLayout(new BorderLayout(16, 0));
                filterPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 10));
                searchFilterCard.add(searchField, BorderLayout.CENTER);
                searchFilterCard.add(filterPanel, BorderLayout.EAST);
                searchFilterCard.setMaximumSize(new Dimension(contentWidth, 60));
            }
            searchFilterCard.revalidate();
            searchFilterCard.repaint();
        } else {
            int cardH = isSearchStacked ? 96 : 60;
            searchFilterCard.setMaximumSize(new Dimension(contentWidth, cardH));
        }

        renderItems(currentRenderedItems, contentWidth);
        revalidate();
        repaint();
    }

    private void loadDataAsync() {
        SwingWorker<List<Item>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Item> doInBackground() {
                posNameMap = posService.findAll().stream()
                        .collect(Collectors.toMap(PosPenyimpanan::getId, PosPenyimpanan::getNama, (a, b) -> a));
                return itemService.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Item> items = get();
                    currentRenderedItems = items;
                    renderItems(items, getCurrentContentWidth());
                } catch (Exception ignored) {
                    renderEmpty();
                }
            }
        };
        worker.execute();
    }

    private void applyFilterAndSearch() {
        String query = searchField.getText().trim();
        List<Item> items = itemService.search(query);

        if (rbTemuan.isSelected()) {
            items = items.stream().filter(i -> i.getJenis() == JenisLaporan.TEMUAN).collect(Collectors.toList());
        } else if (rbHilang.isSelected()) {
            items = items.stream().filter(i -> i.getJenis() == JenisLaporan.HILANG).collect(Collectors.toList());
        }

        currentRenderedItems = items;
        renderItems(items, getCurrentContentWidth());
    }

    private void renderItems(List<Item> items, int contentWidth) {
        listContainer.removeAll();

        if (items == null || items.isEmpty()) {
            renderEmpty();
            return;
        }

        boolean narrowScreen = contentWidth < 740;

        for (Item item : items) {
            String posName = posNameMap != null ? posNameMap.get(item.getPosId()) : null;
            if (posName == null) posName = "-";

            ItemSummaryCard card = new ItemSummaryCard(item, posName);

            // Action component: button or status label
            JPanel actionPanel = new JPanel();
            actionPanel.setOpaque(false);

            if (item.getJenis() == JenisLaporan.TEMUAN && item.getStatus() == StatusItem.TERSEDIA) {
                FlatButton klaimBtn = new FlatButton("KLAIM BARANG");
                klaimBtn.setPreferredSize(new Dimension(126, 34));
                klaimBtn.setRadius(4);
                klaimBtn.addActionListener(e -> controller.navigateToKlaim(item.getId()));
                actionPanel.add(klaimBtn);
            } else if (item.getJenis() == JenisLaporan.HILANG) {
                FlatButton kandidatBtn = new FlatButton("KANDIDAT COCOK");
                kandidatBtn.setPreferredSize(new Dimension(140, 34));
                kandidatBtn.setRadius(4);
                kandidatBtn.addActionListener(e -> controller.showKandidatCocok(item));
                actionPanel.add(kandidatBtn);
            } else {
                JLabel statusChip = new JLabel(item.getStatus().getLabel());
                statusChip.setFont(Fonts.sans(11.5f, Font.BOLD));
                statusChip.setForeground(Theme.TEXT_SECONDARY);
                actionPanel.add(statusChip);
            }

            JPanel itemRow;
            if (narrowScreen) {
                // Stack vertically so summary card is never squished
                itemRow = new JPanel();
                itemRow.setLayout(new BoxLayout(itemRow, BoxLayout.Y_AXIS));
                itemRow.setOpaque(false);
                itemRow.setAlignmentX(Component.CENTER_ALIGNMENT);

                card.setPreferredSize(new Dimension(contentWidth, 108));
                itemRow.add(card);

                JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
                actionRow.setOpaque(false);
                actionRow.add(actionPanel);
                itemRow.add(actionRow);
            } else {
                // Side by side
                itemRow = new JPanel(new BorderLayout());
                itemRow.setOpaque(false);
                itemRow.setAlignmentX(Component.CENTER_ALIGNMENT);
                itemRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));
                itemRow.add(card, BorderLayout.CENTER);

                actionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 36));
                actionPanel.setPreferredSize(new Dimension(150, 108));
                itemRow.add(actionPanel, BorderLayout.EAST);
            }

            listContainer.add(itemRow);
            listContainer.add(Box.createVerticalStrut(14));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void renderEmpty() {
        listContainer.removeAll();

        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 60, 0));

        JLabel emptyLabel = new JLabel("Belum ada laporan barang. Tekan Lapor Barang untuk menambahkan.");
        emptyLabel.setFont(Fonts.sans(14f, Font.PLAIN));
        emptyLabel.setForeground(Theme.TEXT_MUTED);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        FlatButton laporBtn = new FlatButton("LAPOR BARANG SEKARANG");
        laporBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        laporBtn.addActionListener(e -> controller.navigateToLapor());

        emptyPanel.add(emptyLabel);
        emptyPanel.add(Box.createVerticalStrut(20));
        emptyPanel.add(laporBtn);

        listContainer.add(emptyPanel);
        listContainer.revalidate();
        listContainer.repaint();
    }
}
