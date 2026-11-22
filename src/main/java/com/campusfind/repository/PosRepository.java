package com.campusfind.repository;

import com.campusfind.model.PosPenyimpanan;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link PosPenyimpanan} persistence operations.
 */
public interface PosRepository {

    void save(PosPenyimpanan pos);

    Optional<PosPenyimpanan> findById(String id);

    List<PosPenyimpanan> findAll();

    /**
     * Checks whether a pos with the given name already exists (case-insensitive).
     */
    boolean existsByNama(String nama);

    /**
     * Generates the next pos ID in the format "POS-" + %04d.
     */
    String nextId();
}
