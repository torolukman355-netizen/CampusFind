package com.campusfind.repository;

import com.campusfind.model.DataSnapshot;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.StatusItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * File-backed implementation of {@link ItemRepository}.
 * All operations delegate to the {@link DataSnapshot} managed by {@link FileDataStore}.
 */
public class FileItemRepository implements ItemRepository {

    private static final Logger LOG = Logger.getLogger(FileItemRepository.class.getName());

    private final FileDataStore dataStore;

    public FileItemRepository(FileDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void save(Item item) {
        DataSnapshot snap = dataStore.getSnapshot();
        snap.getItems().put(item.getId(), item);
        dataStore.saveSnapshot();
        LOG.fine("Item disimpan: " + item.getId());
    }

    @Override
    public Optional<Item> findById(String id) {
        DataSnapshot snap = dataStore.getSnapshot();
        return Optional.ofNullable(snap.getItems().get(id));
    }

    @Override
    public List<Item> findAll() {
        DataSnapshot snap = dataStore.getSnapshot();
        List<Item> all = new ArrayList<>(snap.getItems().values());
        Collections.reverse(all);
        return List.copyOf(all);
    }

    @Override
    public List<Item> findByJenis(JenisLaporan jenis) {
        DataSnapshot snap = dataStore.getSnapshot();
        List<Item> result = new ArrayList<>();
        for (Item item : snap.getItems().values()) {
            if (item.getJenis() == jenis) {
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public List<Item> findByStatus(StatusItem status) {
        DataSnapshot snap = dataStore.getSnapshot();
        List<Item> result = new ArrayList<>();
        for (Item item : snap.getItems().values()) {
            if (item.getStatus() == status) {
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public List<Item> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String lowerQuery = query.toLowerCase();
        DataSnapshot snap = dataStore.getSnapshot();
        List<Item> result = new ArrayList<>();
        for (Item item : snap.getItems().values()) {
            if (containsIgnoreCase(item.getNama(), lowerQuery)
                    || containsIgnoreCase(item.getId(), lowerQuery)
                    || containsIgnoreCase(item.getLokasiKejadian(), lowerQuery)) {
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public String nextId() {
        DataSnapshot snap = dataStore.getSnapshot();
        int next = snap.getNextItemSeq() + 1;
        snap.setNextItemSeq(next);
        String id = String.format("ARC-%04d", next);
        dataStore.saveSnapshot();
        LOG.fine("ID item berikutnya: " + id);
        return id;
    }

    @Override
    public void updateStatus(String id, StatusItem status) {
        DataSnapshot snap = dataStore.getSnapshot();
        Item item = snap.getItems().get(id);
        if (item != null) {
            item.setStatus(status);
            dataStore.saveSnapshot();
            LOG.fine("Status item " + id + " diubah ke " + status);
        }
    }

    private boolean containsIgnoreCase(String text, String lowerQuery) {
        return text != null && text.toLowerCase().contains(lowerQuery);
    }
}
