package com.campusfind.service;

import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.MatchResult;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strategi pencocokan kandidat berbasis pembobotan atribut (US-015, US-026).
 *
 * <p>Aturan pembobotan:
 * <ul>
 *   <li><b>Gerbang:</b> Kategori harus sama. Jika berbeda, skor langsung 0.</li>
 *   <li><b>Override:</b> Nomor seri pada {@link BarangElektronik} identik &amp; non-kosong → skor 100.</li>
 *   <li><b>Nama barang:</b> Overlap token kata (maks. 30 poin).</li>
 *   <li><b>Deskripsi:</b> Overlap token kata (maks. 20 poin).</li>
 *   <li><b>Lokasi kejadian:</b> Overlap token kata (maks. 20 poin).</li>
 *   <li><b>Kedekatan waktu:</b> Tanggal temuan &gt;= tanggal hilang; selisih &le; 7 hari (maks. 20 poin).</li>
 *   <li><b>Atribut spesifik:</b> Merek untuk elektronik / warna &amp; bahan untuk non-elektronik (maks. 10 poin).</li>
 * </ul>
 *
 * <p><b>KEAMANAN KRITIS:</b> Field {@code ciriRahasia} tidak pernah dievaluasi atau disertakan dalam alasan.
 * Nomor seri utuh tidak pernah dibocorkan.
 */
public class WeightedAttributeStrategy implements CandidateMatchingStrategy {

    private static final Set<String> STOPWORDS = Set.of(
            "yang", "di", "dan", "dengan", "ke", "dari", "ini", "itu", "untuk",
            "pada", "adalah", "sebagai", "oleh", "saat", "atau", "juga", "ada",
            "saya", "kami", "dia", "mereka", "nya", "dalam", "atas", "bawah"
    );

