import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;

/**
 * Main Entry Point - CampusFind Petugas Dashboard (Bintang)
 */
public class Main {

    public static void main(String[] args) {
        // Tangani pengecekan display dan Headless secara aman dari AWTError
        try {
            if (GraphicsEnvironment.isHeadless()) {
                tampilkanPesanHeadless(null);
                return;
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable t) {
            tampilkanPesanHeadless(t.getMessage());
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("CampusFind - Dashboard Petugas (Bintang)");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1240, 740);
                frame.setLocationRelativeTo(null);
                frame.add(new PetugasDashboardPanel("Bintang"));
                frame.setVisible(true);
            } catch (Throwable ex) {
                System.err.println("[ERROR] Gagal membuka antarmuka GUI: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }

    private static void tampilkanPesanHeadless(String detail) {
        System.out.println("===============================================================");
        System.out.println("  [CAMPUSFIND] DASHBOARD ROLE PETUGAS (BINTANG)");
        System.out.println("===============================================================");
        System.out.println("[INFO] Aplikasi desktop Swing memerlukan sesi grafis (X11 / Wayland).");
        if (detail != null && !detail.isBlank()) {
            System.out.println("Detail: " + detail);
        }
        System.out.println("Tips:");
        System.out.println("1. Jika menggunakan IntelliJ IDEA: Klik tombol 'Run' hijau pada kelas Main.");
        System.out.println("2. Jika menggunakan Linux Terminal: Pastikan berada di sesi desktop aktif.");
        System.out.println("===============================================================");
    }
}
