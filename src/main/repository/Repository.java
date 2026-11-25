package com.campusfind.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface repositori generik untuk operasi persistensi entitas.
 *
 * @param <T>  tipe entitas
 * @param <ID> tipe identifier primer
 */
public interface Repository<T, ID> {

    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean deleteById(ID id);
}
