package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PosService;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.ui.theme.Theme;
import com.campusfind.validation.IdentitasValidator;
import com.campusfind.validation.ValidationResult;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class NavigationController {

    private final JFrame mainFrame;
    private final ItemService itemService;
    private final KlaimService klaimService;
    private final PosService posService;
    private final NotificationService notificationService;
    private final KandidatService kandidatService;
    private final AppSession appSession;

    private final Deque<String> historyStack = new ArrayDeque<>();
    private java.util.function.Consumer<JComponent> viewSetter;
    private java.util.function.Consumer<Integer> notificationCountUpdater;

    public NavigationController(JFrame mainFrame,
                                ItemService itemService,
                                KlaimService klaimService,
                                PosService posService,
                                NotificationService notificationService,
                                KandidatService kandidatService,
                                AppSession appSession) {
        this.mainFrame = mainFrame;
        this.itemService = itemService;
        this.klaimService = klaimService;
        this.posService = posService;
        this.notificationService = notificationService;
        this.kandidatService = kandidatService;
        this.appSession = appSession;
    }

    public NavigationController(JFrame mainFrame,
                                ItemService itemService,
                                KlaimService klaimService,
                                PosService posService,
                                NotificationService notificationService,
                                AppSession appSession) {
        this(mainFrame, itemService, klaimService, posService, notificationService, null, appSession);
    }

    public void setViewSetter(java.util.function.Consumer<JComponent> viewSetter) {
        this.viewSetter = viewSetter;
    }

    public void setNotificationCountUpdater(java.util.function.Consumer<Integer> updater) {
        this.notificationCountUpdater = updater;
    }

    public void updateNotificationCount() {
        if (notificationCountUpdater != null && appSession.isLoggedIn()) {
            int unread = notificationService.countBelumDibaca(appSession.getNomorIdentitas());
            notificationCountUpdater.accept(unread);
        }
    }

    public void navigateToTemuan() {
        pushHistory("TEMUAN");
        viewSetter.accept(new TemuanPage(this, itemService, posService));
        updateNotificationCount();
    }

    public void navigateToLapor() {
        pushHistory("LAPOR");
        viewSetter.accept(new LaporPage(this, itemService, posService, appSession));
        updateNotificationCount();
    }

    public void navigateToKlaim(String itemId) {
        pushHistory("KLAIM:" + itemId);
        viewSetter.accept(new KlaimPage(this, itemId, itemService, klaimService, posService, appSession));
        updateNotificationCount();
    }

    public void navigateToPos() {
        pushHistory("POS");
        viewSetter.accept(new PosPage(this, posService));
        updateNotificationCount();
    }

    public void navigateToBantuan() {
        pushHistory("BANTUAN");
        viewSetter.accept(new BantuanPage(this));
        updateNotificationCount();
    }

    public void navigateBack() {
        if (historyStack.size() > 1) {
            historyStack.pop(); // pop current
            String previous = historyStack.pop(); // get previous
            restorePage(previous);
        } else {
            navigateToTemuan();
        }
    }

    private void pushHistory(String token) {
        if (historyStack.isEmpty() || !historyStack.peek().equals(token)) {
            historyStack.push(token);
        }
    }

    private void restorePage(String token) {
        if (token.startsWith("KLAIM:")) {
            String id = token.substring(6);
            navigateToKlaim("null".equals(id) || id.isBlank() ? null : id);
        } else if ("LAPOR".equals(token)) {
            navigateToLapor();
        } else if ("POS".equals(token)) {
            navigateToPos();
        } else if ("BANTUAN".equals(token)) {
            navigateToBantuan();
        } else {
            navigateToTemuan();
        }
    }

    public void ensureLoggedIn(Runnable onSuccess) {
        if (appSession.isLoggedIn()) {
            onSuccess.run();
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 8));
        JLabel prompt = new JLabel("Masukkan Nomor Identitas Anda (NIM / NIP):");
        prompt.setFont(Fonts.sans(13f, Font.PLAIN));
        JTextField inputField = new JTextField();
        panel.add(prompt);
        panel.add(inputField);

        int result = JOptionPane.showConfirmDialog(
                mainFrame,
                panel,
                "Identifikasi Pengguna",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String nomor = inputField.getText().trim();
            ValidationResult v = IdentitasValidator.validate(nomor);
            if (!v.isValid()) {
                JOptionPane.showMessageDialog(mainFrame, v.getFirstError(), "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            appSession.setIdentitas(nomor, "Pengguna " + nomor);
            updateNotificationCount();
            onSuccess.run();
        }
    }

    public void showKlaimSaya() {
        ensureLoggedIn(() -> {
            KlaimSayaDialog dialog = new KlaimSayaDialog(mainFrame, klaimService, itemService, posService, appSession, this);
            dialog.setVisible(true);
        });
    }

    public void showNotifikasi() {
        ensureLoggedIn(() -> {
            NotifikasiDialog dialog = new NotifikasiDialog(mainFrame, notificationService, appSession, this);
            dialog.setVisible(true);
            updateNotificationCount();
        });
    }

    public void showKandidatCocok(Item laporanHilang) {
        ensureLoggedIn(() -> {
            KandidatCocokDialog dialog = new KandidatCocokDialog(mainFrame, laporanHilang, kandidatService, this, appSession);
            dialog.setVisible(true);
        });
    }

    public void showKandidatCocokById(String laporanHilangId) {
        if (laporanHilangId == null || laporanHilangId.isBlank()) return;
        Item item = itemService.findById(laporanHilangId).orElse(null);
        if (item != null) {
            showKandidatCocok(item);
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Laporan kehilangan tidak ditemukan.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void showPrivasiDialog() {
        String msg = "Kebijakan Privasi CampusFind:\n\n" +
                "1. Data yang disimpan adalah informasi laporan barang hilang/temuan,\n" +
                "   nomor identitas pelapor/pengklaim, berkas bukti, dan jadwal serah terima.\n" +
                "2. Ciri rahasia barang hanya digunakan untuk sistem verifikasi internal\n" +
                "   dan tidak pernah ditampilkan ke publik.\n" +
                "3. Berkas disimpan secara lokal di repositori penyimpanan kampus.";
        JOptionPane.showMessageDialog(mainFrame, msg, "Privasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showKontakDaruratDialog() {
        List<PosPenyimpanan> posList = posService.findAll();
        StringBuilder sb = new StringBuilder("Daftar Pos Jaga Resmi CampusFind:\n\n");
        for (PosPenyimpanan pos : posList) {
            sb.append("• ").append(pos.getNama()).append("\n")
              .append("  Lokasi: ").append(pos.getLokasiPenyerahan()).append("\n\n");
        }
        sb.append("Silakan kunjungi pos di atas secara langsung untuk pelaporan darurat.");
        JOptionPane.showMessageDialog(mainFrame, sb.toString(), "Kontak Darurat", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showSuccessDialog(String message, String kode) {
        SuccessDialog dialog = new SuccessDialog(mainFrame, message, kode);
        dialog.setVisible(true);
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }
}
