package com.campusfind.repository;

import com.campusfind.model.UserAccount;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementasi berbasis file lokal untuk {@link UserRepository}.
 * Terpisah ke file `users.dat` agar tetap menjaga kompatibilitas skema dataStore utama.
 */
public class FileUserRepository implements UserRepository {

    private static final Logger LOG = Logger.getLogger(FileUserRepository.class.getName());

    private final Path userFile;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, UserAccount> accounts = new LinkedHashMap<>();

    public FileUserRepository() {
        this(Path.of("data"));
    }

    public FileUserRepository(Path dataDir) {
        this.userFile = dataDir.resolve("users.dat");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori data pengguna", e);
        }
        load();
    }

    private void load() {
        lock.lock();
        try {
            if (!Files.exists(userFile)) {
                return;
            }
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(userFile))) {
                Object obj = ois.readObject();
                if (obj instanceof Map<?, ?> map) {
                    accounts.clear();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() instanceof String key && entry.getValue() instanceof UserAccount val) {
                            accounts.put(key, val);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Gagal membaca users.dat, memulai akun baru: " + e.getMessage(), e);
            }
        } finally {
            lock.unlock();
        }
    }

    private void persist() {
        lock.lock();
        try {
            Path temp = userFile.resolveSibling("users.dat.tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(temp))) {
                oos.writeObject(new LinkedHashMap<>(accounts));
            }
            Files.move(temp, userFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Gagal menyimpan users.dat: " + e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void save(UserAccount account) {
        lock.lock();
        try {
            accounts.put(account.getId(), account);
            persist();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<UserAccount> findById(String id) {
        lock.lock();
        try {
            return Optional.ofNullable(accounts.get(id));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String norm = email.trim().toLowerCase(Locale.ROOT);
        lock.lock();
        try {
            return accounts.values().stream()
                    .filter(a -> a.getEmail().trim().toLowerCase(Locale.ROOT).equals(norm))
                    .findFirst();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<UserAccount> findByNomorIdentitas(String nomorIdentitas) {
        if (nomorIdentitas == null) return Optional.empty();
        lock.lock();
        try {
            return accounts.values().stream()
                    .filter(a -> a.getNomorIdentitas().equalsIgnoreCase(nomorIdentitas.trim()))
                    .findFirst();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<UserAccount> findAll() {
        lock.lock();
        try {
            return new ArrayList<>(accounts.values());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public String nextId() {
        lock.lock();
        try {
            int max = accounts.size();
            for (String key : accounts.keySet()) {
                if (key.startsWith("USR-")) {
                    try {
                        int n = Integer.parseInt(key.substring(4));
                        if (n > max) max = n;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return String.format("USR-%03d", max + 1);
        } finally {
            lock.unlock();
        }
    }
}