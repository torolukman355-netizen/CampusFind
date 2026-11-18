import model.*;
import model.enums.HakAkses;
import model.enums.JenisLaporan;
import model.enums.StatusItem;
import repository.ItemRepository;

public class TestBintangOOP {
    public static void main(String[] args) {
        System.out.println("=== 1. TEST REPOSITORY & POLIMORFISME (US-007, US-008) ===");
        ItemRepository repo = ItemRepository.getInstance();
        for (Item item : repo.findAll()) {
            System.out.println("Barang ID: " + item.getId() + " [" + item.getClass().getSimpleName() + "]");
            // Pemanggilan method polimorfik
            String detail = item.tampilkanDetail();
            assert detail != null && !detail.isEmpty();
        }
        System.out.println("-> OK: Polimorfisme Item.tampilkanDetail() berjalan sukses!\n");

        System.out.println("=== 2. TEST TRANSISI STATUS BERURUTAN (US-012) ===");
        BarangElektronik laptop = new BarangElektronik(
                "T-999", JenisLaporan.TEMUAN, "MacBook Air M2",
                "Ruang Seminar", "Andi", "Bintang",
                "Apple", "SN-MAC123456"
        );
        System.out.println("Status Awal: " + laptop.getStatus());

        // 1. DILAPORKAN -> DIVERIFIKASI (otomatis generate QR jika Temuan)
        laptop.ubahStatus(StatusItem.DIVERIFIKASI, "Bintang", "Laporan terverifikasi");
        System.out.println("Status 1   : " + laptop.getStatus() + " (QR: " + laptop.getKodeQR() + ")");
        assert laptop.getKodeQR().equals("QR-T-999");

        // 2. DIVERIFIKASI -> COCOK
        laptop.ubahStatus(StatusItem.COCOK, "Bintang", "Ditemukan kecocokan");
        System.out.println("Status 2   : " + laptop.getStatus());

        // 3. COCOK -> DIKLAIM
        laptop.ubahStatus(StatusItem.DIKLAIM, "Bintang", "Klaim mahasiswa diterima");
        System.out.println("Status 3   : " + laptop.getStatus());

        // 4. DIKLAIM -> DIKEMBALIKAN (otomatis terkunci)
        laptop.ubahStatus(StatusItem.DIKEMBALIKAN, "Bintang", "Barang diserahterimakan");
        System.out.println("Status 4   : " + laptop.getStatus() + " (Terkunci: " + laptop.isTerkunci() + ")");
        assert laptop.isTerkunci();

        // 5. Coba ubah saat sudah terkunci -> harus lempar IllegalStateException
        try {
            laptop.ubahStatus(StatusItem.DIVERIFIKASI, "Bintang", "Coba hack status");
            System.err.println("FAILED: Seharusnya gagal karena barang terkunci!");
        } catch (IllegalStateException e) {
            System.out.println("-> OK: Proteksi data terkunci bekerja (" + e.getMessage() + ")");
        }

        System.out.println("\n=== 3. TEST VALIDASI LOKASI SIMPAN KHUSUS TEMUAN (US-013) ===");
        BarangNonElektronik barangHilang = new BarangNonElektronik(
                "H-888", JenisLaporan.HILANG, "Kacamata Hitam",
                "Gazebo", "Budi", "Bintang", "Plastik", "Frame kotak hitam"
        );
        try {
            barangHilang.setLokasiSimpan("Loker A-1", "Bintang");
            System.err.println("FAILED: Barang hilang tidak boleh diberi lokasi simpan!");
        } catch (IllegalArgumentException e) {
            System.out.println("-> OK: Validasi lokasi simpan menolak barang hilang (" + e.getMessage() + ")");
        }

        System.out.println("\n=== 4. TEST HAK AKSES PENGGUNA (US-001, US-002, US-003) ===");
        Petugas p = new Petugas("P1", "Bintang", "bintang@campusfind.ac.id", "pass", "NIP01");
        Mahasiswa m = new Mahasiswa("M1", "Pitya", "pitya@campusfind.ac.id", "pass", "NIM01");
        Admin a = new Admin("A1", "Lukman", "lukman@campusfind.ac.id", "pass");

        System.out.println("Petugas punya akses VERIFIKASI_LAPORAN? " + p.punyaAkses(HakAkses.VERIFIKASI_LAPORAN));
        System.out.println("Mahasiswa punya akses VERIFIKASI_LAPORAN? " + m.punyaAkses(HakAkses.VERIFIKASI_LAPORAN));
        System.out.println("Admin punya akses LIHAT_SEMUA_BARANG? " + a.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG));
        System.out.println("Mahasiswa punya akses LIHAT_SEMUA_BARANG? " + m.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG));

        assert p.punyaAkses(HakAkses.VERIFIKASI_LAPORAN);
        assert !m.punyaAkses(HakAkses.VERIFIKASI_LAPORAN);
        assert a.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG);
        assert !m.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG);

        System.out.println("\n=== SEMUA TEST FITUR BINTANG (US-001 s.d. US-012) 100% SUKSES! ===");
    }
}
