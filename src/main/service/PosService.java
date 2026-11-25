package com.campusfind.service;

import com.campusfind.model.PosPenyimpanan;
import com.campusfind.repository.PosRepository;
import com.campusfind.validation.PosValidator;
import com.campusfind.validation.ValidationResult;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Logika bisnis untuk pengelolaan pos penyimpanan.
 */
public class PosService {

    private static final Logger LOG = Logger.getLogger(PosService.class.getName());

    private final PosRepository posRepository;

    public PosService(PosRepository posRepository) {
        this.posRepository = posRepository;
    }

    /**
     * Menambahkan pos penyimpanan baru.
     *
     * @param nama   nama pos (e.g. "Pos Satpam Rektorat")
     * @param lokasi lokasi penyerahan
     * @param label  label pendek (e.g. "POS REKTORAT")
     * @return pos yang telah disimpan
     */
    public PosPenyimpanan tambah(String nama, String lokasi, String label) {
        List<String> existingNames = posRepository.findAll().stream()
                .map(PosPenyimpanan::getNama)
                .collect(Collectors.toList());

        ValidationResult result = PosValidator.validateAll(nama, existingNames, lokasi, label);
        if (!result.isValid()) {
            throw new IllegalArgumentException(String.join("\n", result.getErrors()));
        }

        String id = posRepository.nextId();
        PosPenyimpanan pos = new PosPenyimpanan(id, nama.trim(), lokasi.trim(), label.trim());
        posRepository.save(pos);

        LOG.info(() -> "Pos ditambahkan: " + pos.getNama() + " (" + id + ")");
        return pos;
    }

    /**
     * Mengambil pos yang sudah ada berdasarkan ID atau nama (case-insensitive),
     * atau membuat pos baru secara dinamis jika belum ada.
     *
     * @param namaTempat nama atau ID tempat penitipan yang diketikkan pengguna
     * @return PosPenyimpanan yang cocok atau pos baru yang disimpan
     */
    public PosPenyimpanan getOrCreateByNama(String namaTempat) {
        if (namaTempat == null || namaTempat.trim().isBlank()) {
            return posRepository.findAll().stream().findFirst().orElseGet(() -> {
                String id = posRepository.nextId();
                PosPenyimpanan def = new PosPenyimpanan(id, "Pos Keamanan Kampus", "Gedung Pusat", "POS UTAMA");
                posRepository.save(def);
                return def;
            });
        }

        String cleaned = namaTempat.trim();

        // 1. Cek by ID langsung
        Optional<PosPenyimpanan> byId = posRepository.findById(cleaned);
        if (byId.isPresent()) {
            return byId.get();
        }

        // 2. Cek by nama case-insensitive
        for (PosPenyimpanan p : posRepository.findAll()) {
            if (p.getNama().equalsIgnoreCase(cleaned)) {
                return p;
            }
        }

        // 3. Buat pos baru secara dinamis
        String id = posRepository.nextId();
        String label = cleaned.length() > 20 ? cleaned.substring(0, 20).toUpperCase() : cleaned.toUpperCase();
        PosPenyimpanan newPos = new PosPenyimpanan(id, cleaned, cleaned, label);
        posRepository.save(newPos);
        return newPos;
    }

    public List<PosPenyimpanan> findAll() {
        return posRepository.findAll();
    }

    public Optional<PosPenyimpanan> findById(String id) {
        return posRepository.findById(id);
    }
}
