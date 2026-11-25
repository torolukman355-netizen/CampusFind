<<<<<<< HEAD:src/main/legacy/PetugasDashboardPanel.java
package com.campusfind.legacy;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
=======
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.FileKlaimRepository;
import com.campusfind.repository.FileNotificationRepository;
import com.campusfind.repository.FilePosRepository;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.CandidateMatchingStrategy;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingStrategy;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PetugasService;
import com.campusfind.service.QRCodeService;
import com.campusfind.service.TokenOverlapStrategy;
import com.campusfind.service.WeightedAttributeStrategy;
import com.campusfind.ui.pages.PetugasPage;
>>>>>>> ca2e27ff81bd0eaf0f0496ef3f356f4006abcbb8:src/main/java/PetugasDashboardPanel.java

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.time.Clock;

/**
 * =============================================================================
 * CampusFind - Dashboard & Antrean Kerja Petugas (Bintang, Jepin, Fakhri)
 * =============================================================================
 * Memenuhi seluruh spesifikasi User Stories:
 * - US-001 s.d. US-008 : Arsitektur OOP Bersih, Hirarki Item & User, Polimorfisme Item.tampilkanDetail()
 * - US-010 & US-028    : Antrean Kerja & Background Smart Matching (SwingWorker)
 * - US-011 & US-028    : Verifikasi Laporan Masuk (Terima / Tolak dengan Wajib Alasan)
 * - US-012             : Kontrol Transisi Status Berurutan & Audit Logging
 * - US-013             : Manajemen Lokasi Penyimpanan Fisik (Khusus Temuan)
 * - US-015             : Konfirmasi Rekomendasi Kecocokan (COCOK)
 * - US-017, US-018, US-029 : Manajemen & Pelacakan QR Code Barang Temuan
 * - US-020, US-028    : Pemeriksaan & Keputusan Klaim Mahasiswa
 * - US-021, US-029    : Pencatatan Serah Terima Barang & Penguncian Data Permanen
 * =============================================================================
 */
public class PetugasDashboardPanel extends JPanel {

    private final String namaPetugas;
    private final PetugasPage petugasPage;

    public PetugasDashboardPanel() {
        this("Bintang");
    }

    public PetugasDashboardPanel(String namaPetugas) {
        this.namaPetugas = (namaPetugas != null && !namaPetugas.isBlank()) ? namaPetugas : "Bintang";
        setLayout(new BorderLayout());

        // Inisialisasi Data Store & Repository CampusFind
        FileDataStore dataStore = new FileDataStore();
        ItemRepository itemRepository = new FileItemRepository(dataStore);
        KlaimRepository klaimRepository = new FileKlaimRepository(dataStore);
        PosRepository posRepository = new FilePosRepository(dataStore);
        NotificationRepository notificationRepository = new FileNotificationRepository(dataStore);

        Clock clock = Clock.systemDefaultZone();
        MatchingStrategy matchingStrategy = new TokenOverlapStrategy();

        ItemService itemService = new ItemService(itemRepository, posRepository, clock);
        KlaimService klaimService = new KlaimService(
                klaimRepository,
                itemRepository,
                posRepository,
                notificationRepository,
                matchingStrategy,
                clock
        );
        NotificationService notificationService = new NotificationService(notificationRepository);
        QRCodeService qrCodeService = new QRCodeService();
        PetugasService petugasService = new PetugasService(itemRepository, posRepository, notificationService, qrCodeService);

        CandidateMatchingStrategy candidateStrategy = new WeightedAttributeStrategy();
        KandidatService kandidatService = new KandidatService(itemRepository, candidateStrategy);

        // Inisialisasi Sesi Petugas Aktif
        AppSession appSession = new AppSession();
        appSession.setIdentitas("199504122020121001", this.namaPetugas);

        // Instansiasi UI Halaman Petugas Profesional
        this.petugasPage = new PetugasPage(
                itemService,
                klaimService,
                klaimRepository,
                kandidatService,
                petugasService,
                qrCodeService,
                appSession
        );

        add(petugasPage, BorderLayout.CENTER);
    }

    public void refreshAllQueues() {
        if (petugasPage != null) {
            petugasPage.refreshAllQueues();
        }
    }

    public PetugasPage getPetugasPage() {
        return petugasPage;
    }

    public String getNamaPetugas() {
        return namaPetugas;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CampusFind — Dashboard Petugas (Bintang)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1240, 740);
            frame.setLocationRelativeTo(null);
            frame.add(new PetugasDashboardPanel("Bintang"));
            frame.setVisible(true);
        });
    }
}
