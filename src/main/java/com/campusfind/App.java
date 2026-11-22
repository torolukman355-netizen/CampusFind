package com.campusfind;

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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
        PosService posService = new PosService(posRepository);
        NotificationService notificationService = new NotificationService(notificationRepository);
        QRCodeService qrCodeService = new QRCodeService();
        PetugasService petugasService = new PetugasService(itemRepository, posRepository, notificationService, qrCodeService);

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
            AppFrame frame = new AppFrame(
                    itemService,
                    klaimService,
                    klaimRepository,
                    posService,
                    notificationService,
                    kandidatService,
                    petugasService,
                    qrCodeService,
                    appSession
            );
            frame.setVisible(true);
            LOG.info("Aplikasi CampusFind berhasil dijalankan.");
        });
    }
}