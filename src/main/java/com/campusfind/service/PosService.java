package com.campusfind.service;

import com.campusfind.model.PosPenyimpanan;
import com.campusfind.repository.PosRepository;
import com.campusfind.validation.PosValidator;
import com.campusfind.validation.ValidationResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    public List<PosPenyimpanan> findAll() {
        return posRepository.findAll();
    }

    public Optional<PosPenyimpanan> findById(String id) {
        return posRepository.findById(id);
    }
}
