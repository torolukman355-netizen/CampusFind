package com.campusfind.ui.pages;

import com.campusfind.ui.components.FooterBar;
import com.campusfind.ui.components.PageHeader;
import com.campusfind.ui.components.ResponsivePage;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

public class BantuanPage extends ResponsivePage {

    private final NavigationController controller;
    private final PageHeader header;
    private final RoundedPanel card1;
    private final RoundedPanel card2;
    private final FooterBar footerBar;

    public BantuanPage(NavigationController controller) {
        super(860);
        this.controller = controller;

        // 1. Reusable Page Header (Centered eyebrow, title, and wrapping subtitle)
        header = new PageHeader(
                "PUSAT INFORMASI & PANDUAN",
                "Panduan Restitusi",
                "Langkah terpadu tata kelola barang hilang dan temuan di kawasan kampus."
        );
        addContent(header);

        // 2. Guide Cards
        addContentStrut(28);
        card1 = createCard("1. Panduan Melaporkan Barang",
                "<html><body style='font-family: sans-serif; font-size: 13px; color: #2A2420; line-height: 1.5;'>" +
                        "• <b>Jika Anda menemukan barang:</b> Buka menu <i>Lapor Barang</i>, pilih jenis <i>Barang Temuan</i>. " +
                        "Pilih Pos Penyimpanan resmi tempat Anda menitipkan barang tersebut, dan berikan ciri rahasia verifikasi (jika ada).<br>" +
                        "• <b>Jika barang Anda hilang:</b> Pilih jenis <i>Barang Hilang</i>, jelaskan spesifikasi barang dan lokasi perkiraan kehilangan. " +
                        "Laporan Anda akan terdaftar di katalog pencarian.</body></html>");
        card1.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(card1);

        addContentStrut(16);
        card2 = createCard("2. Prosedur Klaim Barang Temuan",
                "<html><body style='font-family: sans-serif; font-size: 13px; color: #2A2420; line-height: 1.5;'>" +
                        "• Cari barang Anda di katalog <i>Barang Temuan</i>.<br>" +
                        "• Tekan tombol <b>KLAIM BARANG</b> pada barang yang sesuai.<br>" +
                        "• Masukkan Nomor Identitas (NIM/NIP) dan jelaskan ciri khusus kepemilikan Anda secara spesifik.<br>" +
                        "• Tentukan jadwal pengambilan (hari Senin–Jumat, Sesi Pagi / Siang).<br>" +
                        "• Bawa kartu identitas fisik (KTM/KTP) saat mengambil barang di Pos Satpam yang bersangkutan.</body></html>");
        card2.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(card2);

        // 3. Footer
        addContentStrut(40);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);
    }

    @Override
    protected void onContentWidthChanged(int contentWidth) {
        header.updateWidth(contentWidth);
        footerBar.updateResponsive(contentWidth);
        card1.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        card2.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        revalidate();
        repaint();
    }

    private RoundedPanel createCard(String title, String htmlBody) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 10), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Fonts.sans(15f, Font.BOLD));
        titleLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel bodyLbl = new JLabel(htmlBody);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(bodyLbl, BorderLayout.CENTER);
        return card;
    }
}
