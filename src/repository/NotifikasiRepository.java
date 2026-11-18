package repository;

import model.Notifikasi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * US-016: Repository untuk mengelola Notifikasi In-App Mahasiswa.
 */
public class NotifikasiRepository {
    private static NotifikasiRepository instance;
    private final List<Notifikasi> notifikasiList = new ArrayList<>();

    private NotifikasiRepository() {
        seedDummyData();
    }

    public static synchronized NotifikasiRepository getInstance() {
        if (instance == null) {
            instance = new NotifikasiRepository();
        }
        return instance;
    }

    public synchronized List<Notifikasi> getNotifikasiByUser(String namaOrEmail) {
        return notifikasiList.stream()
                .filter(n -> n.getUntukUser().equalsIgnoreCase(namaOrEmail) ||
                        n.getUntukUser().toLowerCase().contains(namaOrEmail.toLowerCase()))
                .collect(Collectors.toList());
    }

    public synchronized void tambahNotifikasi(String untukUser, String pesan) {
        String id = "NOTIF-" + (notifikasiList.size() + 1);
        notifikasiList.add(new Notifikasi(id, untukUser, pesan));
    }

    public synchronized int countUnreadByUser(String namaOrEmail) {
        return (int) notifikasiList.stream()
                .filter(n -> !n.isDibaca() && (n.getUntukUser().equalsIgnoreCase(namaOrEmail) ||
                        n.getUntukUser().toLowerCase().contains(namaOrEmail.toLowerCase())))
                .count();
    }

    public synchronized void tandaiSemuaDibaca(String namaOrEmail) {
        for (Notifikasi n : notifikasiList) {
            if (n.getUntukUser().equalsIgnoreCase(namaOrEmail) ||
                    n.getUntukUser().toLowerCase().contains(namaOrEmail.toLowerCase())) {
                n.setDibaca(true);
            }
        }
    }

    private void seedDummyData() {
        tambahNotifikasi("Siti (Mhs)", "Laporan H-102 (Dompet Kulit) Anda berhasil dibuat dan menunggu verifikasi petugas.");
        tambahNotifikasi("Rama (Mhs)", "Laporan H-012 (Flashdisk Sandisk) Anda telah diverifikasi oleh Petugas Bintang!");
        tambahNotifikasi("Rama (Mhs)", "Ditemukan rekomendasi Smart Matching untuk laporan Flashdisk Sandisk dengan skor 88%!");
        tambahNotifikasi("Andi (Mhs)", "Klaim K-201 Anda atas Tumbler Corkcicle sedang dalam pemeriksaan petugas.");
        tambahNotifikasi("Pitya", "Selamat datang di CampusFind! Anda dapat melaporkan barang hilang atau temuan di kampus.");
        tambahNotifikasi("Nadia", "Sistem Smart Matching aktif. Anda akan menerima notifikasi jika ditemukan barang yang cocok.");
    }
}
