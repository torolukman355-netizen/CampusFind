package com.campusfind.service;

/**
 * Konstanta kebijakan bisnis aplikasi CampusFind.
 */
public final class ServicePolicy {

    public static final int NIM_LENGTH = 9;
    public static final int NIP_LENGTH = 18;
    public static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    public static final int MAX_KLAIM_HARI_KEDEPAN = 30;
    public static final int MIN_CIRI_KHUSUS = 10;
    public static final int MAX_CIRI_KHUSUS = 500;

    // Konstanta Kandidat Cocok (US-015, US-026)
    public static final int MIN_SKOR_KANDIDAT = 70;
    public static final int BOBOT_NAMA = 30;
    public static final int BOBOT_DESKRIPSI = 20;
    public static final int BOBOT_LOKASI = 20;
    public static final int BOBOT_WAKTU = 20;
    public static final int BOBOT_ATRIBUT_SPESIFIK = 10;
    public static final int MAX_HARI_KEDEKATAN_WAKTU = 7;
    public static final int MAX_KANDIDAT_HASIL = 10;

    private ServicePolicy() {
        // utility class – tidak dapat diinstansiasi
    }
}
