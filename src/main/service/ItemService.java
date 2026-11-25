package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.StatusItem;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.validation.LaporanValidator;
import com.campusfind.validation.ValidationResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logika bisnis untuk pengelolaan barang (laporan hilang/temuan).
 */
public class ItemService {

    private static final Logger LOG = Logger.getLogger(ItemService.class.getName());

    private final ItemRepository itemRepository;
    private final PosRepository posRepository;
    private final Clock clock;
    private final List<ItemCreatedListener> itemCreatedListeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    public ItemService(ItemRepository itemRepository, PosRepository posRepository, Clock clock) {
        this.itemRepository = itemRepository;
        this.posRepository = posRepository;
        this.clock = clock;
    }

    /**
     * Melaporkan barang hilang atau temuan.
     *
     * @return item yang telah disimpan
     */
    public Item laporkan(JenisLaporan jenis, String namaBarang, KategoriBarang kategori,
                         String deskripsi, String lokasiKejadian, LocalDate tanggalKejadian,
                         String posId, File foto, String ciriRahasia,
                         String namaPelapor, String nomorIdentitas,
                         String merekAtauWarna, String nomorSeriAtauBahan) {

        ValidationResult result = LaporanValidator.validateAll(
                namaBarang, lokasiKejadian, deskripsi, tanggalKejadian,
                posId, jenis, ciriRahasia, foto, nomorIdentitas, namaPelapor, clock);

        if (!result.isValid()) {
            throw new IllegalArgumentException(String.join("\n", result.getErrors()));
        }

        // Resolusi pos penyimpanan / tempat penitipan (bisa berupa ID pos atau teks bebas yang diketik pengguna)
        String resolvedPosId = null;
        if (jenis == JenisLaporan.TEMUAN && posId != null && !posId.isBlank()) {
            Optional<PosPenyimpanan> existing = posRepository.findById(posId.trim());
            if (existing.isPresent()) {
                resolvedPosId = existing.get().getId();
            } else {
                PosPenyimpanan matched = null;
                for (PosPenyimpanan p : posRepository.findAll()) {
                    if (p.getNama().equalsIgnoreCase(posId.trim())) {
                        matched = p;
                        break;
                    }
                }
                if (matched == null) {
                    String nextPosId = posRepository.nextId();
                    String label = posId.trim().length() > 20 ? posId.trim().substring(0, 20).toUpperCase() : posId.trim().toUpperCase();
                    matched = new PosPenyimpanan(nextPosId, posId.trim(), posId.trim(), label);
                    posRepository.save(matched);
                }
                resolvedPosId = matched.getId();
            }
        }

        String id = itemRepository.nextId();
        StatusItem status = (jenis == JenisLaporan.TEMUAN) ? StatusItem.TERSEDIA : StatusItem.DICARI;

        String fotoPath = null;
        byte[] fotoBytes = null;
        if (foto != null) {
            fotoPath = copyFoto(foto);
            try {
                if (foto.exists()) {
                    fotoBytes = Files.readAllBytes(foto.toPath());
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Gagal membaca bytes foto: " + e.getMessage(), e);
            }
        }

        Item item = ItemFactory.create(
                id, jenis, namaBarang, kategori, deskripsi,
                lokasiKejadian, tanggalKejadian, resolvedPosId, fotoPath, ciriRahasia,
                status, LocalDateTime.now(clock),
                namaPelapor, nomorIdentitas,
                merekAtauWarna, nomorSeriAtauBahan);
        if (fotoBytes != null) {
            item.setFotoBytes(fotoBytes);
        }

        itemRepository.save(item);
        LOG.info(() -> "Barang dilaporkan: " + item.kode() + " (" + jenis + ")");
        notifyItemCreated(item);
        return item;
    }

    public void addItemCreatedListener(ItemCreatedListener listener) {
        if (listener != null) {
            itemCreatedListeners.add(listener);
        }
    }

    public void removeItemCreatedListener(ItemCreatedListener listener) {
        itemCreatedListeners.remove(listener);
    }

    private void notifyItemCreated(Item item) {
        for (ItemCreatedListener listener : itemCreatedListeners) {
            try {
                listener.onItemCreated(item);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Kegagalan listener saat pemrosesan laporan baru: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Menyalin foto ke direktori data/images/{UUID}.{ext}.
     *
     * @param source berkas sumber foto
     * @return path relatif hasil salinan
     */
    public String copyFoto(File source) {
        try {
            String originalName = source.getName();
            String ext = "";
            int dotIdx = originalName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalName.substring(dotIdx + 1).toLowerCase(Locale.ROOT);
            }

            String targetName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path targetDir = Path.of("data", "images");
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(targetName);
            Files.copy(source.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.fine(() -> "Foto disalin ke " + targetPath);
            return targetDir.resolve(targetName).toString();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Gagal menyalin foto", e);
            throw new RuntimeException("Gagal menyalin foto: " + e.getMessage(), e);
        }
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public List<Item> findByJenis(JenisLaporan jenis) {
        return itemRepository.findByJenis(jenis);
    }

    public List<Item> findByStatus(StatusItem status) {
        return itemRepository.findByStatus(status);
    }

    public List<Item> search(String keyword) {
        return itemRepository.search(keyword);
    }

    public Optional<Item> findById(String id) {
        return itemRepository.findById(id);
    }
}
