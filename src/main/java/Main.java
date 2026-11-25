import com.campusfind.ui.pages.CampusFindDesktopFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.GraphicsEnvironment;

/**
 * Main Entry Point - CampusFind v2.0 (Aplikasi Desktop Java Swing)
 */
public class Main {

    public static void main(String[] args) {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                tampilkanPesanHeadless(null);
                return;
            }
            // Gunakan CrossPlatform L&F + paksa palet Light agar tema gelap OS (GTK Dark) tidak merusak tabel & scrollpane
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            terapkanTemaTerang();
        } catch (Throwable t) {
            tampilkanPesanHeadless(t.getMessage());
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                CampusFindDesktopFrame frame = new CampusFindDesktopFrame();
                frame.setVisible(true);
            } catch (Throwable ex) {
                System.err.println("[ERROR] Gagal membuka aplikasi desktop Java: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private static void terapkanTemaTerang() {
        Color white = Color.WHITE;
        Color slateBg = new Color(241, 245, 249);
        Color slateText = new Color(15, 23, 42);
        Color slateBorder = new Color(226, 232, 240);

        UIManager.put("Panel.background", slateBg);
        UIManager.put("Viewport.background", white);
        UIManager.put("ScrollPane.background", white);
        UIManager.put("Table.background", white);
        UIManager.put("Table.foreground", slateText);
        UIManager.put("Table.gridColor", new Color(241, 245, 249));
        UIManager.put("TableHeader.background", new Color(248, 250, 252));
        UIManager.put("TableHeader.foreground", new Color(51, 65, 85));
        UIManager.put("TabbedPane.background", white);
        UIManager.put("TabbedPane.foreground", slateText);
        UIManager.put("TabbedPane.selected", new Color(239, 246, 255));
        UIManager.put("TabbedPane.contentAreaColor", white);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.borderHightlightColor", slateBorder);
        UIManager.put("TabbedPane.darkShadow", slateBorder);
        UIManager.put("TabbedPane.shadow", slateBorder);
        UIManager.put("TabbedPane.light", white);
        UIManager.put("ComboBox.background", white);
        UIManager.put("ComboBox.foreground", slateText);
        UIManager.put("TextField.background", white);
        UIManager.put("TextField.foreground", slateText);
        UIManager.put("TextArea.background", new Color(248, 250, 252));
        UIManager.put("TextArea.foreground", slateText);
        UIManager.put("OptionPane.background", white);
    }

    private static void tampilkanPesanHeadless(String detail) {
        System.out.println("===============================================================");
        System.out.println("  [CAMPUSFIND v2.0] APLIKASI DESKTOP JAVA");
        System.out.println("===============================================================");
        System.out.println("[INFO] Aplikasi desktop Swing memerlukan sesi grafis (X11 / Wayland).");
        if (detail != null && !detail.isBlank()) {
            System.out.println("Detail: " + detail);
        }
        System.out.println("===============================================================");
    }
}
