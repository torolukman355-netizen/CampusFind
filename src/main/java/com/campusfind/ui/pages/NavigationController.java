package com.campusfind.ui.pages;

import com.campusfind.model.Item;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PetugasService;
import com.campusfind.service.PosService;
import com.campusfind.service.QRCodeService;
import com.campusfind.ui.theme.Fonts;
import com.campusfind.validation.IdentitasValidator;
import com.campusfind.validation.ValidationResult;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class NavigationController {

    private final JFrame mainFrame;
    private final ItemService itemService;
    private final KlaimService klaimService;
    private final KlaimRepository klaimRepository;
    private final PosService posService;
    private final NotificationService notificationService;
    private final KandidatService kandidatService;
    private final PetugasService petugasService;
    private final QRCodeService qrCodeService;
    private final AppSession appSession;

    private final Deque<String> historyStack = new ArrayDeque<>();
    private java.util.function.Consumer<JComponent> viewSetter;
    private java.util.function.Consumer<Integer> notificationCountUpdater;

    public NavigationController(JFrame mainFrame,
                                ItemService itemService,
                                KlaimService klaimService,
                                KlaimRepository klaimRepository,
                                PosService posService,
                                NotificationService notificationService,
                                KandidatService kandidatService,
                                PetugasService petugasService,
                                QRCodeService qrCodeService,
                                AppSession appSession) {
        this.mainFrame = mainFrame;
        this.itemService = itemService;
        this.klaimService = klaimService;
        this.klaimRepository = klaimRepository;
        this.posService = posService;
        this.notificationService = notificationService;
        this.kandidatService = kandidatService;
        this.petugasService = petugasService;
        this.qrCodeService = qrCodeService;
        this.appSession = appSession;
    }

    public NavigationController(JFrame mainFrame,
                                ItemService itemService,
                                KlaimService klaimService,
                                PosService posService,
                                NotificationService notificationService,
                                KandidatService kandidatService,
                                AppSession appSession) {
        this(mainFrame, itemService, klaimService, null, posService, notificationService, kandidatService, null, null, appSession);
    }

    public NavigationController(JFrame mainFrame,
                                ItemService itemService,
                                KlaimService klaimService,
                                PosService posService,
                                NotificationService notificationService,
                                AppSession appSession) {
        this(mainFrame, itemService, klaimService, null, posService, notificationService, null, null, null, appSession);
    }

    public void setViewSetter(java.util.function.Consumer<JComponent> viewSetter) {
        this.viewSetter = viewSetter;
    }

    public void setNotificationCountUpdater(java.util.function.Consumer<Integer> updater) {
        this.notificationCountUpdater = updater;
    }

    public void updateNotificationCount() {
        if (notificationCountUpdater != null) {
            if (appSession.isLoggedIn()) {
                int unread = notificationService.countBelumDibaca(appSession.getNomorIdentitas());
                notificationCountUpdater.accept(unread);
            } else {
                notificationCountUpdater.accept(0);
            }
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

    public void navigateToPetugas() {
        ensureLoggedIn(() -> {
            String nomor = appSession.getNomorIdentitas();
            if (nomor == null || nomor.length() == 9) {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Akses Ditolak: Dashboard Petugas hanya dapat diakses menggunakan NIP Petugas (bukan NIM Mahasiswa).",
                        "Akses Ditolak",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            pushHistory("PETUGAS");
            viewSetter.accept(new PetugasPage(
                    itemService,
                    klaimService,
                    klaimRepository,
                    kandidatService,
                    petugasService,
                    qrCodeService,
                    appSession
            ));
            updateNotificationCount();
        });
    }

    public void logout() {
        if (!appSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(mainFrame, "Belum ada akun yang aktif.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        appSession.clear();
        updateNotificationCount();
        navigateToTemuan();
        JOptionPane.showMessageDialog(mainFrame, "Sesi akun telah keluar. Silakan login kembali saat dibutuhkan.", "Logout Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    public void navigateBack() {
        if (historyStack.size() > 1) {
            historyStack.pop();
            String previous = historyStack.pop();
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
        } else if ("PETUGAS".equals(token)) {
            navigateToPetugas();
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
        JLabel prompt = new JLabel("Masukkan Nomor Identitas Anda (NIM Mahasiswa / NIP Petugas):");
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
            String labelRole = (nomor.length() == 9) ? "Mahasiswa " + nomor : "Petugas " + nomor;
            appSession.setIdentitas(nomor, labelRole);
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