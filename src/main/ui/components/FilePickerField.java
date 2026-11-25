package com.campusfind.ui.components;

import com.campusfind.ui.icons.CloudUploadIcon;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class FilePickerField extends JPanel {

    private File selectedFile;
    private boolean hasError = false;
    private final JLabel contentLabel;
    private final JLabel removeButton;
    private Runnable onFileChanged;

    public FilePickerField() {
        super(new BorderLayout(8, 0));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(getPreferredSize().width, Theme.INPUT_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.INPUT_HEIGHT));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        contentLabel = new JLabel();
        contentLabel.setFont(Fonts.sans(13f, Font.PLAIN));
        contentLabel.setForeground(Theme.TEXT_PRIMARY);
        contentLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 0));

        removeButton = new JLabel("✕");
        removeButton.setFont(Fonts.sans(14f, Font.BOLD));
        removeButton.setForeground(Theme.TEXT_MUTED);
        removeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 14));
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeButton.setVisible(false);

        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clearFile();
                e.consume();
            }
        });

        add(contentLabel, BorderLayout.CENTER);
        add(removeButton, BorderLayout.EAST);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // If clicked on remove button, already handled
                if (removeButton.isVisible() && removeButton.getBounds().contains(e.getPoint())) {
                    return;
                }
                chooseFile();
            }
        });

        updateDisplay();
    }

    public void setOnFileChanged(Runnable onFileChanged) {
        this.onFileChanged = onFileChanged;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih Berkas Bukti Pendukung");
        chooser.setFileFilter(new FileNameExtensionFilter("Berkas Gambar & PDF (JPG, PNG, PDF)", "jpg", "jpeg", "png", "pdf"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            this.selectedFile = chooser.getSelectedFile();
            updateDisplay();
            if (onFileChanged != null) {
                onFileChanged.run();
            }
        }
    }

    public void clearFile() {
        this.selectedFile = null;
        updateDisplay();
        if (onFileChanged != null) {
            onFileChanged.run();
        }
    }

    private void updateDisplay() {
        if (selectedFile == null) {
            contentLabel.setIcon(new CloudUploadIcon(16, Theme.TEXT_SECONDARY));
            contentLabel.setText("<html><span style='font-size: 13px; color: #2A2420;'>Pilih berkas struk, foto Find My, atau kotak dus </span><span style='font-size: 9px; font-weight: bold; color: #8A7F73;'>(MAKS 10MB)</span></html>");
            removeButton.setVisible(false);
        } else {
            contentLabel.setIcon(null);
            long bytes = selectedFile.length();
            String sizeStr;
            if (bytes < 1024) {
                sizeStr = bytes + " B";
            } else if (bytes < 1024 * 1024) {
                sizeStr = String.format("%.1f KB", bytes / 1024.0);
            } else {
                sizeStr = String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            }
            contentLabel.setText("<html><b>" + selectedFile.getName() + "</b> <span style='color: #6E655C;'>(" + sizeStr + ")</span></html>");
            removeButton.setVisible(true);
        }
        repaint();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File file) {
        this.selectedFile = file;
        updateDisplay();
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        repaint();
    }

    public boolean isHasError() {
        return hasError;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background
        g2.setColor(Theme.INPUT_BACKGROUND);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);

        // Draw border
        if (hasError) {
            g2.setColor(Theme.ERROR_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        } else {
            g2.setColor(Theme.INPUT_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.INPUT_RADIUS, Theme.INPUT_RADIUS);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
