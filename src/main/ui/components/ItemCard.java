package com.campusfind.ui.components;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.StatusItem;
import com.campusfind.ui.theme.Fonts;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Komponen kartu item vertikal ("Card Grid") sesuai desain Figma Galeri Barang.
 * Menampilkan:
 * 1. Gambar item berukuran besar di bagian atas dengan badge jenis & status di atasnya.
 * 2. Judul nama barang tebal.
 * 3. Baris lokasi kejadian dan tanggal dengan ikon pin.
 * 4. Tombol aksi responsif di bagian bawah ("Ini barang saya", "Saya melihat ini",
 *    "Sudah dikembalikan", atau "Sedang diproses").
 */
public class ItemCard extends JPanel {

    public static final int CARD_WIDTH = 268;
    public static final int CARD_HEIGHT = 385;
    private static final int IMAGE_HEIGHT = 225;
    private static final int CORNER_RADIUS = 14;

    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(0xE2, 0xE8, 0xF0);
    private static final Color CARD_HOVER_BORDER = new Color(0x81, 0x8C, 0xF8);

    private static final Color PRIMARY_BTN_BG = new Color(0x4F, 0x46, 0xE5); // Indigo
    private static final Color PRIMARY_BTN_HOVER = new Color(0x43, 0x38, 0xCA);
    private static final Color OUTLINE_BTN_BORDER = new Color(0x4F, 0x46, 0xE5);
    private static final Color OUTLINE_BTN_TEXT = new Color(0x4F, 0x46, 0xE5);
    private static final Color DISABLED_BTN_BG = new Color(0xF1, 0xF5, 0xF9);
    private static final Color DISABLED_BTN_TEXT = new Color(0x94, 0xA3, 0xB8);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM", new Locale("id", "ID"));

    private final Item item;
    private final String posNama;
    private final Consumer<Item> onClaimAction;
    private final Consumer<Item> onSightingAction;

    private BufferedImage cachedImage;
    private boolean isHovered = false;

    public ItemCard(Item item,
                    String posNama,
                    Consumer<Item> onClaimAction,
                    Consumer<Item> onSightingAction) {
        this.item = item;
        this.posNama = posNama != null ? posNama : "-";
        this.onClaimAction = onClaimAction;
        this.onSightingAction = onSightingAction;

        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setLayout(new BorderLayout());
        setOpaque(false);

        loadThumbnail();
        initContentUI();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    private void loadThumbnail() {
        if (item == null) {
            return;
        }
        try {
            BufferedImage original = null;
            if (item.hasFotoBytes()) {
                original = ImageIO.read(new ByteArrayInputStream(item.getFotoBytes()));
            } else if (item.getFotoPath() != null) {
                File file = new File(item.getFotoPath());
                if (!file.exists()) {
                    file = new File("data" + File.separator + item.getFotoPath());
                }
                if (file.exists()) {
                    original = ImageIO.read(file);
                }
            }
            if (original != null) {
                this.cachedImage = createCropToFill(original, CARD_WIDTH, IMAGE_HEIGHT);
            }
        } catch (Exception ignored) {
            this.cachedImage = null;
        }
    }

    private BufferedImage createCropToFill(BufferedImage src, int targetW, int targetH) {
        BufferedImage dest = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double srcW = src.getWidth();
        double srcH = src.getHeight();
        double scale = Math.max((double) targetW / srcW, (double) targetH / srcH);
        int scaledW = (int) Math.round(srcW * scale);
        int scaledH = (int) Math.round(srcH * scale);

        int dx = (targetW - scaledW) / 2;
        int dy = (targetH - scaledH) / 2;

        g2.drawImage(src, dx, dy, scaledW, scaledH, null);
        g2.dispose();
        return dest;
    }

    private void initContentUI() {
        // Area bawah gambar: padding 14px
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(null);
        bottomPanel.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT - IMAGE_HEIGHT));

