package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.Klaim;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.SesiLayanan;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Pabrik pesan notifikasi terpusat untuk menjaga konsistensi teks dan kemudahan pengujian (ADR-5).
 */
public final class NotificationMessages {

    private static final DateTimeFormatter TANGGAL_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private NotificationMessages() {
        // utility class
    }

    public static String judulKlaimDiterima() {
        return "Klaim Diterima";
    }

    public static String isiKlaimDiterima(Item item, Klaim klaim, PosPenyimpanan pos, LocalDate tanggal, SesiLayanan sesi) {
        String lokasi = (pos != null) ? pos.getLokasiPenyerahan() : "-";
        return String.format(
                "Pengajuan klaim %s diterima (kode %s). Datang pada %s, %s, di %s.",
                item.kode(),
                klaim.getId(),
                tanggal.format(TANGGAL_FORMAT),
                sesi.getLabel(),
                lokasi
        );
    }

    public static String judulKlaimDisetujui() {
        return "Klaim Disetujui";
    }

    public static String isiKlaimDisetujui(Item item, Klaim klaim) {
        return String.format(
                "Selamat! Pengajuan klaim Anda (%s) untuk barang %s telah disetujui. Silakan lakukan serah terima sesuai jadwal.",
                klaim.getId(),
                item.kode()
        );
    }

    public static String judulKlaimDitolak() {
        return "Klaim Ditolak";
    }

    public static String isiKlaimDitolak(Item item, Klaim klaim, String alasan) {
        String kodeItem = (item != null) ? item.kode() : "(Barang)";
        return String.format(
                "Pengajuan klaim Anda (%s) untuk barang %s ditolak. Alasan: %s",
                klaim.getId(),
                kodeItem,
                alasan
        );
    }

    public static String judulKandidatCocok() {
        return "Kandidat Cocok Ditemukan";
    }

    public static String isiKandidatCocok(Item hilang, Item temuan, int skor) {
        return String.format(
                "Sistem menemukan barang temuan (%s - %s) dengan kecocokan %d%% untuk laporan kehilangan Anda %s (%s).",
                temuan.kode(),
                temuan.getNama(),
                skor,
                hilang.kode(),
                hilang.getNama()
        );
    }
}
