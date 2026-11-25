package com.campusfind.ui.components;

import com.campusfind.model.StatusItem;
import com.campusfind.ui.theme.Fonts;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Design system helper untuk membuat komponen Swing bergaya modern SaaS / Executive Desktop.
 */
public final class ModernSwingKit {

    public static final Color SLATE_900 = new Color(15, 23, 42);
    public static final Color SLATE_800 = new Color(30, 41, 59);
    public static final Color SLATE_700 = new Color(51, 65, 85);
    public static final Color SLATE_500 = new Color(100, 116, 139);
    public static final Color SLATE_400 = new Color(148, 163, 184);
    public static final Color SLATE_200 = new Color(226, 232, 240);
    public static final Color SLATE_100 = new Color(241, 245, 249);
    public static final Color SLATE_50  = new Color(248, 250, 252);
    public static final Color PAGE_BG   = new Color(241, 245, 249);

    public static final Color BLUE_600    = new Color(37, 99, 235);
    public static final Color INDIGO_600  = new Color(79, 70, 229);
    public static final Color EMERALD_600 = new Color(5, 150, 105);
    public static final Color AMBER_600   = new Color(217, 119, 6);
    public static final Color ROSE_600    = new Color(225, 29, 72);
    public static final Color PURPLE_600  = new Color(124, 58, 237);

    private ModernSwingKit() {}

    public static JPanel createRoundedCard(int padding) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SLATE_200, 1, true),
                new EmptyBorder(padding, padding, padding, padding)
        ));
        return card;
    }

    public static JPanel createKpiCard(String title, JLabel countLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SLATE_200, 1, true),
                BorderFactory.createCompoundBorder(
                        new MatteBorder(3, 0, 0, 0, accentColor),
                        new EmptyBorder(14, 16, 14, 16)
                )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Fonts.sans(11f, Font.BOLD));
        lblTitle.setForeground(SLATE_500);

        countLabel.setFont(Fonts.sans(28f, Font.BOLD));
        countLabel.setForeground(accentColor);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(Fonts.sans(11.5f, Font.PLAIN));
        lblSub.setForeground(SLATE_400);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(SLATE_200);
                } else if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Fonts.sans(12f, Font.BOLD));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    public static JScrollPane wrapScroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(Color.WHITE);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(new LineBorder(SLATE_200, 1));
        return sp;
    }

    public static DefaultTableModel createReadOnlyModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable createModernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(Fonts.sans(12.5f, Font.PLAIN));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(SLATE_100);
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(new Color(30, 27, 75));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 38));
        header.setFont(Fonts.sans(11.5f, Font.BOLD));
        header.setBackground(SLATE_100);
        header.setForeground(SLATE_700);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SLATE_200));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setBorder(new EmptyBorder(0, 12, 0, 12));
                String valStr = (value != null) ? value.toString() : "-";

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : SLATE_50);
                    c.setForeground(SLATE_800);
                }

                String upper = valStr.toUpperCase();
                if (upper.equals("DILAPORKAN") || upper.equals("DIAJUKAN")) {
                    c.setForeground(AMBER_600);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (upper.equals("DIVERIFIKASI") || upper.equals("TERSEDIA") || upper.equals("DICARI") || upper.equals("AKTIF")) {
                    c.setForeground(BLUE_600);
                } else if (upper.equals("COCOK") || upper.contains("OTP")) {
                    c.setForeground(PURPLE_600);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (upper.equals("DIKLAIM") || upper.equals("DALAM PROSES KLAIM") || upper.equals("DALAM_PROSES_KLAIM") || upper.equals("DISETUJUI")) {
                    c.setForeground(new Color(194, 65, 12));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (upper.equals("DIKEMBALIKAN") || upper.equals("DISERAHKAN") || upper.contains("TERKUNCI") || upper.equals("TEMUAN")) {
                    c.setForeground(EMERALD_600);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (upper.contains("TOLAK") || upper.equals("NONAKTIF") || upper.equals("HILANG")) {
                    c.setForeground(ROSE_600);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (upper.equals("DIARSIPKAN")) {
                    c.setForeground(SLATE_500);
                    c.setFont(c.getFont().deriveFont(Font.ITALIC));
                }
                return c;
            }
        });
        return table;
    }

    /**
     * Stepper pelacakan status 5 tahap (US-010, US-026) untuk Role Mahasiswa & Detail.
     */
    public static JPanel createStatusStepper(StatusItem status) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setOpaque(false);
        if (status == StatusItem.DITOLAK) {
            JLabel lbl = new JLabel("✖ DITOLAK PETUGAS");
            lbl.setFont(Fonts.sans(11.5f, Font.BOLD));
            lbl.setForeground(ROSE_600);
            p.add(lbl);
            return p;
        }
        if (status == StatusItem.DIARSIPKAN) {
            JLabel lbl = new JLabel("🗄 DIARSIPKAN (> 60 Hari)");
            lbl.setFont(Fonts.sans(11.5f, Font.BOLD));
            lbl.setForeground(SLATE_500);
            p.add(lbl);
            return p;
        }

        int stepIdx = switch (status) {
            case DILAPORKAN -> 0;
            case DIVERIFIKASI, TERSEDIA, DICARI -> 1;
            case COCOK -> 2;
            case DIKLAIM, DALAM_PROSES_KLAIM -> 3;
            case DIKEMBALIKAN, DISERAHKAN -> 4;
            default -> 0;
        };

        String[] steps = {"1. Dilaporkan", "2. Diverifikasi", "3. Cocok", "4. Diklaim", "5. Dikembalikan"};
        for (int i = 0; i < steps.length; i++) {
            JLabel lbl = new JLabel(steps[i], SwingConstants.CENTER);
            lbl.setFont(Fonts.sans(11f, i <= stepIdx ? Font.BOLD : Font.PLAIN));
            lbl.setOpaque(true);
            lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
            if (i < stepIdx) {
                lbl.setBackground(new Color(209, 250, 229));
                lbl.setForeground(EMERALD_600);
            } else if (i == stepIdx) {
                lbl.setBackground(new Color(219, 234, 254));
                lbl.setForeground(BLUE_600);
            } else {
                lbl.setBackground(SLATE_100);
                lbl.setForeground(SLATE_400);
            }
            p.add(lbl);
            if (i < steps.length - 1) {
                JLabel arrow = new JLabel("→");
                arrow.setForeground(SLATE_400);
                p.add(arrow);
            }
        }
        return p;
    }
}
