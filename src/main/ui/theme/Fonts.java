package com.campusfind.ui.theme;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Fonts {

    private static final String SERIF_FAMILY;
    private static final String SANS_FAMILY;

    static {
        Set<String> available = new HashSet<>(
                Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
        );

        // Serif resolution
        if (available.contains("Playfair Display")) {
            SERIF_FAMILY = "Playfair Display";
        } else if (available.contains("Newsreader")) {
            SERIF_FAMILY = "Newsreader";
        } else if (available.contains("Georgia")) {
            SERIF_FAMILY = "Georgia";
        } else {
            SERIF_FAMILY = Font.SERIF;
        }

        // Sans resolution
        if (available.contains("Inter")) {
            SANS_FAMILY = "Inter";
        } else if (available.contains("Segoe UI")) {
            SANS_FAMILY = "Segoe UI";
        } else {
            SANS_FAMILY = Font.SANS_SERIF;
        }
    }

    private Fonts() {}

    public static Font serif(float size, int style) {
        return new Font(SERIF_FAMILY, style, (int) size).deriveFont(size);
    }

    public static Font sans(float size, int style) {
        return new Font(SANS_FAMILY, style, (int) size).deriveFont(size);
    }

    public static Font trackedSans(float size, int style, double tracking) {
        Font font = sans(size, style);
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, tracking);
        return font.deriveFont(attributes);
    }
}
