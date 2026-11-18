package model;

import model.enums.StatusItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * US-012: Pencatatan waktu dan pelaku di setiap perubahan status barang.
 */
public class LogStatus {
    private final LocalDateTime waktu;
    private final StatusItem status;
    private final String pelaku;
    private final String catatan;

    public LogStatus(StatusItem status, String pelaku, String catatan) {
        this.waktu = LocalDateTime.now();
        this.status = status;
        this.pelaku = pelaku;
        this.catatan = catatan;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public StatusItem getStatus() {
        return status;
    }

    public String getPelaku() {
        return pelaku;
    }

    public String getCatatan() {
        return catatan;
    }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String res = "[" + waktu.format(dtf) + "] " + status + " (oleh " + pelaku + ")";
        if (catatan != null && !catatan.trim().isEmpty()) {
            res += " -> Catatan: " + catatan;
        }
        return res;
    }
}
