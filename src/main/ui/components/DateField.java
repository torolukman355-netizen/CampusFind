package com.campusfind.ui.components;

import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class DateField extends JFormattedTextField {

    private static final DateTimeFormatter STRICT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private boolean hasError = false;

    public DateField() {
        super(createFormatter());
        init();
    }

    private static MaskFormatter createFormatter() {
        try {
            MaskFormatter formatter = new MaskFormatter("##/##/####");
            formatter.setPlaceholderCharacter('_');
            formatter.setValidCharacters("0123456789");
            return formatter;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        setFont(Fonts.sans(13f, Font.PLAIN));
        setForeground(Theme.TEXT_PRIMARY);
        setBackground(Theme.INPUT_BACKGROUND);
        setOpaque(false);
        setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        setPreferredSize(new Dimension(getPreferredSize().width, Theme.INPUT_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.INPUT_HEIGHT));
        setMargin(new Insets(8, 12, 8, 12));

        // Up/down arrow key shifts date ±1 day
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    shiftDate(1);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    shiftDate(-1);
                    e.consume();
                }
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    private void shiftDate(int deltaDays) {
        LocalDate current = getDate();
        if (current == null) {
            current = LocalDate.now();
        }
        LocalDate shifted = current.plusDays(deltaDays);
        // Skip weekend if shifted lands on Saturday or Sunday
        if (shifted.getDayOfWeek() == DayOfWeek.SATURDAY) {
            shifted = deltaDays > 0 ? shifted.plusDays(2) : shifted.minusDays(1);
        } else if (shifted.getDayOfWeek() == DayOfWeek.SUNDAY) {
            shifted = deltaDays > 0 ? shifted.plusDays(1) : shifted.minusDays(2);
        }
        setDate(shifted);
    }

    public LocalDate getDate() {
        String text = getText();
        if (text == null || text.contains("_") || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), STRICT_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public void setDate(LocalDate date) {
        if (date != null) {
            setText(date.format(STRICT_FORMATTER));
        } else {
            setText("");
        }
        repaint();
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
        } else if (hasFocus()) {
            g2.setColor(Theme.INPUT_FOCUS_BORDER);
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
