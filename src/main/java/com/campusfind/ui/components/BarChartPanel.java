package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Komponen Grafik Batang (Bar Chart) kustom berbasis Java2D untuk Dashboard Statistik Admin (US-023, US-031).
 * Menangani keadaan data kosong dengan rapi tanpa error atau division by zero.
 */
public class BarChartPanel extends JPanel {

    private final String chartTitle;
    private Map<String, Integer> data = new LinkedHashMap<>();

    public BarChartPanel(String chartTitle) {
        this.chartTitle = chartTitle;
        setOpaque(false);
        setPreferredSize(new Dimension(420, 240));
    }

    public void setData(Map<String, Integer> newData) {
        this.data = (newData != null) ? new LinkedHashMap<>(newData) : new LinkedHashMap<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Card container
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w, h, 12, 12);
        g2.setColor(ModernSwingKit.SLATE_200);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

        // Title
        g2.setColor(ModernSwingKit.SLATE_800);
        g2.setFont(Fonts.sans(13.5f, Font.BOLD));
        g2.drawString(chartTitle, 18, 28);

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (data.isEmpty() || total == 0) {
            g2.setColor(ModernSwingKit.SLATE_400);
            g2.setFont(Fonts.sans(12.5f, Font.ITALIC));
            String emptyMsg = "Belum ada data laporan untuk ditampilkan pada grafik.";
            int sw = g2.getFontMetrics().stringWidth(emptyMsg);
            g2.drawString(emptyMsg, Math.max(16, (w - sw) / 2), h / 2 + 10);
            g2.dispose();
            return;
        }

        int maxVal = Math.max(1, data.values().stream().mapToInt(Integer::intValue).max().orElse(1));
        int startY = 48;
        int availH = Math.max(60, h - startY - 16);
        int rowCount = data.size();
        int rowHeight = Math.min(34, availH / Math.max(1, rowCount));
        int labelColW = 145;
        int valColW = 42;
        int barMaxW = Math.max(60, w - labelColW - valColW - 36);

        int idx = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int y = startY + idx * rowHeight;
            if (y + 18 > h) break;

            String label = entry.getKey();
            if (label.length() > 20) label = label.substring(0, 18) + "...";
            int val = entry.getValue();

            // Label
            g2.setColor(ModernSwingKit.SLATE_700);
            g2.setFont(Fonts.sans(11.5f, Font.PLAIN));
            g2.drawString(label, 18, y + 14);

            // Track
            int trackX = 18 + labelColW;
            int trackY = y + 4;
            int barH = 12;
            g2.setColor(ModernSwingKit.SLATE_100);
            g2.fillRoundRect(trackX, trackY, barMaxW, barH, 12, 12);

            // Fill bar
            int fillW = (int) Math.round(((double) val / maxVal) * barMaxW);
            if (val > 0 && fillW < 8) fillW = 8;
            if (fillW > 0) {
                g2.setPaint(new GradientPaint(trackX, trackY, ModernSwingKit.BLUE_600,
                        trackX + fillW, trackY, ModernSwingKit.INDIGO_600));
                g2.fillRoundRect(trackX, trackY, fillW, barH, 12, 12);
            }

            // Value
            g2.setColor(ModernSwingKit.SLATE_900);
            g2.setFont(Fonts.sans(11.5f, Font.BOLD));
            g2.drawString(String.valueOf(val), trackX + barMaxW + 10, y + 14);
            idx++;
        }

        g2.dispose();
    }
}