        // 1. Title (Nama Barang)
        String title = (item != null) ? item.getNama() : "Nama Barang";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Fonts.sans(15f, Font.BOLD));
        titleLabel.setForeground(new Color(0x0F, 0x17, 0x2A));
        titleLabel.setBounds(14, 12, CARD_WIDTH - 28, 22);
        bottomPanel.add(titleLabel);

        // 2. Subtitle: Lokasi & Tanggal
        String lokasi = (item != null && item.getLokasiKejadian() != null) ? item.getLokasiKejadian() : "-";
        String tanggal = "";
        if (item != null && item.getTanggalKejadian() != null) {
            tanggal = " · " + item.getTanggalKejadian().format(DATE_FORMAT);
        }
        JLabel subLabel = new JLabel("📍 " + lokasi + tanggal);
        subLabel.setFont(Fonts.sans(12f, Font.PLAIN));
        subLabel.setForeground(new Color(0x64, 0x74, 0x8B));
        subLabel.setBounds(14, 38, CARD_WIDTH - 28, 18);
        bottomPanel.add(subLabel);

        // 3. Tombol Aksi di bagian paling bawah
        JButton actionBtn = createActionButton();
        actionBtn.setBounds(14, 88, CARD_WIDTH - 28, 40);
        bottomPanel.add(actionBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createActionButton() {
        if (item == null) {
            JButton btn = new JButton("Detail");
            btn.setEnabled(false);
            return btn;
        }

        StatusItem status = item.getStatus();
        JenisLaporan jenis = item.getJenis();

        // 1. Jika barang sudah dikembalikan
        if (status == StatusItem.DIKEMBALIKAN || status == StatusItem.DISERAHKAN) {
            return makePillButton("Sudah dikembalikan", DISABLED_BTN_BG, DISABLED_BTN_TEXT, false, null);
        }

        // 2. Jika barang sedang dalam proses klaim
        if (status == StatusItem.DIKLAIM || status == StatusItem.DALAM_PROSES_KLAIM) {
            return makePillButton("Sedang diproses", DISABLED_BTN_BG, DISABLED_BTN_TEXT, false, null);
        }

        // 3. Jika barang Temuan -> tombol "Ini barang saya" (Primary Purple/Indigo)
        if (jenis == JenisLaporan.TEMUAN) {
            return makePillButton("Ini barang saya", PRIMARY_BTN_BG, Color.WHITE, true, e -> {
                if (onClaimAction != null) {
                    onClaimAction.accept(item);
                }
            });
        }

        // 4. Jika barang Hilang -> tombol "Saya melihat ini" (White Outline with Indigo border)
        return makeOutlineButton("Saya melihat ini", e -> {
            if (onSightingAction != null) {
                onSightingAction.accept(item);
            }
        });
    }

    private JButton makePillButton(String text, Color bg, Color fg, boolean enabled, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg = bg;
                if (enabled && getModel().isRollover()) {
                    currentBg = PRIMARY_BTN_HOVER;
                }
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2.setFont(Fonts.sans(13f, Font.BOLD));
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setEnabled(enabled);
        if (enabled) {
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (al != null) {
                btn.addActionListener(al);
            }
        }
        return btn;
    }

    private JButton makeOutlineButton(String text, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg = getModel().isRollover() ? new Color(0xEE, 0xF2, 0xFF) : Color.WHITE;
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2.setColor(OUTLINE_BTN_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2.setFont(Fonts.sans(13f, Font.BOLD));
                g2.setColor(OUTLINE_BTN_TEXT);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (al != null) {
            btn.addActionListener(al);
        }
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Background Card Putih
        g2.setColor(CARD_BG);
        g2.fillRoundRect(0, 0, w, h, CORNER_RADIUS, CORNER_RADIUS);

        // 2. Gambar Thumbnail di bagian atas
        Graphics2D gImg = (Graphics2D) g2.create();
        // Clip hanya sudut atas kartu
        gImg.setClip(new RoundRectangle2D.Float(0, 0, w, IMAGE_HEIGHT + CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS));
        if (cachedImage != null) {
            gImg.drawImage(cachedImage, 0, 0, w, IMAGE_HEIGHT, null);
        } else {
            // Placeholder gradien abu-abu jika tidak ada gambar
            gImg.setColor(new Color(0xED, 0xF2, 0xF7));
            gImg.fillRect(0, 0, w, IMAGE_HEIGHT);

            gImg.setColor(new Color(0xA0, 0xAE, 0xC0));
            gImg.setStroke(new BasicStroke(2.0f));
            int cx = w / 2;
            int cy = IMAGE_HEIGHT / 2;
            gImg.drawRoundRect(cx - 24, cy - 16, 48, 36, 6, 6);
            gImg.drawOval(cx - 10, cy - 6, 20, 20);
        }
        gImg.dispose();

        // 3. Overlay Badges di atas gambar
        paintBadges(g2);

        // 4. Border Kartu
        g2.setColor(isHovered ? CARD_HOVER_BORDER : CARD_BORDER);
        g2.setStroke(new BasicStroke(isHovered ? 1.5f : 1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS, CORNER_RADIUS);

        g2.dispose();
    }

    private void paintBadges(Graphics2D g2) {
        if (item == null) {
            return;
        }

        // a) Badge Jenis (Top-Left)
        boolean isTemuan = item.getJenis() == JenisLaporan.TEMUAN;
        String jenisText = isTemuan ? "🤝 Temuan" : "❓ Hilang";
        Color jenisBg = isTemuan ? new Color(0x05, 0x96, 0x69) : new Color(0xDC, 0x26, 0x26); // Emerald vs Red
        drawPillBadge(g2, 12, 12, jenisText, jenisBg, Color.WHITE);

        // b) Badge Status (Top-Right)
        StatusItem status = item.getStatus();
        String statusText;
        Color statusBg;
        Color statusFg;

        switch (status) {
            case COCOK -> {
                statusText = "Cocok";
                statusBg = new Color(0xEE, 0xF2, 0xFF);
                statusFg = new Color(0x4F, 0x46, 0xE5); // Indigo
            }
            case DIVERIFIKASI -> {
                statusText = "Diverifikasi";
                statusBg = new Color(0xE0, 0xF2, 0xFE);
                statusFg = new Color(0x02, 0x84, 0xC7); // Sky Blue
            }
            case DIKLAIM, DALAM_PROSES_KLAIM -> {
                statusText = "Diklaim";
                statusBg = new Color(0xFE, 0xF3, 0xC7);
                statusFg = new Color(0xD9, 0x77, 0x06); // Amber
            }
            case DIKEMBALIKAN, DISERAHKAN -> {
                statusText = "Dikembalikan";
                statusBg = new Color(0xD1, 0xFA, 0xE5);
                statusFg = new Color(0x05, 0x96, 0x69); // Emerald
            }
            default -> {
                statusText = "Dilaporkan";
                statusBg = new Color(0xF1, 0xF5, 0xF9);
                statusFg = new Color(0x47, 0x55, 0x69); // Slate
            }
        }

        Font badgeFont = Fonts.sans(11f, Font.BOLD);
        g2.setFont(badgeFont);
        FontMetrics fm = g2.getFontMetrics();
        int badgeW = fm.stringWidth(statusText) + 18;
        int badgeX = getWidth() - 12 - badgeW;

        drawPillBadge(g2, badgeX, 12, statusText, statusBg, statusFg);
    }

    private void drawPillBadge(Graphics2D g2, int x, int y, String text, Color bg, Color fg) {
        Font font = Fonts.sans(11.5f, Font.BOLD);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int padH = 10;
        int textW = fm.stringWidth(text);
        int badgeW = textW + (padH * 2);
        int badgeH = 24;

        g2.setColor(bg);
        g2.fillRoundRect(x, y, badgeW, badgeH, 12, 12);

        g2.setColor(fg);
        int textX = x + padH;
        int textY = y + (badgeH - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, textX, textY);
    }

    public Item getItem() {
        return item;
    }
}
