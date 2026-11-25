package com.campusfind.repository;

import com.campusfind.exception.StorageException;
import com.campusfind.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementasi in-memory / file fallback untuk {@link UserRepository}.
 */
public class FileUserRepository implements UserRepository {

    private final Map<String, User> userMap = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "user tidak boleh null");
        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            for (User existing : userMap.values()) {
                if (email.equalsIgnoreCase(existing.getEmail())
                        && !existing.getNomorIdentitas().equals(user.getNomorIdentitas())) {
                    throw new StorageException("Email sudah terdaftar: " + email);
                }
            }
        }
        userMap.put(user.getNomorIdentitas(), user);
        return user;
    }

    @Override
    public Optional<User> findById(String nomorIdentitas) {
        if (nomorIdentitas == null) return Optional.empty();
        return Optional.ofNullable(userMap.get(nomorIdentitas.trim()));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String target = email.trim().toLowerCase();
        return userMap.values().stream()
                .filter(u -> target.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(userMap.values());
    }

    @Override
    public boolean deleteById(String nomorIdentitas) {
        if (nomorIdentitas == null) return false;
        return userMap.remove(nomorIdentitas.trim()) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByNomorIdentitas(String nomorIdentitas) {
        return findById(nomorIdentitas).isPresent();
    }
}
