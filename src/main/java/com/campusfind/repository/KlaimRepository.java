package com.campusfind.repository;

import com.campusfind.model.Klaim;
import com.campusfind.model.StatusKlaim;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Klaim} persistence operations.
 */
public interface KlaimRepository {

    void save(Klaim klaim);

    Optional<Klaim> findById(String id);

    List<Klaim> findAll();

    List<Klaim> findByPengklaim(String nomorIdentitas);

    /**
     * Finds an active claim for the given item ID.
     * Active means status is DIAJUKAN or DISETUJUI.
     */
    Optional<Klaim> findAktifByItemId(String itemId);

    /**
     * Generates the next claim ID in the format "CLM-" + tahun + "-" + %04d.
     */
    String nextId(int tahun);

    void updateStatus(String id, StatusKlaim status);

    void delete(String id);
}
