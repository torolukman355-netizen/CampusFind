package com.campusfind.ui.components;

import com.campusfind.model.Item;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

public class ItemSummaryCard extends JPanel {

    private final Item item;
    private final String posNama;
    private BufferedImage cachedImage;

    public ItemSummaryCard(Item item, String posNama) {
        this.item = item;
        this.posNama = posNama != null ? posNama : "Pos Satpam Rektorat";
        setOpaque(false);
        setPreferredSize(new Dimension(Theme.MAX_CONTENT_WIDTH, 108));
        loadThumbnail();
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
                this.cachedImage = createCropToFill(original, 80, 80);
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

        double srcRatio = (double) src.getWidth() / src.getHeight();
        double targetRatio = (double) targetW / targetH;
        int drawW, drawH, drawX, drawY;

        if (srcRatio > targetRatio) {
            drawH = targetH;
            drawW = (int) Math.round(targetH * srcRatio);
            drawX = (targetW - drawW) / 2;
            drawY = 0;
        } else {
            drawW = targetW;
            drawH = (int) Math.round(targetW / srcRatio);
            drawX = 0;
            drawY = (targetH - drawH) / 2;
        }

        g2.drawImage(src, drawX, drawY, drawW, drawH, null);
        g2.dispose();
        return dest;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Background Card (#F4EFE6, border #E8E1D5, radius 8)
        g2.setColor(Theme.CARD_BACKGROUND);
        g2.fillRoundRect(0, 0, w, h, Theme.CARD_RADIUS, Theme.CARD_RADIUS);

        g2.setColor(Theme.CARD_BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, Theme.CARD_RADIUS, Theme.CARD_RADIUS);

        // 2. Thumbnail 80x80 (padding 14, radius 6)
        int thumbX = 14;
        int thumbY = 14;
        int thumbSize = 80;
        int thumbRadius = 6;

        Graphics2D gThumb = (Graphics2D) g2.create();
        gThumb.setClip(new RoundRectangle2D.Float(thumbX, thumbY, thumbSize, thumbSize, thumbRadius, thumbRadius));

        if (cachedImage != null) {
            gThumb.drawImage(cachedImage, thumbX, thumbY, thumbSize, thumbSize, null);
        } else {
            // Java2D Placeholder thumbnail
            gThumb.setColor(new Color(0xE3, 0xDC, 0xCF));
            gThumb.fillRect(thumbX, thumbY, thumbSize, thumbSize);

            gThumb.setColor(new Color(0xA5, 0x9B, 0x8F));
            gThumb.setStroke(new BasicStroke(1.5f));
            // Camera icon placeholder
            gThumb.drawRoundRect(thumbX + 24, thumbY + 28, 32, 24, 4, 4);
            gThumb.drawOval(thumbX + 34, thumbY + 34, 12, 12);
            gThumb.fillRect(thumbX + 30, thumbY + 24, 10, 4);
        }
        gThumb.dispose();

        // Thumbnail border
        g2.setColor(new Color(0xD8, 0xD0, 0xC3));
        g2.drawRoundRect(thumbX, thumbY, thumbSize, thumbSize, thumbRadius, thumbRadius);

        // 3. Information layout (X = 110)
        int infoX = thumbX + thumbSize + 16;

        // a) Chip kode (#4B443D, white 10 bold, padding 8x3, radius 3)
        String kodeStr = (item != null) ? item.kode() : "#ARC-0000";
        Font chipFont = Fonts.sans(10f, Font.BOLD);
        g2.setFont(chipFont);
        int chipTextW = g2.getFontMetrics().stringWidth(kodeStr);
        int chipW = chipTextW + 16;
        int chipH = 18;
        int chipY = 16;

        g2.setColor(Theme.CHIP_BG);
        g2.fillRoundRect(infoX, chipY, chipW, chipH, 4, 4);

        g2.setColor(Color.WHITE);
        g2.drawString(kodeStr, infoX + 8, chipY + 13);

        int maxTextW = Math.max(100, (w >= 700) ? (w - 164 - infoX - 12) : (w - infoX - 14));

        // b) Judul nama barang apa adanya (serif 24, JANGAN auto-kapital)
        String namaStr = (item != null) ? item.getNama() : "Barang";
        g2.setFont(Fonts.serif(24f, Font.BOLD));
        g2.setColor(Theme.TEXT_PRIMARY);
        int titleY = chipY + chipH + 24;
        String displayNama = truncateText(namaStr, g2.getFontMetrics(), maxTextW);
        g2.drawString(displayNama, infoX, titleY);

        // c) Meta: "Ditemukan di {lokasiKejadian} • Tersimpan di {nama pos}" (sans 12.5, #6E655C)
        String lokasiStr = (item != null) ? item.getLokasiKejadian() : "-";
        String metaText = "Ditemukan di " + lokasiStr + " • Tersimpan di " + posNama;
        g2.setFont(Fonts.sans(12.5f, Font.PLAIN));
        g2.setColor(Theme.TEXT_SECONDARY);
        int metaY = titleY + 18;
        String displayMeta = truncateText(metaText, g2.getFontMetrics(), maxTextW);
        g2.drawString(displayMeta, infoX, metaY);

        // 4. Vertical divider line: 1px #E3DCCF, inset top/bottom 20, 164 px from right
        if (w >= 700) {
            int dividerX = w - 164;
            g2.setColor(Theme.CARD_DIVIDER);
            g2.drawLine(dividerX, 20, dividerX, h - 20);
        }

        g2.dispose();
    }

    private String truncateText(String text, java.awt.FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisW = fm.stringWidth(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fm.stringWidth(sb.toString() + c) + ellipsisW > maxWidth) {
                return sb.append(ellipsis).toString();
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
