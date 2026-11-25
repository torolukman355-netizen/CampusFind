package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.StatusItem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Layanan perhitungan statistik kehilangan dan pengembalian barang (US-023).
 */
public class StatistikService {

    private static final DateTimeFormatter MONTH_LABEL_FMT =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.forLanguageTag("id-ID"));

    public Map<KategoriBarang, Long> hitungPerKategori(List<Item> items) {
        Map<KategoriBarang, Long> hasil = new EnumMap<>(KategoriBarang.class);
        for (KategoriBarang kategori : KategoriBarang.values()) {
            hasil.put(kategori, 0L);
        }
        if (items == null || items.isEmpty()) {
            return hasil;
        }
        for (Item item : items) {
            if (item != null && item.getKategori() != null) {
                hasil.merge(item.getKategori(), 1L, Long::sum);
            }
        }
        return hasil;
    }

    public Map<String, Long> hitungPerBulan(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<YearMonth, Long> terurut = new TreeMap<>();
        for (Item item : items) {
            if (item == null) continue;
            YearMonth ym = ekstrakBulan(item);
            if (ym != null) {
                terurut.merge(ym, 1L, Long::sum);
            }
        }
        Map<String, Long> hasil = new LinkedHashMap<>();
        for (Map.Entry<YearMonth, Long> entry : terurut.entrySet()) {
            hasil.put(entry.getKey().format(MONTH_LABEL_FMT), entry.getValue());
        }
        return hasil;
    }

    public long hitungPerJenis(List<Item> items, JenisLaporan jenis) {
        if (items == null || jenis == null) {
            return 0L;
        }
        return items.stream()
                .filter(item -> item != null && item.getJenis() == jenis)
                .count();
    }

    /**
     * Menghitung tingkat pengembalian (0 - 100%) berdasarkan jumlah barang dikembalikan
     * dibagi jumlah laporan HILANG terverifikasi (atau total HILANG bila belum ada yang terverifikasi).
     */
    public double hitungTingkatPengembalianPersen(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        long hilangTerverifikasi = items.stream()
                .filter(i -> i != null && i.getJenis() == JenisLaporan.HILANG)
                .filter(i -> i.getStatus() != StatusItem.DILAPORKAN && i.getStatus() != StatusItem.DITOLAK)
                .count();

        long pembagi = hilangTerverifikasi > 0 ? hilangTerverifikasi : hitungPerJenis(items, JenisLaporan.HILANG);
        if (pembagi == 0) {
            return 0.0;
        }

        long dikembalikan = items.stream()
                .filter(i -> i != null && (i.getStatus() == StatusItem.DIKEMBALIKAN
                        || i.getStatus() == StatusItem.DISERAHKAN))
                .count();

        return Math.min(100.0, (dikembalikan * 100.0) / pembagi);
    }

    /**
     * Menghitung rata-rata durasi (dalam jam) dari laporan dibuat hingga status DIKEMBALIKAN.
     * Mengembalikan 0.0 jika belum ada barang yang dikembalikan.
     */
    public double hitungRataRataWaktuPengembalianJam(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        long totalJam = 0L;
        long jumlahSelesai = 0L;

        for (Item item : items) {
            if (item == null) continue;
            if (item.getStatus() == StatusItem.DIKEMBALIKAN || item.getStatus() == StatusItem.DISERAHKAN) {
                LocalDateTime mulai = item.getDibuatPada();
                LocalDate tglSelesai = item.getTanggalSerahTerima();
                if (mulai != null && tglSelesai != null) {
                    LocalDateTime akhir = tglSelesai.atTime(17, 0);
                    if (akhir.isBefore(mulai)) {
                        akhir = mulai.plusHours(1);
                    }
                    long jam = Math.max(1L, Duration.between(mulai, akhir).toHours());
                    totalJam += jam;
                    jumlahSelesai++;
                }
            }
        }
        return jumlahSelesai == 0 ? 0.0 : (double) totalJam / jumlahSelesai;
    }

    public String formatRataRataWaktuPengembalian(List<Item> items) {
        double rataJam = hitungRataRataWaktuPengembalianJam(items);
        if (rataJam <= 0.0) {
            return "Belum ada";
        }
        if (rataJam < 24.0) {
            return String.format(Locale.US, "%.1f jam", rataJam);
        }
        double hari = rataJam / 24.0;
        return String.format(Locale.US, "%.1f hari", hari);
    }

    private YearMonth ekstrakBulan(Item item) {
        if (item.getTanggalKejadian() != null) {
            return YearMonth.from(item.getTanggalKejadian());
        }
        if (item.getDibuatPada() != null) {
            return YearMonth.from(item.getDibuatPada());
        }
        return null;
    }
}