package com.campusfind.repository;

import com.campusfind.model.DataSnapshot;
import com.campusfind.model.PosPenyimpanan;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * File-backed implementation of {@link PosRepository}.
 * All operations delegate to the {@link DataSnapshot} managed by {@link FileDataStore}.
 */
public class FilePosRepository implements PosRepository {

    private static final Logger LOG = Logger.getLogger(FilePosRepository.class.getName());

    private final FileDataStore dataStore;

    public FilePosRepository(FileDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void save(PosPenyimpanan pos) {
        DataSnapshot snap = dataStore.getSnapshot();
        snap.getPos().put(pos.getId(), pos);
        dataStore.saveSnapshot();
        LOG.fine("PosPenyimpanan disimpan: " + pos.getId());
    }

    @Override
    public Optional<PosPenyimpanan> findById(String id) {
        DataSnapshot snap = dataStore.getSnapshot();
        return Optional.ofNullable(snap.getPos().get(id));
    }

    @Override
    public List<PosPenyimpanan> findAll() {
        DataSnapshot snap = dataStore.getSnapshot();
        return List.copyOf(snap.getPos().values());
    }

    @Override
    public boolean existsByNama(String nama) {
        if (nama == null) {
            return false;
        }
        DataSnapshot snap = dataStore.getSnapshot();
        String lowerNama = nama.toLowerCase();
        for (PosPenyimpanan pos : snap.getPos().values()) {
            if (pos.getNama().toLowerCase().equals(lowerNama)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String nextId() {
        DataSnapshot snap = dataStore.getSnapshot();
        int nextSeq = snap.getPos().size() + 1;
        String id = String.format("POS-%04d", nextSeq);
        LOG.fine("ID pos berikutnya: " + id);
        return id;
    }
}
