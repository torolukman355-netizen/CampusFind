package com.campusfind;

import com.campusfind.exception.StorageException;
import com.campusfind.repository.DatabaseManager;
import com.campusfind.repository.DatabaseSchema;
import com.campusfind.repository.DatabaseSeeder;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.FileKlaimRepository;
import com.campusfind.repository.FileNotificationRepository;
import com.campusfind.repository.FilePetugasAuditRepository;
import com.campusfind.repository.FilePosRepository;
import com.campusfind.repository.FileUserRepository;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.JdbcItemRepository;
import com.campusfind.repository.JdbcKlaimRepository;
import com.campusfind.repository.JdbcNotificationRepository;
import com.campusfind.repository.JdbcPetugasAuditRepository;
import com.campusfind.repository.JdbcPosRepository;
import com.campusfind.repository.JdbcUserRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PetugasAuditRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.repository.UserRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.CandidateMatchingStrategy;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatNotifier;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingStrategy;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PetugasService;
import com.campusfind.service.PosService;
import com.campusfind.service.QRCodeService;
import com.campusfind.service.TokenOverlapStrategy;
import com.campusfind.service.WeightedAttributeStrategy;
import com.campusfind.ui.pages.AppFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.time.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Tidak dapat mengatur System Look & Feel", e);
        }

        ItemRepository itemRepository;
        KlaimRepository klaimRepository;
        PosRepository posRepository;
        NotificationRepository notificationRepository;
        UserRepository userRepository;
        PetugasAuditRepository petugasAuditRepository;
        String storageWarning = null;

        try {
            DatabaseManager databaseManager = new DatabaseManager();
            DatabaseSchema.ensureSchema(databaseManager);
            itemRepository = new JdbcItemRepository(databaseManager);
            klaimRepository = new JdbcKlaimRepository(databaseManager);
            posRepository = new JdbcPosRepository(databaseManager);
            notificationRepository = new JdbcNotificationRepository(databaseManager);
            userRepository = new JdbcUserRepository(databaseManager);
            petugasAuditRepository = new JdbcPetugasAuditRepository(databaseManager);
            DatabaseSeeder.seedIfEmpty(posRepository, itemRepository, klaimRepository, notificationRepository, userRepository);
            LOG.info("Penyimpanan data: PostgreSQL " + databaseManager.safeUrl());
        } catch (StorageException e) {
            LOG.log(Level.WARNING,
                    "Database tidak tersedia, beralih ke penyimpanan file: " + e.getMessage());
            FileDataStore dataStore = new FileDataStore();
            itemRepository = new FileItemRepository(dataStore);
            klaimRepository = new FileKlaimRepository(dataStore);
            posRepository = new FilePosRepository(dataStore);
            notificationRepository = new FileNotificationRepository(dataStore);
            userRepository = new FileUserRepository();
            petugasAuditRepository = new FilePetugasAuditRepository(dataStore);
            DatabaseSeeder.seedIfEmpty(posRepository, itemRepository, klaimRepository, notificationRepository, userRepository);
            storageWarning = "Database tidak dapat dihubungi.\n\n"
                    + e.getMessage()
                    + "\n\nAplikasi tetap berjalan dengan penyimpanan file lokal "
                    + "(data/campusfind.dat).";
        }

        final String warning = storageWarning;
        final KlaimRepository klaimRepositoryFinal = klaimRepository;
        final UserRepository userRepositoryFinal = userRepository;

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
        PosService posService = new PosService(posRepository);
        NotificationService notificationService = new NotificationService(notificationRepository);
        QRCodeService qrCodeService = new QRCodeService();
        PetugasService petugasService = new PetugasService(
                itemRepository,
                posRepository,
                notificationService,
                qrCodeService,
                petugasAuditRepository
        );

        CandidateMatchingStrategy candidateMatchingStrategy = new WeightedAttributeStrategy();
        KandidatService kandidatService = new KandidatService(itemRepository, candidateMatchingStrategy);
        KandidatNotifier kandidatNotifier = new KandidatNotifier(
                itemRepository,
                candidateMatchingStrategy,
                notificationService,
                notificationRepository
        );
        itemService.addItemCreatedListener(kandidatNotifier);
        AppSession appSession = new AppSession();

        SwingUtilities.invokeLater(() -> {
            if (warning != null && !GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        warning,
                        "Mode Penyimpanan File",
                        JOptionPane.WARNING_MESSAGE);
            }
            AppFrame frame = new AppFrame(
                    itemService,
                    klaimService,
                    klaimRepositoryFinal,
                    posService,
                    notificationService,
                    kandidatService,
                    petugasService,
                    qrCodeService,
                    userRepositoryFinal,
                    appSession
            );
            frame.setVisible(true);
            LOG.info("Aplikasi CampusFind berhasil dijalankan.");
        });
    }
}