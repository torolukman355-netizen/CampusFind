package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * US-016: Model Notifikasi In-App untuk Mahasiswa (Pitya dan Nadia).
 */
public class Notifikasi {
    private final String id;
    private final String untukUser;
    private final String pesan;
    private final LocalDateTime waktu;
    private boolean dibaca;

    public Notifikasi(String id, String untukUser, String pesan) {
        this.id = id;
        this.untukUser = untukUser;
        this.pesan = pesan;
        this.waktu = LocalDateTime.now();
        this.dibaca = false;
    }

    public String getId() { return id; }
    public String getUntukUser() { return untukUser; }
    public String getPesan() { return pesan; }
    public LocalDateTime getWaktu() { return waktu; }
    public boolean isDibaca() { return dibaca; }
    public void setDibaca(boolean dibaca) { this.dibaca = dibaca; }

    public String getWaktuFormatted() {
        return waktu.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
