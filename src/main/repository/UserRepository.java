package com.campusfind.repository;

import com.campusfind.model.User;

import java.util.Optional;

/**
 * Interface repositori khusus entitas {@link User}.
 */
public interface UserRepository extends Repository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNomorIdentitas(String nomorIdentitas);
}
