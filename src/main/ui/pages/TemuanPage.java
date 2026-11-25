package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.StatusItem;
import com.campusfind.service.ItemService;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.components.FooterBar;
import com.campusfind.ui.components.ItemCard;
import com.campusfind.ui.components.PlaceholderTextField;
import com.campusfind.ui.components.ResponsivePage;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Halaman Galeri Barang ("Card Grid") sesuai desain Figma.
 * Menampilkan:
 * 1. Header: Judul "Galeri Barang", Subtitle, dan Search Bar di sebelah kanan.
 * 2. Bar Kategori (Semua, Elektronik, Non-Elektronik).
 * 3. Bar Filter Tipe (Semua | Hilang | Temuan), Lokasi, Pengurutan, dan Total Barang.
 * 4. Grid Kartu Item (ItemCard) responsif (4 kolom pada layar standar).
 */
public class TemuanPage extends ResponsivePage {

    private final NavigationController controller;
    private final ItemService itemService;
    private final PosService posService;

    // Komponen Header
    private JPanel headerPanel;
    private PlaceholderTextField searchField;

    // Filter Kategori
    private JPanel categoryPillsPanel;
    private final List<JButton> categoryButtons = new ArrayList<>();
    private String selectedCategory = "SEMUA";

    // Filter Sub-row
    private JPanel filterRowPanel;
    private JButton btnFilterSemua;
    private JButton btnFilterHilang;
    private JButton btnFilterTemuan;
    private String selectedTypeFilter = "SEMUA"; // "SEMUA", "HILANG", "TEMUAN"

    private JComboBox<String> locationCombo;
    private JComboBox<String> sortCombo;
    private JCheckBox cbSembunyikanDiklaim;
    private JLabel countLabel;

    // Grid Container Kartu
    private final JPanel cardsGridContainer;
    private final FooterBar footerBar;

    private Map<String, String> posNameMap;
    private List<Item> rawItems = new ArrayList<>();
    private List<Item> currentRenderedItems = new ArrayList<>();

    public TemuanPage(NavigationController controller, ItemService itemService, PosService posService) {
        super(Theme.MAX_CONTENT_WIDTH);
        this.controller = controller;
        this.itemService = itemService;
        this.posService = posService;

        // 1. Header Row (Title di kiri, Search Bar di kanan)
        headerPanel = createHeaderPanel();
        addContent(headerPanel);
        addContentStrut(18);

        // 2. Category Pills (Semua, Elektronik, Dompet & Dokumen, Tas, Pakaian, Lainnya)
        categoryPillsPanel = createCategoryPillsPanel();
        addContent(categoryPillsPanel);
        addContentStrut(14);

        // 3. Sub-filter Row (Tipe [Semua|Hilang|Temuan], Lokasi, Sort, Total Barang)
        filterRowPanel = createSubFilterRow();
        addContent(filterRowPanel);
        addContentStrut(24);

        // 4. Cards Grid Container
        cardsGridContainer = new JPanel();
        cardsGridContainer.setOpaque(false);
        cardsGridContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(cardsGridContainer);

        // 5. Footer Bar
        addContentStrut(40);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);