    @Override
    public MatchResult evaluate(Item hilang, Item temuan) {
        Objects.requireNonNull(hilang, "Item hilang tidak boleh null");
        Objects.requireNonNull(temuan, "Item temuan tidak boleh null");

        List<String> alasan = new ArrayList<>();

        // 1. Gerbang: Kategori harus sama
        if (hilang.getKategori() == null || temuan.getKategori() == null ||
                hilang.getKategori() != temuan.getKategori()) {
            return new MatchResult(temuan.getId(), 0, Collections.emptyList());
        }

        alasan.add("Kategori sama: " + temuan.getKategori().getLabel());

        // 2. Override: Nomor seri identik pada BarangElektronik
        if (hilang instanceof BarangElektronik eHilang && temuan instanceof BarangElektronik eTemuan) {
            String seriHilang = (eHilang.getNomorSeri() != null) ? eHilang.getNomorSeri().trim() : "";
            String seriTemuan = (eTemuan.getNomorSeri() != null) ? eTemuan.getNomorSeri().trim() : "";
            if (!seriHilang.isEmpty() && !seriTemuan.isEmpty() && seriHilang.equalsIgnoreCase(seriTemuan)) {
                // Jangan tampilkan nomor seri utuh
                alasan.add("Nomor seri identik");
                return new MatchResult(temuan.getId(), 100, alasan);
            }
        }

        int totalSkor = 0;

        // 3. Nama Barang (Overlap token kata: maks 30)
        double overlapNama = hitungOverlap(hilang.getNama(), temuan.getNama());
        if (overlapNama > 0.0) {
            int skorNama = (int) Math.round(ServicePolicy.BOBOT_NAMA * overlapNama);
            totalSkor += skorNama;
            if (overlapNama >= 0.99) {
                alasan.add("Nama barang serupa");
            } else {
                alasan.add("Nama barang memiliki kesamaan kata");
            }
        }

        // 4. Deskripsi (Overlap token kata: maks 20)
        double overlapDeskripsi = hitungOverlap(hilang.getDeskripsi(), temuan.getDeskripsi());
        if (overlapDeskripsi > 0.0) {
            int skorDeskripsi = (int) Math.round(ServicePolicy.BOBOT_DESKRIPSI * overlapDeskripsi);
            totalSkor += skorDeskripsi;
            alasan.add("Deskripsi memiliki kesamaan");
        }

        // 5. Lokasi Kejadian (Overlap token kata: maks 20)
        double overlapLokasi = hitungOverlap(hilang.getLokasiKejadian(), temuan.getLokasiKejadian());
        if (overlapLokasi > 0.0) {
            int skorLokasi = (int) Math.round(ServicePolicy.BOBOT_LOKASI * overlapLokasi);
            totalSkor += skorLokasi;
            alasan.add("Lokasi kejadian mirip: " + temuan.getLokasiKejadian());
        }

        // 6. Kedekatan Waktu (Temuan >= Hilang; maks 20)
        if (hilang.getTanggalKejadian() != null && temuan.getTanggalKejadian() != null) {
            long selisihHari = ChronoUnit.DAYS.between(hilang.getTanggalKejadian(), temuan.getTanggalKejadian());
            if (selisihHari >= 0 && selisihHari <= ServicePolicy.MAX_HARI_KEDEKATAN_WAKTU) {
                int skorWaktu = (int) Math.round((double) ServicePolicy.BOBOT_WAKTU * (ServicePolicy.MAX_HARI_KEDEKATAN_WAKTU - selisihHari + 1) / (ServicePolicy.MAX_HARI_KEDEKATAN_WAKTU + 1));
                totalSkor += skorWaktu;
                if (selisihHari == 0) {
                    alasan.add("Ditemukan pada hari yang sama dengan kehilangan");
                } else {
                    alasan.add("Ditemukan " + selisihHari + " hari setelah kehilangan");
                }
            }
        }

        // 7. Atribut Spesifik (maks 10)
        if (hilang instanceof BarangElektronik eHilang && temuan instanceof BarangElektronik eTemuan) {
            String merekHilang = (eHilang.getMerek() != null) ? eHilang.getMerek().trim() : "";
            String merekTemuan = (eTemuan.getMerek() != null) ? eTemuan.getMerek().trim() : "";
            if (!merekHilang.isEmpty() && !merekTemuan.isEmpty() && merekHilang.equalsIgnoreCase(merekTemuan)) {
                totalSkor += ServicePolicy.BOBOT_ATRIBUT_SPESIFIK;
                alasan.add("Merek sama: " + eTemuan.getMerek());
            }
        } else if (hilang instanceof BarangNonElektronik neHilang && temuan instanceof BarangNonElektronik neTemuan) {
            int skorAtribut = 0;
            String warnaHilang = (neHilang.getWarna() != null) ? neHilang.getWarna().trim() : "";
            String warnaTemuan = (neTemuan.getWarna() != null) ? neTemuan.getWarna().trim() : "";
            if (!warnaHilang.isEmpty() && !warnaTemuan.isEmpty() && warnaHilang.equalsIgnoreCase(warnaTemuan)) {
                skorAtribut += 5;
                alasan.add("Warna sama: " + neTemuan.getWarna());
            }

            String bahanHilang = (neHilang.getBahan() != null) ? neHilang.getBahan().trim() : "";
            String bahanTemuan = (neTemuan.getBahan() != null) ? neTemuan.getBahan().trim() : "";
            if (!bahanHilang.isEmpty() && !bahanTemuan.isEmpty() && bahanHilang.equalsIgnoreCase(bahanTemuan)) {
                skorAtribut += 5;
                alasan.add("Bahan sama: " + neTemuan.getBahan());
            }
            totalSkor += skorAtribut;
        }

        int finalSkor = Math.min(100, Math.max(0, totalSkor));
        return new MatchResult(temuan.getId(), finalSkor, alasan);
    }

    private double hitungOverlap(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;
        Set<String> tokens1 = ekstrakToken(text1);
        Set<String> tokens2 = ekstrakToken(text2);
        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        Set<String> irisan = new HashSet<>(tokens1);
        irisan.retainAll(tokens2);

        if (irisan.isEmpty()) {
            return 0.0;
        }

        Set<String> gabungan = new HashSet<>(tokens1);
        gabungan.addAll(tokens2);

        return (double) irisan.size() / gabungan.size();
    }

    private Set<String> ekstrakToken(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptySet();
        }
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
        String[] words = normalized.split("\\s+");
        return Arrays.stream(words)
                .filter(w -> !w.isBlank() && w.length() > 1 && !STOPWORDS.contains(w))
                .collect(Collectors.toSet());
    }
}
