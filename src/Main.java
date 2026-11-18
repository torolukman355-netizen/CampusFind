import repository.DatabaseManager;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Main Entry Point - CampusFind
 * Mendukung 2 Mode Eksekusi:
 * 1. GUI Mode (Desktop Swing) jika ada server X11 / Layar Desktop (default pada IntelliJ / Windows / Linux Desktop).
 * 2. CLI Console Mode jika dijalankan di Terminal Headless (WSL, SSH) agar tidak terjadi HeadlessException.
 */
public class Main {
    public static void main(String[] args) {
        // Muat data tersimpan dari database lokal
        DatabaseManager.muatDariFile();

        boolean forceCli = false;
        if (args.length > 0 && ("--cli".equalsIgnoreCase(args[0]) || "-c".equalsIgnoreCase(args[0]))) {
            forceCli = true;
        }

        // Cek apakah lingkungan headless (tidak ada X11 display / terminal Linux murni)
        if (forceCli || GraphicsEnvironment.isHeadless()) {
            System.out.println("===============================================================");
            System.out.println("[INFO] Berjalan dalam Mode Console CLI (Headless Environment).");
            System.out.println("Tips GUI: Jalankan dari IntelliJ IDEA / VS Code Desktop atau set DISPLAY=:0");
            System.out.println("===============================================================\n");

            PetugasConsoleUI consoleUI = new PetugasConsoleUI();
            consoleUI.start();
        } else {
            // Jalankan GUI Swing (Desktop Mode)
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }
}
