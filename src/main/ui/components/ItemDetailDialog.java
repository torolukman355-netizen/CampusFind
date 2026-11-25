package com.campusfind.ui.components;

import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;

/**
 * Dialog detail barang yang menampilkan foto berukuran besar beserta
 * seluruh informasi barang. Dipakai oleh halaman Admin dan Petugas.
 */
public final class ItemDetailDialog {

    private static final int PHOTO_WIDTH = 340;
    private static final int PHOTO_HEIGHT = 300;

    private ItemDetailDialog() {
    }

    public static void show(Component parent, Item item) {
        if (item == null) {
            return;
        }
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Detail Barang " + item.getId(),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Theme.PAGE_BACKGROUND);

        dialog.add(buildHeader(item), BorderLayout.NORTH);
        dialog.add(buildBody(item), BorderLayout.CENTER);
        dialog.add(buildFooter(dialog), BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(760, 520));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static JPanel buildHeader(Item item) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.PAGE_BACKGROUND);
        header.setBorder(new EmptyBorder(18, 22, 10, 22));

        JLabel kode = new JLabel(item.kode());
        kode.setFont(Fonts.sans(11f, Font.BOLD));
        kode.setForeground(Theme.TEXT_MUTED);

        JLabel nama = new JLabel(item.getNama());
        nama.setFont(Fonts.serif(24f, Font.BOLD));
        nama.setForeground(Theme.TEXT_PRIMARY);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        meta.setOpaque(false);
        meta.add(chip(item.getJenis() == com.campusfind.model.JenisLaporan.HILANG ? "Hilang" : "Temuan",
                item.getJenis() == com.campusfind.model.JenisLaporan.HILANG
                        ? new Color(0xC0, 0x39, 0x2B) : new Color(0x27, 0xAE, 0x60)));
        meta.add(chip(item.getKategori().getLabel(), new Color(0x4B, 0x44, 0x3D)));
        meta.add(chip(item.getStatus().getLabel(), new Color(0x29, 0x80, 0xB9)));

