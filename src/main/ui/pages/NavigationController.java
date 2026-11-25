package com.campusfind.ui.pages;

import com.campusfind.exception.AksesDitolakException;
import com.campusfind.model.HakAkses;
import com.campusfind.model.Item;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.Role;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.UserRepository;
import com.campusfind.service.AksesService;
import com.campusfind.service.AppSession;
import com.campusfind.service.AuthService;
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

import javax.swing.JComboBox;
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
    private final UserRepository userRepository;
    private final AppSession appSession;
    private final AuthService authService;
    private final AksesService aksesService;

    private final Deque<String> historyStack = new ArrayDeque<>();
    private java.util.function.Consumer<JComponent> viewSetter;
    private java.util.function.Consumer<Integer> notificationCountUpdater;
    private java.util.function.Consumer<Boolean> navBarVisibilityHandler;
    private Runnable sessionChangeListener;
    private Runnable pendingLoginAction;

    public NavigationController(JFrame mainFrame,
                                ItemService itemService,
                                KlaimService klaimService,
                                KlaimRepository klaimRepository,
                                PosService posService,
                                NotificationService notificationService,
                                KandidatService kandidatService,
                                PetugasService petugasService,
                                QRCodeService qrCodeService,
                                UserRepository userRepository,
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
        this.userRepository = userRepository;
        this.appSession = appSession;
        this.authService = new AuthService(appSession, userRepository);
        this.aksesService = new AksesService(appSession);
    }

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
        this(mainFrame, itemService, klaimService, klaimRepository, posService, notificationService,
                kandidatService, petugasService, qrCodeService, null, appSession);
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

    public AuthService getAuthService() {
        return authService;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setViewSetter(java.util.function.Consumer<JComponent> viewSetter) {
        this.viewSetter = viewSetter;
    }

    public void setNotificationCountUpdater(java.util.function.Consumer<Integer> updater) {
        this.notificationCountUpdater = updater;
    }

    public void setNavBarVisibilityHandler(java.util.function.Consumer<Boolean> handler) {
        this.navBarVisibilityHandler = handler;
    }

    public void setNavBarVisible(boolean visible) {
        if (navBarVisibilityHandler != null) {
            navBarVisibilityHandler.accept(visible);
        }
    }

    public Runnable consumePendingLoginAction() {
        Runnable action = this.pendingLoginAction;
        this.pendingLoginAction = null;
        return action;
    }

    /**
     * Listener yang dipanggil setiap kali sesi login berubah (login/logout),
     * dipakai agar menu navigasi dapat menyesuaikan role.
     */
    public void setSessionChangeListener(Runnable listener) {
        this.sessionChangeListener = listener;
    }

    private void notifySessionChanged() {
        if (sessionChangeListener != null) {
            sessionChangeListener.run();
        }
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

    // ---------------------------------------------------------------
    // Status sesi & hak akses (dipakai juga oleh NavBar)
    // ---------------------------------------------------------------

    public boolean isLoggedIn() {
        return appSession.isLoggedIn();
    }

    public boolean boleh(HakAkses hakAkses) {
        return aksesService.boleh(hakAkses);
    }

    public Role getRoleSesi() {
        return appSession.getRole();
    }

    public String getNamaSesi() {
        return appSession.getNama();
    }

    // ---------------------------------------------------------------
    // Navigasi halaman
    // ---------------------------------------------------------------

    public void navigateToLogin() {
        pushHistory("LOGIN");
        setNavBarVisible(false);
        viewSetter.accept(new LoginPage(this, authService, appSession));
    }

    public void navigateToTemuan() {
        setNavBarVisible(true);
        pushHistory("TEMUAN");
        viewSetter.accept(new TemuanPage(this, itemService, posService));
        updateNotificationCount();
    }

    public void navigateToLapor() {
        denganHak(HakAkses.BUAT_LAPORAN,
                "Pelaporan barang hanya dapat dilakukan oleh Mahasiswa.",
                () -> {
                    setNavBarVisible(true);
                    pushHistory("LAPOR");
                    viewSetter.accept(new LaporPage(this, itemService, posService, appSession));
                    updateNotificationCount();
                });
    }

    public void navigateToKlaim(String itemId) {
        denganHak(HakAkses.AJUKAN_KLAIM,
                "Pengajuan klaim hanya dapat dilakukan oleh Mahasiswa.",
                () -> {
                    setNavBarVisible(true);
                    pushHistory("KLAIM:" + itemId);
                    viewSetter.accept(new KlaimPage(this, itemId, itemService, klaimService, posService, appSession));
                    updateNotificationCount();
                });
    }

    public void navigateToPos() {
        setNavBarVisible(true);
        pushHistory("POS");
        viewSetter.accept(new PosPage(this, posService));
        updateNotificationCount();
    }

    public void navigateToBantuan() {
        setNavBarVisible(true);
        pushHistory("BANTUAN");
        viewSetter.accept(new BantuanPage(this));
        updateNotificationCount();
    }

    public void navigateToPetugas() {
        denganHak(HakAkses.VERIFIKASI_LAPORAN,
                "Dashboard Petugas hanya dapat diakses oleh Petugas atau Admin.",
                () -> {
                    setNavBarVisible(true);
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

    public void navigateToAdmin() {
        denganHak(HakAkses.LIHAT_STATISTIK,
                "Dashboard Admin hanya dapat diakses oleh Admin.",
                () -> {
                    setNavBarVisible(true);
                    pushHistory("ADMIN");
                    viewSetter.accept(new AdminPage(this, itemService, klaimRepository, appSession));
                    updateNotificationCount();
                });
    }

    public void logout() {
        if (!appSession.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        authService.logout();
        updateNotificationCount();
        notifySessionChanged();
        navigateToLogin();
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
        } else if ("ADMIN".equals(token)) {
            navigateToAdmin();
        } else if ("LOGIN".equals(token)) {
            navigateToLogin();
        } else {
            navigateToTemuan();
        }
    }

    // ---------------------------------------------------------------
    // Autentikasi & otorisasi
    // ---------------------------------------------------------------

    /**
     * Menjalankan aksi hanya bila pengguna sudah login dan memiliki hak akses.
     * Bila belum login, dialog login ditampilkan lebih dahulu.
     */
    private void denganHak(HakAkses hakAkses, String pesanTolak, Runnable aksi) {
        ensureLoggedIn(() -> {
            try {
                aksesService.wajibPunyaHak(hakAkses);
            } catch (AksesDitolakException ex) {
                tampilkanAksesDitolak(pesanTolak != null ? pesanTolak : ex.getMessage());
                return;
            }
            aksi.run();
        });
    }

    private void tampilkanAksesDitolak(String pesan) {
        JOptionPane.showMessageDialog(mainFrame, pesan, "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Membuka halaman login tanpa melanjutkan ke halaman tertentu.
     */
    public void bukaLogin() {
        navigateToLogin();
    }

    public void ensureLoggedIn(Runnable onSuccess) {
        if (appSession.isLoggedIn()) {
            onSuccess.run();
            return;
        }

        this.pendingLoginAction = onSuccess;
        navigateToLogin();
    }

    public void showKlaimSaya() {
        denganHak(HakAkses.LIHAT_KLAIM_SENDIRI,
                "Klaim Saya hanya tersedia untuk Mahasiswa.",
                () -> {
                    KlaimSayaDialog dialog = new KlaimSayaDialog(mainFrame, klaimService, itemService, posService, appSession, this);
                    dialog.setVisible(true);
                });
    }

    public void showNotifikasi() {
        denganHak(HakAkses.LIHAT_NOTIFIKASI,
                "Notifikasi hanya dapat dilihat setelah login.",
                () -> {
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
