package repository;

import model.BarangElektronik;
import model.BarangNonElektronik;
import model.Item;
import model.enums.JenisLaporan;
import model.enums.StatusItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * US-007: Repository terpusat untuk data Item (BarangElektronik & BarangNonElektronik).
 * Menyediakan method findAll(), findById(), save(), dan query status untuk dipakai
 * oleh Petugas (Bintang) maupun Admin (Lukman).
 */
public class ItemRepository {
    private static ItemRepository instance;
    private final List<Item> items = new ArrayList<>();

    private ItemRepository() {
        seedDummyData();
    }

    public static synchronized ItemRepository getInstance() {
        if (instance == null) {
            instance = new ItemRepository();
        }
        return instance;
    }

    public synchronized List<Item> findAll() {
        return new ArrayList<>(items);
    }

    public synchronized Optional<Item> findById(String id) {
        return items.stream()
                .filter(i -> i.getId().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    public synchronized List<Item> findByStatus(StatusItem status) {
        return items.stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    public synchronized List<Item> findByJenis(JenisLaporan jenis) {
        return items.stream()
                .filter(i -> i.getJenis() == jenis)
                .collect(Collectors.toList());
    }

    public synchronized void save(Item item) {
        int idx = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equalsIgnoreCase(item.getId())) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            items.set(idx, item);
        } else {
            items.add(item);
        }
    }

    public synchronized boolean delete(String id) {
        return items.removeIf(i -> i.getId().equalsIgnoreCase(id.trim()));
    }

    public synchronized int countByStatus(StatusItem status) {
        return (int) items.stream().filter(i -> i.getStatus() == status).count();
    }

    private void seedDummyData() {
        // 1. Barang Elektronik Temuan (Belum diverifikasi)
        BarangElektronik b1 = new BarangElektronik(
                "T-101", JenisLaporan.TEMUAN, "iPhone 13 Hitam",
                "Gedung A Lt. 2", "Budi (Mhs)", "Sistem",
                "Apple", "IMEI-8839201"
        );
        items.add(b1);

        // 2. Barang Non-Elektronik Hilang (Belum diverifikasi)
        BarangNonElektronik b2 = new BarangNonElektronik(
                "H-102", JenisLaporan.HILANG, "Dompet Kulit Coklat",
                "Kantin Pusat", "Siti (Mhs)", "Sistem",
                "Kulit Asli", "Ada inisial S & KTP a.n Siti"
        );
        items.add(b2);

        // 3. Barang Non-Elektronik Temuan (Diverifikasi & punya lokasi simpan)
        BarangNonElektronik b3 = new BarangNonElektronik(
                "T-055", JenisLaporan.TEMUAN, "Tumbler Corkcicle Biru",
                "Perpustakaan Lt. 1", "Jepin (Petugas)", "Jepin",
                "Stainless Steel", "Stiker nama Andi di bawah botol"
        );
        b3.ubahStatus(StatusItem.DIVERIFIKASI, "Jepin", "Laporan diverifikasi valid");
        b3.setLokasiSimpan("Ruang C - 101", "Jepin");
        items.add(b3);

        // 4. Barang Elektronik Hilang (Diverifikasi)
        BarangElektronik b4 = new BarangElektronik(
                "H-012", JenisLaporan.HILANG, "Flashdisk Sandisk 32GB",
                "Lab Komputer 1", "Rama (Mhs)", "Bintang",
                "Sandisk", "SN-FD9921"
        );
        b4.ubahStatus(StatusItem.DIVERIFIKASI, "Bintang", "Laporan diverifikasi valid");
        items.add(b4);

        // 5. Barang Elektronik Temuan (Diverifikasi & pasangan cocok)
        BarangElektronik b5 = new BarangElektronik(
                "T-034", JenisLaporan.TEMUAN, "USB Sandisk Merah Hitam",
                "Lab Komputer 1", "Agnes (Mhs)", "Bintang",
                "Sandisk", "SN-FD9921"
        );
        b5.ubahStatus(StatusItem.DIVERIFIKASI, "Bintang", "Laporan diverifikasi valid");
        b5.setLokasiSimpan("Ruang C - 106", "Bintang");
        items.add(b5);
    }
}
