package com.campusfind.ui.pages;

import com.campusfind.model.PosPenyimpanan;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.FooterBar;
import com.campusfind.ui.components.PageHeader;
import com.campusfind.ui.components.ResponsivePage;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.icons.PinIcon;
import com.campusfind.ui.icons.ShieldIcon;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class PosPage extends ResponsivePage {

    private final NavigationController controller;
    private final PosService posService;
    private final PageHeader header;
    private final JPanel posListContainer;
    private final RoundedPanel infoCard;
    private final FooterBar footerBar;

    public PosPage(NavigationController controller, PosService posService) {
        super(860);
        this.controller = controller;
        this.posService = posService;

        // 1. Reusable Header (all centered, responsive title & subtitle)
        header = new PageHeader(
                "LOKASI PENYIMPANAN RESMI",
                "Daftar Pos Jaga",
                "Pos penyimpanan resmi untuk pengamanan dan serah-terima barang temuan di seluruh kawasan kampus.<br>" +
                "Barang temuan yang diserahkan akan dicatat dalam Berita Acara resmi oleh petugas jaga."
        );
        addContent(header);

        // 2. Pos List Container
        addContentStrut(24);
        posListContainer = new JPanel();
        posListContainer.setLayout(new BoxLayout(posListContainer, BoxLayout.Y_AXIS));
        posListContainer.setOpaque(false);
        posListContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(posListContainer);

        // 3. Information Card (Admin-only custody notice)
        addContentStrut(24);
        infoCard = createInfoCard();
        infoCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(infoCard);

        // 4. Footer
        addContentStrut(40);
        footerBar = new FooterBar(controller);
        footerBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(footerBar);
        addContentStrut(40);

        loadPosList();
    }

    @Override
    protected void onContentWidthChanged(int contentWidth) {
        header.updateWidth(contentWidth);
        footerBar.updateResponsive(contentWidth);
        infoCard.setMaximumSize(new Dimension(contentWidth, Integer.MAX_VALUE));
        revalidate();
        repaint();
    }

    private RoundedPanel createInfoCard() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(14, 0), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.add(new JLabel(new ShieldIcon(20, Theme.NUMBER_CIRCLE_BG)));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel infoTitle = new JLabel("Pusat Kendali Keamanan Kampus");
        infoTitle.setFont(Fonts.sans(13f, Font.BOLD));
        infoTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel infoDesc = new JLabel("<html><body style='font-family: sans-serif; font-size: 12px; color: #6E655C; line-height: 1.4;'>" +
                "Penetapan dan penambahan pos kustodi baru dikelola secara terpusat oleh Petugas Keamanan Kampus & Administrator sistem.<br>" +
                "Untuk menitipkan barang temuan, silakan datangi pos jaga terdekat pada jam layanan resmi.</body></html>");

        text.add(infoTitle);
        text.add(Box.createVerticalStrut(4));
        text.add(infoDesc);

        left.add(text);
        card.add(left, BorderLayout.CENTER);
        return card;
    }

    private void loadPosList() {
        posListContainer.removeAll();
        List<PosPenyimpanan> list = posService.findAll();

        for (PosPenyimpanan pos : list) {
            RoundedPanel card = new RoundedPanel(new BorderLayout(16, 0), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, Theme.CARD_RADIUS);
            card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            left.setOpaque(false);
            left.add(new JLabel(new PinIcon(18, Theme.TEXT_PRIMARY)));

            JPanel text = new JPanel();
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.setOpaque(false);

            JLabel name = new JLabel(pos.getNama());
            name.setFont(Fonts.sans(14f, Font.BOLD));
            name.setForeground(Theme.TEXT_PRIMARY);

            JLabel loc = new JLabel(pos.getLokasiPenyerahan());
            loc.setFont(Fonts.sans(12f, Font.PLAIN));
            loc.setForeground(Theme.TEXT_SECONDARY);

            text.add(name);
            text.add(Box.createVerticalStrut(4));
            text.add(loc);
            left.add(text);

            JLabel chip = new JLabel(pos.getLabelPendek());
            chip.setFont(Fonts.trackedSans(10f, Font.BOLD, 0.12));
            chip.setForeground(Theme.TEXT_MUTED);

            card.add(left, BorderLayout.CENTER);
            card.add(chip, BorderLayout.EAST);

            posListContainer.add(card);
            posListContainer.add(Box.createVerticalStrut(12));
        }

        posListContainer.revalidate();
        posListContainer.repaint();
    }
}
