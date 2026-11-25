package com.campusfind.repository;

import com.campusfind.model.DataSnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementasi berbasis file (DataSnapshot) untuk penyimpanan log status dan serah terima petugas.
 */
public class FilePetugasAuditRepository implements PetugasAuditRepository {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final FileDataStore dataStore;

    public FilePetugasAuditRepository(FileDataStore dataStore) {
        this.dataStore = Objects.requireNonNull(dataStore, "dataStore");
    }

    @Override
    public void simpanLog(String itemId, String aksi, String pelaku) {
        dataStore.runAtomically(() -> {
            DataSnapshot snap = dataStore.getSnapshot();
            String entry = "[" + LocalDateTime.now().format(DTF) + "] " + aksi + " (oleh " + pelaku + ")";
            snap.getStatusLogs().computeIfAbsent(itemId, k -> new ArrayList<>()).add(entry);
            dataStore.saveSnapshot();
        });
    }

    @Override
    public List<String> ambilLog(String itemId) {
        DataSnapshot snap = dataStore.getSnapshot();
        List<String> logs = snap.getStatusLogs().get(itemId);
        return logs == null ? List.of() : List.copyOf(logs);
    }

    @Override
    public void simpanSerahTerima(String itemId, String namaPenerima, LocalDate tanggal, String nipPetugas) {
        dataStore.runAtomically(() -> {
            DataSnapshot snap = dataStore.getSnapshot();
            String info = namaPenerima + "|" + (tanggal != null ? tanggal : LocalDate.now()) + "|" + nipPetugas;
            snap.getSerahTerima().put(itemId, info);
            dataStore.saveSnapshot();
        });
    }
}