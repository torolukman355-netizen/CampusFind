package com.campusfind.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Role pengguna CampusFind beserta kumpulan {@link HakAkses} yang dimilikinya.
 * <p>
 * Semua keputusan otorisasi diambil dari role ini sehingga lapisan UI/servis
 * tidak perlu mengetahui kelas konkret pengguna ({@code instanceof}).
 */
public enum Role {

    MAHASISWA("Mahasiswa", 9, hakMahasiswa()),
    PETUGAS("Petugas", 18, hakPetugas()),
    ADMIN("Admin", 0, hakAdmin());

    private final String label;
    private final int panjangIdentitas;
    private final Set<HakAkses> hakAkses;

    Role(String label, int panjangIdentitas, HakAkses[] daftarHak) {
        this.label = label;
        this.panjangIdentitas = panjangIdentitas;
        EnumSet<HakAkses> hak = EnumSet.noneOf(HakAkses.class);
        Collections.addAll(hak, daftarHak);
        this.hakAkses = Collections.unmodifiableSet(hak);
    }

    private static HakAkses[] hakMahasiswa() {
        return new HakAkses[]{
                HakAkses.LIHAT_TEMUAN,
                HakAkses.LIHAT_NOTIFIKASI,
                HakAkses.BUAT_LAPORAN,
                HakAkses.AJUKAN_KLAIM,
                HakAkses.LIHAT_KLAIM_SENDIRI
        };
    }

    private static HakAkses[] hakPetugas() {
        return new HakAkses[]{
                HakAkses.LIHAT_TEMUAN,
                HakAkses.LIHAT_NOTIFIKASI,
                HakAkses.VERIFIKASI_LAPORAN,
                HakAkses.KELOLA_STATUS_ITEM,
                HakAkses.KELOLA_LOKASI_SIMPAN,
                HakAkses.KONFIRMASI_KANDIDAT,
                HakAkses.KELOLA_QR_CODE,
                HakAkses.PUTUSKAN_KLAIM,
                HakAkses.SERAH_TERIMA,
                HakAkses.LIHAT_SEMUA_KLAIM
        };
    }

    private static HakAkses[] hakAdmin() {
        EnumSet<HakAkses> hak = EnumSet.noneOf(HakAkses.class);
        Collections.addAll(hak, hakPetugas());
        hak.add(HakAkses.LIHAT_STATISTIK);
        hak.add(HakAkses.LIHAT_SEMUA_BARANG);
        hak.add(HakAkses.KELOLA_SEMUA_LAPORAN);
        hak.add(HakAkses.MANAJEMEN_PENGGUNA);
        return hak.toArray(new HakAkses[0]);
    }

    public String getLabel() {
        return label;
    }

    /**
     * Panjang nomor identitas yang sah untuk role ini.
     *
     * @return 9 untuk NIM, 18 untuk NIP, atau 0 bila tidak dibatasi (Admin)
     */
    public int getPanjangIdentitas() {
        return panjangIdentitas;
    }

    public Set<HakAkses> getHakAkses() {
        return hakAkses;
    }

    public boolean boleh(HakAkses hak) {
        return hak != null && hakAkses.contains(hak);
    }

    /**
     * Menebak role dari panjang nomor identitas (dipakai untuk kompatibilitas
     * alur lama). Nomor sepanjang NIP dianggap Petugas, selain itu Mahasiswa.
     */
    public static Role dariNomorIdentitas(String nomorIdentitas) {
        if (nomorIdentitas != null && nomorIdentitas.trim().length() == PETUGAS.panjangIdentitas) {
            return PETUGAS;
        }
        return MAHASISWA;
    }

    @Override
    public String toString() {
        return label;
    }
}
