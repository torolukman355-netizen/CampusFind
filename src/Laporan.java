/**
 * MODUL SINKRONISASI KELOMPOK 4
 * Berdasarkan project: CampusFind — Sistem Smart Lost and Found Berbasis Java
 * Anggota Kelompok:
 * - Muhammad Fakhry Adrian (251402053)
 * - Nadia Stevany Br Situmorang (251402073)
 * - Bintang Pamungkas (251402082)
 * - Jevine Jeje Zakarias Simanjuntak (251402085)
 * - Muhammad Lukman Toro (251402105)
 * - Fitya Shofi Ahmad (251402132)
 */
public class Laporan {

    // state milik tiap object
    String namaBarang;
    String jenis;
    int skorMatching;

    // state milik class (dibagi semua object)
    static int jumlahLaporan = 0;

    // constructor: jalan otomatis saat new
    public Laporan(String namaBarang, String jenis, int skorAwal) {
        this.namaBarang = namaBarang;
        this.jenis = jenis;

        if (skorAwal >= 0 && skorAwal <= 100) {
            this.skorMatching = skorAwal;
        } else {
            this.skorMatching = 0;
        }

        jumlahLaporan++;
    }

    // method pengubah state, punya boundary, return boolean
    public boolean naikkanSkor(int tambahan) {
        if (tambahan > 0 && skorMatching + tambahan <= 100) {
            skorMatching = skorMatching + tambahan;
            return true;
        }
        return false;
    }

    // method return angka
    public int getSkor() {
        return skorMatching;
    }

    // method return String gabungan state
    public String getInfo() {
        return namaBarang + " - " + jenis + " - Skor: " + skorMatching;
    }

    public static void main(String[] args) {

        Laporan l1 = new Laporan("Dompet", "HILANG", 40);
        Laporan l2 = new Laporan("Laptop", "TEMUAN", 150);

        l1.naikkanSkor(30);

        System.out.println(l1.getInfo());
        System.out.println(l2.getInfo());

        if (l1.namaBarang.equals("Dompet")) {
            System.out.println("Laporan ditemukan");
        }

        System.out.println("Jumlah laporan: " + Laporan.jumlahLaporan);

        Laporan l3 = l1;
        l3.naikkanSkor(10);

        System.out.println("Skor l1: " + l1.getSkor());
        System.out.println("Skor l3: " + l3.getSkor());
    }
}
