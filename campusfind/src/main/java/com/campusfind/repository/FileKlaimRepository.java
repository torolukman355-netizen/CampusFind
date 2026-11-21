package com.campusfind.repository;

import com.campusfind.model.DataSnapshot;
import com.campusfind.model.Klaim;
import com.campusfind.model.StatusKlaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * File-backed implementation of {@link KlaimRepository}.
 * All operations delegate to the {@link DataSnapshot} managed by {@link FileDataStore}.
 */
public class FileKlaimRepository implements KlaimRepository {

    private static final Logger LOG = Logger.getLogger(FileKlaimRepository.class.getName());

    private final FileDataStore dataStore;

    public FileKlaimRepository(FileDataStore dataStore) {
        this.dataStore = dataStore;
    }

    private boolean isAktif(StatusKlaim status) {
        return status == StatusKlaim.DIAJUKAN
                || status == StatusKlaim.MENUNGGU_VERIFIKASI
                || status == StatusKlaim.DISETUJUI;
    }

    @Override
    public void save(Klaim klaim) {
        DataSnapshot snap = dataStore.getSnapshot();
        snap.getKlaim().put(klaim.getId(), klaim);

        // Update active index if the claim is active
        if (isAktif(klaim.getStatus())) {
            snap.getKlaimAktifIndex().put(klaim.getItemId(), klaim.getId());
        }

        dataStore.saveSnapshot();
        LOG.fine("Klaim disimpan: " + klaim.getId());
    }

    @Override
    public Optional<Klaim> findById(String id) {
        DataSnapshot snap = dataStore.getSnapshot();
        return Optional.ofNullable(snap.getKlaim().get(id));
    }

    @Override
    public List<Klaim> findAll() {
        DataSnapshot snap = dataStore.getSnapshot();
        return List.copyOf(snap.getKlaim().values());
    }

    @Override
    public List<Klaim> findByPengklaim(String nomorIdentitas) {
        DataSnapshot snap = dataStore.getSnapshot();
        List<Klaim> result = new ArrayList<>();
        for (Klaim klaim : snap.getKlaim().values()) {
            if (klaim.getPengklaim().getNomorIdentitas().equals(nomorIdentitas)) {
                result.add(klaim);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public Optional<Klaim> findAktifByItemId(String itemId) {
        DataSnapshot snap = dataStore.getSnapshot();
        Map<String, String> index = snap.getKlaimAktifIndex();
        String klaimId = index.get(itemId);
        if (klaimId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(snap.getKlaim().get(klaimId));
    }

    @Override
    public String nextId(int tahun) {
        DataSnapshot snap = dataStore.getSnapshot();
        Map<Integer, Integer> seqMap = snap.getNextKlaimSeqPerTahun();
        int current = seqMap.getOrDefault(tahun, 0);
        int next = current + 1;
        seqMap.put(tahun, next);
        String id = String.format("CLM-%d-%04d", tahun, next);
        dataStore.saveSnapshot();
        LOG.fine("ID klaim berikutnya: " + id);
        return id;
    }

    @Override
    public void updateStatus(String id, StatusKlaim status) {
        DataSnapshot snap = dataStore.getSnapshot();
        Klaim klaim = snap.getKlaim().get(id);
        if (klaim != null) {
            StatusKlaim oldStatus = klaim.getStatus();
            klaim.setStatus(status);

            // Update active index
            Map<String, String> index = snap.getKlaimAktifIndex();
            boolean wasActive = isAktif(oldStatus);
            boolean isActive = isAktif(status);

            if (wasActive && !isActive) {
                // Remove from active index
                index.remove(klaim.getItemId());
            } else if (!wasActive && isActive) {
                // Add to active index
                index.put(klaim.getItemId(), klaim.getId());
            }

            dataStore.saveSnapshot();
            LOG.fine("Status klaim " + id + " diubah ke " + status);
        }
    }

    @Override
    public void delete(String id) {
        DataSnapshot snap = dataStore.getSnapshot();
        Klaim removed = snap.getKlaim().remove(id);
        if (removed != null) {
            if (isAktif(removed.getStatus())) {
                snap.getKlaimAktifIndex().remove(removed.getItemId());
            }
            dataStore.saveSnapshot();
            LOG.fine("Klaim dihapus: " + id);
        }
    }
}