        header.add(kode);
        header.add(Box.createVerticalStrut(4));
        header.add(nama);
        header.add(Box.createVerticalStrut(8));
        header.add(meta);
        return header;
    }

    private static JPanel buildBody(Item item) {
        JPanel body = new JPanel(new BorderLayout(22, 0));
        body.setBackground(Theme.PAGE_BACKGROUND);
        body.setBorder(new EmptyBorder(6, 22, 10, 22));

        body.add(buildPhoto(item), BorderLayout.WEST);
        body.add(buildDetails(item), BorderLayout.CENTER);
        return body;
    }

    private static JPanel buildPhoto(Item item) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(PHOTO_WIDTH, PHOTO_HEIGHT));

        JLabel foto = new JLabel("Foto tidak tersedia", SwingConstants.CENTER);
        foto.setFont(Fonts.sans(12f, Font.PLAIN));
        foto.setForeground(Theme.TEXT_MUTED);
        foto.setOpaque(true);
        foto.setBackground(new Color(0xE3, 0xDC, 0xCF));
        foto.setBorder(new LineBorder(new Color(0xD8, 0xD0, 0xC3), 1));
        foto.setPreferredSize(new Dimension(PHOTO_WIDTH, PHOTO_HEIGHT));

        BufferedImage original = ItemImages.load(item);
        if (original != null) {
            BufferedImage scaled = ItemImages.fit(original, PHOTO_WIDTH - 8, PHOTO_HEIGHT - 8);
            foto.setIcon(new ImageIcon(scaled));
            foto.setText("");
            foto.setBackground(Color.WHITE);
        }

        wrapper.add(foto, BorderLayout.CENTER);
        return wrapper;
    }

    private static JPanel buildDetails(Item item) {
        JPanel details = new JPanel(new GridBagLayout());
        details.setOpaque(false);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(4, 0, 4, 14);

        GridBagConstraints valueConstraints = new GridBagConstraints();
        valueConstraints.gridx = 1;
        valueConstraints.gridy = 0;
        valueConstraints.weightx = 1;
        valueConstraints.fill = GridBagConstraints.HORIZONTAL;
        valueConstraints.anchor = GridBagConstraints.NORTHWEST;
        valueConstraints.insets = new Insets(4, 0, 4, 0);

        addRow(details, labelConstraints, valueConstraints, "ID Barang", item.getId());
        addRow(details, labelConstraints, valueConstraints, "Jenis Laporan",
                item.getJenis() == com.campusfind.model.JenisLaporan.HILANG ? "Hilang" : "Temuan");
        addRow(details, labelConstraints, valueConstraints, "Kategori", item.getKategori().getLabel());
        addRow(details, labelConstraints, valueConstraints, "Deskripsi", safe(item.getDeskripsi()));
        addRow(details, labelConstraints, valueConstraints, "Lokasi Kejadian", safe(item.getLokasiKejadian()));
        addRow(details, labelConstraints, valueConstraints, "Tanggal Kejadian",
                item.getTanggalKejadian() == null ? "-"
                        : item.getTanggalKejadian().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        addRow(details, labelConstraints, valueConstraints, "Lokasi Simpan",
                safe(item.getTempatPenitipan()));

        if (item instanceof BarangElektronik elektronik) {
            addRow(details, labelConstraints, valueConstraints, "Merek", safe(elektronik.getMerek()));
            addRow(details, labelConstraints, valueConstraints, "Nomor Seri", safe(elektronik.getNomorSeri()));
        } else if (item instanceof BarangNonElektronik nonElektronik) {
            addRow(details, labelConstraints, valueConstraints, "Warna", safe(nonElektronik.getWarna()));
            addRow(details, labelConstraints, valueConstraints, "Bahan", safe(nonElektronik.getBahan()));
        }

        addRow(details, labelConstraints, valueConstraints, "Ciri Rahasia", safe(item.getCiriRahasia()));
        addRow(details, labelConstraints, valueConstraints, "Pelapor",
                item.getPelapor() == null ? "-"
                        : item.getPelapor().getNama() + " (" + item.getPelapor().getNomorIdentitas()
                        + " - " + item.getPelapor().getLabelRole() + ")");
        addRow(details, labelConstraints, valueConstraints, "Status", item.getStatus().getLabel());
        addRow(details, labelConstraints, valueConstraints, "Dibuat Pada",
                item.getDibuatPada() == null ? "-"
                        : item.getDibuatPada().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = GridBagConstraints.RELATIVE;
        filler.gridwidth = 2;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;
        details.add(Box.createVerticalGlue(), filler);

        JScrollPane scroll = new JScrollPane(details);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Theme.PAGE_BACKGROUND);
        scroll.setPreferredSize(new Dimension(360, PHOTO_HEIGHT));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private static void addRow(JPanel panel, GridBagConstraints labelConstraints,
                               GridBagConstraints valueConstraints, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(Fonts.sans(11f, Font.BOLD));
        lblLabel.setForeground(Theme.TEXT_MUTED);

        JLabel lblValue = new JLabel("<html><body style='width:230px'>" + escapeHtml(value) + "</body></html>");
        lblValue.setFont(Fonts.sans(12f, Font.PLAIN));
        lblValue.setForeground(Theme.TEXT_PRIMARY);

        panel.add(lblLabel, labelConstraints);
        panel.add(lblValue, valueConstraints);

        labelConstraints.gridy++;
        valueConstraints.gridy++;
    }

    private static JPanel buildFooter(JDialog dialog) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBackground(Theme.PAGE_BACKGROUND);
        footer.setBorder(new EmptyBorder(6, 22, 18, 22));

        JButton tutup = new JButton("Tutup");
        tutup.setFont(Fonts.sans(12f, Font.BOLD));
        tutup.setFocusPainted(false);
        tutup.setBackground(new Color(0x1E, 0x1B, 0x4B));
        tutup.setForeground(Color.WHITE);
        tutup.setOpaque(true);
        tutup.setBorder(new EmptyBorder(9, 24, 9, 24));
        tutup.addActionListener(e -> dialog.dispose());

        footer.add(tutup);
        return footer;
    }

    private static JLabel chip(String text, Color background) {
        JLabel chip = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setForeground(Color.WHITE);
        chip.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        chip.setBorder(new EmptyBorder(4, 10, 4, 10));
        return chip;
    }

    private static String safe(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private static String escapeHtml(String value) {
        return safe(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
