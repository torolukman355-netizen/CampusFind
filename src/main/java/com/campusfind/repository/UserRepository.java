package com.campusfind.repository;

import com.campusfind.model.UserAccount;

import java.util.List;
import java.util.Optional;

/**
 * Kontrak Repository SQL-Ready untuk persistensi akun pengguna (US-022).
 * Dapat diimplementasikan oleh FileUserRepository (saat ini) maupun JdbcUserRepository (SQL nantinya).
 */
public interface UserRepository {
    void save(UserAccount account);
    Optional<UserAccount> findById(String id);
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByNomorIdentitas(String nomorIdentitas);
    List<UserAccount> findAll();
    boolean existsByEmail(String email);
    String nextId();
}
