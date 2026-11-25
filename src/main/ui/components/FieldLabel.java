package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JLabel;
import java.awt.Font;

public class FieldLabel extends JLabel {

    public FieldLabel(String text) {
        super(text.toUpperCase());
        setFont(Fonts.trackedSans(10.5f, Font.BOLD, 0.12));
        setForeground(Theme.TEXT_PRIMARY);
        setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    }
}
