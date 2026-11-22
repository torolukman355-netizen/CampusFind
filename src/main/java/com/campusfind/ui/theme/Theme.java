package com.campusfind.ui.theme;

import java.awt.Color;
import java.awt.Dimension;

public final class Theme {

    private Theme() {}

    // Colors
    public static final Color PAGE_BACKGROUND = new Color(0xFB, 0xF8, 0xF2);
    public static final Color NAVBAR_BACKGROUND = new Color(0xE9, 0xE7, 0xE4);
    public static final Color NAVBAR_BORDER = new Color(0xDA, 0xD6, 0xD0);
    public static final Color NAVBAR_LINK = new Color(0x4A, 0x43, 0x3C);
    public static final Color NAVBAR_LINK_HOVER = new Color(0x2A, 0x24, 0x20);

    public static final Color CARD_BACKGROUND = new Color(0xF4, 0xEF, 0xE6);
    public static final Color CARD_BORDER = new Color(0xE8, 0xE1, 0xD5);
    public static final int CARD_RADIUS = 8;

    public static final Color INPUT_BACKGROUND = new Color(0xFB, 0xF9, 0xF4);
    public static final Color INPUT_BORDER = new Color(0xE2, 0xDA, 0xCB);
    public static final Color INPUT_FOCUS_BORDER = new Color(0x5C, 0x4B, 0x3C);
    public static final int INPUT_RADIUS = 6;
    public static final int INPUT_HEIGHT = 44;

    public static final Color TEXT_PRIMARY = new Color(0x2A, 0x24, 0x20);
    public static final Color TEXT_SECONDARY = new Color(0x6E, 0x65, 0x5C);
    public static final Color TEXT_MUTED = new Color(0x8A, 0x7F, 0x73);
    public static final Color TEXT_PLACEHOLDER = new Color(0xA5, 0x9B, 0x8F);
    public static final Color EYEBROW_TEXT = new Color(0x7A, 0x6E, 0x62);

    public static final Color BUTTON_DARK_BG = new Color(0x2B, 0x26, 0x22);
    public static final Color BUTTON_DARK_HOVER = new Color(0x3E, 0x37, 0x31);
    public static final Color BUTTON_DARK_PRESSED = new Color(0x1E, 0x1A, 0x17);
    public static final Color BUTTON_DARK_DISABLED = new Color(0x8F, 0x87, 0x7E);

    public static final Color PILL_BUTTON_BG = new Color(0x1F, 0x1B, 0x18);
    public static final Color NUMBER_CIRCLE_BG = new Color(0x5C, 0x4B, 0x3C);
    public static final Color CHIP_BG = new Color(0x4B, 0x44, 0x3D);
    public static final Color ERROR_COLOR = new Color(0xB3, 0x40, 0x2F);

    public static final Color CARD_DIVIDER = new Color(0xE3, 0xDC, 0xCF);

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
