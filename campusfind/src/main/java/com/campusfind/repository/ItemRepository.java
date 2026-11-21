package com.campusfind.repository;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.StatusItem;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Item} persistence operations.
 */
public interface ItemRepository {

    void save(Item item);

    Optional<Item> findById(String id);

    List<Item> findAll();

    List<Item> findByJenis(JenisLaporan jenis);

    List<Item> findByStatus(StatusItem status);

    /**
     * Searches items by matching the query (case-insensitive) against
     * nama, id, and lokasiKejadian.
     */
    List<Item> search(String query);

    /**
     * Generates the next item ID in the format "ARC-" + %04d.
     */
    String nextId();

    void updateStatus(String id, StatusItem status);
}
