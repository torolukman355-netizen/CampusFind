package com.campusfind.ui.pages;

import com.campusfind.ui.components.FlatButton;
import com.campusfind.ui.components.RoundedPanel;
import com.campusfind.ui.icons.CheckIcon;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class SuccessDialog extends JDialog {

    public SuccessDialog(JFrame parent, String message, String kode) {
        super(parent, "Berhasil", true);
        setSize(460, 320);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.PAGE_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Green checkmark badge
        RoundedPanel badge = new RoundedPanel(new BorderLayout(), new Color(0x3B, 0x82, 0x54), null, 24);
        badge.setPreferredSize(new Dimension(48, 48));
        badge.setMaximumSize(new Dimension(48, 48));
        JLabel iconLbl = new JLabel(new CheckIcon(24, Color.WHITE));
        iconLbl.setHorizontalAlignment(JLabel.CENTER);
        badge.add(iconLbl, BorderLayout.CENTER);
        badge.setAlignmentX(CENTER_ALIGNMENT);

        content.add(badge);
        content.add(Box.createVerticalStrut(18));

        JLabel title = new JLabel("Operasi Berhasil");
        title.setFont(Fonts.sans(22f, Font.BOLD));
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);

        content.add(Box.createVerticalStrut(8));
        JLabel msgLbl = new JLabel("<html><body style='width: 320px; text-align: center; font-family: sans-serif; font-size: 13px; color: #475569;'>" +
                message + "</body></html>");
        msgLbl.setAlignmentX(CENTER_ALIGNMENT);
        content.add(msgLbl);

        if (kode != null && !kode.isEmpty()) {
            content.add(Box.createVerticalStrut(14));
            RoundedPanel codePanel = new RoundedPanel(new BorderLayout(), Theme.CARD_BACKGROUND, Theme.CARD_BORDER, 6);
            codePanel.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            codePanel.setMaximumSize(new Dimension(240, 36));

            JLabel codeLbl = new JLabel(kode);
            codeLbl.setFont(Fonts.sans(14f, Font.BOLD));
            codeLbl.setForeground(Theme.NUMBER_CIRCLE_BG);
            codeLbl.setHorizontalAlignment(JLabel.CENTER);
            codePanel.add(codeLbl, BorderLayout.CENTER);
            codePanel.setAlignmentX(CENTER_ALIGNMENT);
            content.add(codePanel);
        }

        content.add(Box.createVerticalStrut(24));
        FlatButton okBtn = new FlatButton("OK, MENGERTI");
        okBtn.setAlignmentX(CENTER_ALIGNMENT);
        okBtn.setPreferredSize(new Dimension(160, 38));
        okBtn.setRadius(4);
        okBtn.addActionListener(e -> dispose());
        content.add(okBtn);

        setContentPane(content);
    }
}