        loadDataAsync();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 8));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sisi Kiri: Judul dan Deskripsi
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel("Galeri Barang");
        titleLabel.setFont(Fonts.sans(26f, Font.BOLD));
        titleLabel.setForeground(new Color(0x0F, 0x17, 0x2A));

        JLabel subLabel = new JLabel("Telusuri barang hilang dan temuan di kampus.");
        subLabel.setFont(Fonts.sans(14f, Font.PLAIN));
        subLabel.setForeground(new Color(0x64, 0x74, 0x8B));

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(subLabel);
        panel.add(left, BorderLayout.WEST);

        // Sisi Kanan: Search Bar Pill
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);

        searchField = new PlaceholderTextField("🔍  Cari barang, warna, atau lokasi...");
        searchField.setPreferredSize(new Dimension(340, 42));
        searchField.setFont(Fonts.sans(13.5f, Font.PLAIN));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new com.campusfind.ui.components.RoundedBorder(new Color(0xCB, 0xD5, 0xE1), 20, 1.0f),
                BorderFactory.createEmptyBorder(0, 16, 0, 16)
        ));
        searchField.setBackground(Color.WHITE);
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilterAndSearch();
            }
        });
        right.add(searchField);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCategoryPillsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[][] categories = {
                {"SEMUA", "Semua"},
                {"ELEKTRONIK", "💻 Elektronik"},
                {"NON_ELEKTRONIK", "📦 Non-Elektronik"}
        };

        for (String[] cat : categories) {
            String key = cat[0];
            String label = cat[1];
            JButton pill = createCategoryButton(key, label);
            categoryButtons.add(pill);
            panel.add(pill);
        }

        updateCategoryButtonsState();
        return panel;
    }

    private JButton createCategoryButton(String key, String label) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isSelected = key.equals(selectedCategory);
                Color bg = isSelected ? new Color(0x1E, 0x1B, 0x4B) : Color.WHITE;
                Color fg = isSelected ? Color.WHITE : new Color(0x33, 0x41, 0x55);
                Color border = isSelected ? new Color(0x1E, 0x1B, 0x4B) : new Color(0xE2, 0xE8, 0xF0);

                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2.setFont(Fonts.sans(13f, isSelected ? Font.BOLD : Font.PLAIN));
                g2.setColor(fg);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(btn.getFontMetrics(Fonts.sans(13f, Font.PLAIN)).stringWidth(label) + 32, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            selectedCategory = key;
            updateCategoryButtonsState();
            applyFilterAndSearch();
        });
        return btn;
    }

    private void updateCategoryButtonsState() {
        for (JButton btn : categoryButtons) {
            btn.repaint();
        }
    }

    private JPanel createSubFilterRow() {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Bagian Kiri: Type Pill (Semua, Hilang, Temuan) + Dropdown Lokasi + Sort
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftGroup.setOpaque(false);

        // Segmented Control Pill (Semua, Hilang, Temuan)
        JPanel typeSegment = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 3));
        typeSegment.setBackground(new Color(0xF1, 0xF5, 0xF9));
        typeSegment.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        btnFilterSemua = createTypeSegmentButton("Semua", "SEMUA");
        btnFilterHilang = createTypeSegmentButton("Hilang", "HILANG");
        btnFilterTemuan = createTypeSegmentButton("Temuan", "TEMUAN");

        typeSegment.add(btnFilterSemua);
        typeSegment.add(btnFilterHilang);
        typeSegment.add(btnFilterTemuan);
        leftGroup.add(typeSegment);

        // Dropdown Semua Lokasi
        locationCombo = new JComboBox<>(new String[]{"Semua lokasi"});
        locationCombo.setPreferredSize(new Dimension(160, 36));
        locationCombo.setFont(Fonts.sans(12.5f, Font.PLAIN));
        locationCombo.setBackground(Color.WHITE);
        locationCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        locationCombo.addActionListener(e -> applyFilterAndSearch());
        leftGroup.add(locationCombo);

        // Dropdown Sort
        sortCombo = new JComboBox<>(new String[]{"Terbaru", "Terlama", "Nama (A-Z)"});
        sortCombo.setPreferredSize(new Dimension(120, 36));
        sortCombo.setFont(Fonts.sans(12.5f, Font.PLAIN));
        sortCombo.setBackground(Color.WHITE);
        sortCombo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sortCombo.addActionListener(e -> applyFilterAndSearch());
        leftGroup.add(sortCombo);

        // Checkbox: Sembunyikan yang sudah diklaim / selesai
        cbSembunyikanDiklaim = new JCheckBox("Sembunyikan yang sudah diklaim", true);
        cbSembunyikanDiklaim.setFont(Fonts.sans(12f, Font.PLAIN));
        cbSembunyikanDiklaim.setForeground(new Color(0x47, 0x55, 0x69));
        cbSembunyikanDiklaim.setOpaque(false);
        cbSembunyikanDiklaim.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cbSembunyikanDiklaim.addActionListener(e -> applyFilterAndSearch());
        leftGroup.add(cbSembunyikanDiklaim);

        row.add(leftGroup, BorderLayout.WEST);

        // Bagian Kanan: Label total barang ("8 barang")
        countLabel = new JLabel("0 barang");
        countLabel.setFont(Fonts.sans(13f, Font.BOLD));
        countLabel.setForeground(new Color(0x64, 0x74, 0x8B));
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rightGroup.setOpaque(false);
        rightGroup.add(countLabel);
        row.add(rightGroup, BorderLayout.EAST);

        return row;
    }

    private JButton createTypeSegmentButton(String text, String typeKey) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isSelected = typeKey.equals(selectedTypeFilter);
                if (isSelected) {
                    g2.setColor(new Color(0x4F, 0x46, 0xE5)); // Indigo
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                g2.setFont(Fonts.sans(12.5f, isSelected ? Font.BOLD : Font.PLAIN));
                g2.setColor(isSelected ? Color.WHITE : new Color(0x47, 0x55, 0x69));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(68, 30));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            selectedTypeFilter = typeKey;
            btnFilterSemua.repaint();
            btnFilterHilang.repaint();
            btnFilterTemuan.repaint();
            applyFilterAndSearch();
        });
        return btn;
    }

    @Override
    protected void onContentWidthChanged(int contentWidth) {
        if (headerPanel != null) {
            headerPanel.setMaximumSize(new Dimension(contentWidth, 60));
        }
        if (categoryPillsPanel != null) {
            categoryPillsPanel.setMaximumSize(new Dimension(contentWidth, 42));
        }
        if (filterRowPanel != null) {
            filterRowPanel.setMaximumSize(new Dimension(contentWidth, 42));
        }
        if (footerBar != null) {
            footerBar.updateResponsive(contentWidth);
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
                    rawItems = get();
                    populateLocations(rawItems);
                    applyFilterAndSearch();
                } catch (Exception ignored) {
                    renderEmpty();
                }
            }
        };
        worker.execute();
    }

    private void populateLocations(List<Item> items) {
        if (locationCombo == null || items == null) {
            return;
        }
        locationCombo.removeAllItems();
        locationCombo.addItem("Semua lokasi");

        List<String> locations = items.stream()
                .map(Item::getLokasiKejadian)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.equals("-"))
                .distinct()
                .sorted()
                .toList();

        for (String loc : locations) {
            locationCombo.addItem(loc);
        }
    }

    private void applyFilterAndSearch() {
        String query = (searchField != null) ? searchField.getText().trim().toLowerCase() : "";
        if (query.startsWith("🔍")) {
            query = query.replace("🔍", "").trim();
        }

        List<Item> filtered = new ArrayList<>(rawItems);

        // 1. Text Search Filter
        if (!query.isBlank()) {
            final String q = query;
            filtered = filtered.stream()
                    .filter(i -> (i.getNama() != null && i.getNama().toLowerCase().contains(q))
                            || (i.getId() != null && i.getId().toLowerCase().contains(q))
                            || (i.getLokasiKejadian() != null && i.getLokasiKejadian().toLowerCase().contains(q))
                            || (i.getDeskripsi() != null && i.getDeskripsi().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        // 2. Type Filter (Semua, Hilang, Temuan)
        if ("TEMUAN".equals(selectedTypeFilter)) {
            filtered = filtered.stream().filter(i -> i.getJenis() == JenisLaporan.TEMUAN).collect(Collectors.toList());
        } else if ("HILANG".equals(selectedTypeFilter)) {
            filtered = filtered.stream().filter(i -> i.getJenis() == JenisLaporan.HILANG).collect(Collectors.toList());
        }

        // 3. Category Filter
        if (!"SEMUA".equals(selectedCategory)) {
            try {
                KategoriBarang cat = KategoriBarang.valueOf(selectedCategory);
                filtered = filtered.stream().filter(i -> i.getKategori() == cat).collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {
            }
        }

        // 4. Location Filter
        if (locationCombo != null && locationCombo.getSelectedIndex() > 0) {
            String selectedLoc = (String) locationCombo.getSelectedItem();
            if (selectedLoc != null && !selectedLoc.equals("Semua lokasi")) {
                filtered = filtered.stream()
                        .filter(i -> selectedLoc.equalsIgnoreCase(i.getLokasiKejadian()))
                        .collect(Collectors.toList());
            }
        }

        // 5. Sembunyikan barang yang sudah diklaim / selesai jika opsi aktif
        if (cbSembunyikanDiklaim != null && cbSembunyikanDiklaim.isSelected()) {
            filtered = filtered.stream()
                    .filter(i -> i.getStatus() != StatusItem.DIKLAIM
                            && i.getStatus() != StatusItem.DALAM_PROSES_KLAIM
                            && i.getStatus() != StatusItem.DIKEMBALIKAN
                            && i.getStatus() != StatusItem.DISERAHKAN)
                    .collect(Collectors.toList());
        }

        // 6. Sorting
        if (sortCombo != null) {
            int sortIdx = sortCombo.getSelectedIndex();
            if (sortIdx == 1) { // Terlama
                filtered.sort(Comparator.comparing(Item::getDibuatPada, Comparator.nullsLast(Comparator.naturalOrder())));
            } else if (sortIdx == 2) { // Nama (A-Z)
                filtered.sort(Comparator.comparing(Item::getNama, String.CASE_INSENSITIVE_ORDER));
            } else { // Terbaru
                filtered.sort(Comparator.comparing(Item::getDibuatPada, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        }

        currentRenderedItems = filtered;
        if (countLabel != null) {
            countLabel.setText(filtered.size() + " barang");
        }

        renderItems(filtered, getCurrentContentWidth());
    }

    private void renderItems(List<Item> items, int contentWidth) {
        cardsGridContainer.removeAll();

        if (items == null || items.isEmpty()) {
            renderEmpty();
            return;
        }

        // Hitung kolom grid responsif (4 kolom jika lebar >= 1120px)
        int cols = 4;
        if (contentWidth < 580) {
            cols = 1;
        } else if (contentWidth < 860) {
            cols = 2;
        } else if (contentWidth < 1120) {
            cols = 3;
        }

        int hGap = 20;
        int vGap = 24;

        JPanel gridPanel = new JPanel(new GridLayout(0, cols, hGap, vGap));
        gridPanel.setOpaque(false);

        for (Item item : items) {
            String posName = posNameMap != null ? posNameMap.get(item.getPosId()) : null;

            ItemCard card = new ItemCard(
                    item,
                    posName,
                    this::handleClaim,
                    this::handleSighting
            );
            gridPanel.add(card);
        }

        cardsGridContainer.setLayout(new BorderLayout());
        cardsGridContainer.add(gridPanel, BorderLayout.CENTER);
        cardsGridContainer.revalidate();
        cardsGridContainer.repaint();
    }

    private void handleClaim(Item item) {
        if (item == null) {
            return;
        }
        controller.navigateToKlaim(item.getId());
    }

    private void handleSighting(Item item) {
        if (item == null) {
            return;
        }
        controller.showKandidatCocok(item);
    }

    private void renderEmpty() {
        cardsGridContainer.removeAll();

        JPanel emptyPanel = new JPanel();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setOpaque(false);
        emptyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 60, 0));

        JLabel emptyLabel = new JLabel("Tidak ada barang yang sesuai dengan filter atau pencarian.");
        emptyLabel.setFont(Fonts.sans(14f, Font.PLAIN));
        emptyLabel.setForeground(Theme.TEXT_MUTED);
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        FlatButton laporBtn = new FlatButton("LAPOR BARANG SEKARANG");
        laporBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        laporBtn.addActionListener(e -> controller.navigateToLapor());

        emptyPanel.add(emptyLabel);
        emptyPanel.add(Box.createVerticalStrut(20));
        emptyPanel.add(laporBtn);

        cardsGridContainer.setLayout(new BorderLayout());
        cardsGridContainer.add(emptyPanel, BorderLayout.CENTER);
        cardsGridContainer.revalidate();
        cardsGridContainer.repaint();
    }
}
