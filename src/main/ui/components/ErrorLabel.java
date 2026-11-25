package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JLabel;
import java.awt.Font;

public class ErrorLabel extends JLabel {

    public ErrorLabel(String text) {
        super(text);
        setFont(Fonts.sans(11.5f, Font.PLAIN));
        setForeground(Theme.ERROR_COLOR);
        setVisible(text != null && !text.isEmpty());
    }

    public ErrorLabel() {
        this("");
    }

    public void setError(String text) {
        if (text != null && !text.trim().isEmpty()) {
            setText(text.trim());
            setVisible(true);
        } else {
            setText("");
            setVisible(false);
        }
    }

    public void clearError() {
        setText("");
        setVisible(false);
    }
}
