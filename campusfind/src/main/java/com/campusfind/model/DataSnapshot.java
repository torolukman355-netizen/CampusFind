package com.campusfind.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private int schemaVersion = 1;
    private int nextItemSeq;
    private Map<Integer, Integer> nextKlaimSeqPerTahun;
    private LinkedHashMap<String, Item> items;
    private LinkedHashMap<String, Klaim> klaim;
    private LinkedHashMap<String, PosPenyimpanan> pos;
    private ArrayList<Notification> notifikasi;

    private transient Map<String, String> klaimAktifIndex;

    public DataSnapshot() {
        this.nextItemSeq = 0;
        this.nextKlaimSeqPerTahun = new HashMap<>();
        this.items = new LinkedHashMap<>();
        this.klaim = new LinkedHashMap<>();
        this.pos = new LinkedHashMap<>();
        this.notifikasi = new ArrayList<>();
        this.klaimAktifIndex = new HashMap<>();
    }

    public void buildIndex() {
        klaimAktifIndex = new HashMap<>();
        for (Map.Entry<String, Klaim> entry : klaim.entrySet()) {
            StatusKlaim status = entry.getValue().getStatus();
            if (status == StatusKlaim.DIAJUKAN || status == StatusKlaim.MENUNGGU_VERIFIKASI
                    || status == StatusKlaim.DISETUJUI) {
                klaimAktifIndex.put(entry.getValue().getItemId(), entry.getKey());
            }
        }
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public int getNextItemSeq() {
        return nextItemSeq;
    }

    public void setNextItemSeq(int nextItemSeq) {
        this.nextItemSeq = nextItemSeq;
    }

    public Map<Integer, Integer> getNextKlaimSeqPerTahun() {
        return nextKlaimSeqPerTahun;
    }

    public LinkedHashMap<String, Item> getItems() {
        return items;
    }

    public LinkedHashMap<String, Klaim> getKlaim() {
        return klaim;
    }

    public LinkedHashMap<String, PosPenyimpanan> getPos() {
        return pos;
    }

    public ArrayList<Notification> getNotifikasi() {
        return notifikasi;
    }

    public Map<String, String> getKlaimAktifIndex() {
        return klaimAktifIndex;
    }
}
