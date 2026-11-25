package com.campusfind.service;

import com.campusfind.model.Item;

/**
 * Listener untuk memantau pembuatan laporan barang baru (HILANG / TEMUAN).
 */
@FunctionalInterface
public interface ItemCreatedListener {

    /**
     * Dipanggil saat laporan barang baru berhasil dibuat dan disimpan.
     *
     * @param item barang yang baru dilaporkan
     */
    void onItemCreated(Item item);
}
