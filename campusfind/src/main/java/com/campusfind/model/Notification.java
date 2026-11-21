package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entitas notifikasi in-app untuk pengguna.
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String penerimaNomorIdentitas;
    private final String judul;
    private final String isi;
    private final LocalDateTime dibuatPada;
    private boolean dibaca;
    private NotificationType tipe;
    private String referensiId;

    public Notification(String id,
                        String penerimaNomorIdentitas,
                        NotificationType tipe,
                        String judul,
                        String isi,
                        String referensiId,
                        LocalDateTime dibuatPada,
                        boolean dibaca) {
        this.id = id;
        this.penerimaNomorIdentitas = penerimaNomorIdentitas;
        this.tipe = (tipe != null) ? tipe : NotificationType.KLAIM_DITERIMA;
        this.judul = judul;
        this.isi = isi;
        this.referensiId = referensiId;
        this.dibuatPada = dibuatPada;
        this.dibaca = dibaca;
    }

    /**
     * Konstruktor kompatibilitas untuk versi lama.
     */
    public Notification(String id,
                        String penerimaNomorIdentitas,
                        String judul,
                        String isi,
                        LocalDateTime dibuatPada,
                        boolean dibaca) {
        this(id, penerimaNomorIdentitas, NotificationType.KLAIM_DITERIMA, judul, isi, null, dibuatPada, dibaca);
    }

    public String getId() {
        return id;
    }

    public String getPenerimaNomorIdentitas() {
        return penerimaNomorIdentitas;
    }

    public NotificationType getTipe() {
        return (tipe != null) ? tipe : NotificationType.KLAIM_DITERIMA;
    }

    public void setTipe(NotificationType tipe) {
        this.tipe = tipe;
    }

    public String getReferensiId() {
        return referensiId;
    }

    public void setReferensiId(String referensiId) {
        this.referensiId = referensiId;
    }

    public String getJudul() {
        return judul;
    }

    public String getIsi() {
        return isi;
    }

    public LocalDateTime getDibuatPada() {
        return dibuatPada;
    }

    public boolean isDibaca() {
        return dibaca;
    }

    public void setDibaca(boolean dibaca) {
        this.dibaca = dibaca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
