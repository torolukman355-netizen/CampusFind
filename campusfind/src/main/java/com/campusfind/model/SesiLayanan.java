package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public enum SesiLayanan implements Serializable {
    PAGI("Sesi Pagi", LocalTime.of(8, 30), LocalTime.of(10, 30)),
    SIANG("Sesi Siang", LocalTime.of(13, 30), LocalTime.of(15, 30));

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("H.mm");

    private final String label;
    private final LocalTime mulai;
    private final LocalTime selesai;

    SesiLayanan(String label, LocalTime mulai, LocalTime selesai) {
        this.label = label;
        this.mulai = mulai;
        this.selesai = selesai;
    }

    public String getLabel() {
        return label + " (" + mulai.format(FORMAT) + " \u2013 " + selesai.format(FORMAT) + " WIB)";
    }

    public LocalTime getMulai() {
        return mulai;
    }

    public LocalTime getSelesai() {
        return selesai;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
