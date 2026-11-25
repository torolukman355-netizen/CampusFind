package com.campusfind.ui.theme;

import java.awt.Color;
import java.awt.Dimension;

public final class Theme {

    private Theme() {}

    // Colors (Unified Modern Slate & Indigo Design System)
    public static final Color PAGE_BACKGROUND = new Color(0xF8, 0xFA, 0xFC);      // Slate 50
    public static final Color NAVBAR_BACKGROUND = Color.WHITE;
    public static final Color NAVBAR_BORDER = new Color(0xE2, 0xE8, 0xF0);        // Slate 200
    public static final Color NAVBAR_LINK = new Color(0x47, 0x55, 0x69);          // Slate 600
    public static final Color NAVBAR_LINK_HOVER = new Color(0x4F, 0x46, 0xE5);    // Indigo 600

    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color CARD_BORDER = new Color(0xE2, 0xE8, 0xF0);          // Slate 200
    public static final int CARD_RADIUS = 12;

    public static final Color INPUT_BACKGROUND = Color.WHITE;
    public static final Color INPUT_BORDER = new Color(0xCB, 0xD5, 0xE1);         // Slate 300
    public static final Color INPUT_FOCUS_BORDER = new Color(0x4F, 0x46, 0xE5);   // Indigo 600
    public static final int INPUT_RADIUS = 8;
    public static final int INPUT_HEIGHT = 44;

    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);         // Slate 900
    public static final Color TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);       // Slate 600
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);           // Slate 500
    public static final Color TEXT_PLACEHOLDER = new Color(0x94, 0xA3, 0xB8);     // Slate 400
    public static final Color EYEBROW_TEXT = new Color(0x4F, 0x46, 0xE5);         // Indigo 600

    public static final Color BUTTON_DARK_BG = new Color(0x4F, 0x46, 0xE5);       // Indigo 600
    public static final Color BUTTON_DARK_HOVER = new Color(0x43, 0x38, 0xCA);    // Indigo 700
    public static final Color BUTTON_DARK_PRESSED = new Color(0x37, 0x30, 0xA3);  // Indigo 800
    public static final Color BUTTON_DARK_DISABLED = new Color(0x94, 0xA3, 0xB8); // Slate 400

    public static final Color PILL_BUTTON_BG = new Color(0x0F, 0x17, 0x2A);       // Slate 900
    public static final Color NUMBER_CIRCLE_BG = new Color(0x4F, 0x46, 0xE5);     // Indigo 600
    public static final Color CHIP_BG = new Color(0x1E, 0x29, 0x3B);              // Slate 800
    public static final Color ERROR_COLOR = new Color(0xDC, 0x26, 0x26);          // Red 600

    public static final Color CARD_DIVIDER = new Color(0xE2, 0xE8, 0xF0);         // Slate 200

    // Dimensions
    public static final Dimension WINDOW_DEFAULT = new Dimension(1280, 832);
    public static final Dimension WINDOW_MINIMUM = new Dimension(640, 500);
    public static final int MAX_CONTENT_WIDTH = 1180;
    public static final int NAVBAR_HEIGHT = 64;

    // Responsive Breakpoints
    public static final int BREAKPOINT_TINY = 480;
    public static final int BREAKPOINT_COMPACT = 650;
    public static final int BREAKPOINT_NAV_COLLAPSE = 860;
    public static final int BREAKPOINT_LARGE = 960;

    public enum ScreenSize {
        TINY,
        COMPACT,
        MEDIUM,
        WIDE;

        public static ScreenSize of(int width) {
            if (width < BREAKPOINT_TINY) return TINY;
            if (width < BREAKPOINT_COMPACT) return COMPACT;
            if (width < BREAKPOINT_LARGE) return MEDIUM;
            return WIDE;
        }
    }
}
