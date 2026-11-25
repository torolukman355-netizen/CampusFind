package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.KandidatCocokItem;
import com.campusfind.model.MatchResult;
import com.campusfind.service.AppSession;
import com.campusfind.service.KandidatService;
import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Dialog antarmuka pengguna untuk melihat hasil rekomendasi "Kandidat Cocok" (US-015, US-026).
 * Pencarian dijalankan secara asinkron menggunakan SwingWorker.
 */
public class KandidatCocokDialog extends JDialog {

    private final Item laporanHilang;
    private final KandidatService kandidatService;
    private final NavigationController controller;
    private final AppSession appSession;

    private JPanel listContainer;
    private SwingWorker<List<KandidatCocokItem>, Void> searchWorker;

    public KandidatCocokDialog(JFrame parent,
                               Item laporanHilang,
                               KandidatService kandidatService,
                               NavigationController controller,
                               AppSession appSession) {
        super(parent, "Kandidat Cocok — " + (laporanHilang != null ? laporanHilang.getNama() : "Barang"), true);
        this.laporanHilang = laporanHilang;
        this.kandidatService = kandidatService;
        this.controller = controller;
        this.appSession = appSession;

        setSize(780, 600);
        setLocationRelativeTo(parent);
        initView();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (searchWorker != null && !searchWorker.isDone()) {
                    searchWorker.cancel(true);
                }
            }
        });

        startSearchWorker();
    }

    private void initView() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(Theme.PAGE_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Rekomendasi Kandidat Cocok");
        title.setFont(Fonts.serif(22f, Font.BOLD));
        title.setForeground(Theme.TEXT_PRIMARY);

        String infoText = (laporanHilang != null)
                ? "Mencocokkan barang temuan untuk laporan Anda: " + laporanHilang.kode() + " (" + laporanHilang.getNama() + ")"
                : "Mencocokkan barang temuan...";
        JLabel subtitle = new JLabel(infoText);
        subtitle.setFont(Fonts.sans(13f, Font.PLAIN));
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        headerPanel.add(title);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);
        content.add(headerPanel, BorderLayout.NORTH);

        // Center Scrollable List
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        content.add(scrollPane, BorderLayout.CENTER);

        // Footer Panel
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        FlatButton closeBtn = new FlatButton("TUTUP");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void startSearchWorker() {
        listContainer.removeAll();
        JPanel loadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        loadingPanel.setOpaque(false);
        JLabel lblLoading = new JLabel("Sedang menganalisis kandidat barang temuan...");
        lblLoading.setFont(Fonts.sans(14f, Font.ITALIC));
        lblLoading.setForeground(Theme.TEXT_SECONDARY);
        loadingPanel.add(lblLoading);
        listContainer.add(loadingPanel);
        listContainer.revalidate();
        listContainer.repaint();

        searchWorker = new SwingWorker<>() {
            @Override
            protected List<KandidatCocokItem> doInBackground() {
                if (kandidatService == null || laporanHilang == null) {
                    return List.of();
                }
                String nomorIdentitas = appSession.getNomorIdentitas();
                return kandidatService.cariKandidat(laporanHilang.getId(), nomorIdentitas);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    List<KandidatCocokItem> hasil = get();
                    renderHasil(hasil);
                } catch (CancellationException ignored) {
                    // Worker dibatalkan
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String msg = (cause != null && cause.getMessage() != null)
                            ? cause.getMessage()
                            : "Terjadi kesalahan saat memuat kandidat cocok.";
                    renderPesan(msg, true);
                } catch (Exception e) {
                    renderPesan("Gagal memuat kandidat cocok.", true);
                }
            }
        };

        searchWorker.execute();
    }

    private void renderHasil(List<KandidatCocokItem> hasil) {
        listContainer.removeAll();

        if (hasil == null || hasil.isEmpty()) {
            renderPesan("Belum ada kandidat dengan kecocokan ≥ 70%.", false);
            return;
        }

        for (KandidatCocokItem item : hasil) {
            Item temuan = item.getItemTemuan();
            MatchResult match = item.getMatchResult();

            RoundedPanel card = new RoundedPanel(new BorderLayout(14, 10), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
            card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            card.setMaximumSize(new Dimension(720, 180));

            // Top Row: Nama, Kode, Badge Skor
            JPanel topRow = new JPanel(new BorderLayout(8, 0));
            topRow.setOpaque(false);

            JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            titlePanel.setOpaque(false);

            JLabel lblNama = new JLabel(temuan.getNama());
            lblNama.setFont(Fonts.sans(15f, Font.BOLD));
            lblNama.setForeground(Theme.TEXT_PRIMARY);

            JLabel lblKode = new JLabel(temuan.kode());
            lblKode.setFont(Fonts.sans(12f, Font.PLAIN));
            lblKode.setForeground(Theme.TEXT_MUTED);

            titlePanel.add(lblNama);
            titlePanel.add(lblKode);

            // Badge Skor (%)
            JLabel badgeSkor = new JLabel("  " + match.getSkor() + "% Cocok  ");
            badgeSkor.setFont(Fonts.sans(12.5f, Font.BOLD));
            badgeSkor.setOpaque(true);
            if (match.getSkor() >= 85) {
                badgeSkor.setBackground(new Color(220, 252, 231)); // Soft green
                badgeSkor.setForeground(new Color(22, 101, 52));
            } else {
                badgeSkor.setBackground(new Color(254, 243, 199)); // Soft amber
                badgeSkor.setForeground(new Color(146, 64, 14));
            }
            badgeSkor.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            topRow.add(titlePanel, BorderLayout.WEST);
            topRow.add(badgeSkor, BorderLayout.EAST);

            // Center: Info barang & Alasan Kecocokan
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.setOpaque(false);

            JLabel lblLokasiWaktu = new JLabel("Ditemukan di: " + temuan.getLokasiKejadian() + " (" + temuan.getTanggalKejadian() + ")");
            lblLokasiWaktu.setFont(Fonts.sans(12f, Font.PLAIN));
            lblLokasiWaktu.setForeground(Theme.TEXT_SECONDARY);
            centerPanel.add(lblLokasiWaktu);
            centerPanel.add(Box.createVerticalStrut(6));

            JLabel lblAlasanHeader = new JLabel("Alasan Kecocokan:");
            lblAlasanHeader.setFont(Fonts.sans(12f, Font.BOLD));
            lblAlasanHeader.setForeground(Theme.TEXT_PRIMARY);
            centerPanel.add(lblAlasanHeader);

            for (String alasan : match.getAlasan()) {
                JLabel lblAlasan = new JLabel("• " + alasan);
                lblAlasan.setFont(Fonts.sans(12f, Font.PLAIN));
                lblAlasan.setForeground(new Color(40, 50, 40));
                centerPanel.add(lblAlasan);
            }

            // Bottom Right: Tombol Ajukan Klaim
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            bottomPanel.setOpaque(false);

            FlatButton klaimBtn = new FlatButton("AJUKAN KLAIM");
            klaimBtn.setPreferredSize(new Dimension(130, 32));
            klaimBtn.setRadius(4);
            klaimBtn.addActionListener(e -> {
                dispose();
                if (controller != null) {
                    controller.navigateToKlaim(temuan.getId());
                }
            });
            bottomPanel.add(klaimBtn);

            card.add(topRow, BorderLayout.NORTH);
            card.add(centerPanel, BorderLayout.CENTER);
            card.add(bottomPanel, BorderLayout.SOUTH);

            listContainer.add(card);
            listContainer.add(Box.createVerticalStrut(12));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void renderPesan(String pesan, boolean isError) {
        listContainer.removeAll();
        JPanel msgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        msgPanel.setOpaque(false);
        JLabel lbl = new JLabel(pesan);
        lbl.setFont(Fonts.sans(13.5f, isError ? Font.BOLD : Font.PLAIN));
        lbl.setForeground(isError ? new Color(185, 28, 28) : Theme.TEXT_MUTED);
        msgPanel.add(lbl);
        listContainer.add(msgPanel);
        listContainer.revalidate();
        listContainer.repaint();
    }
}
