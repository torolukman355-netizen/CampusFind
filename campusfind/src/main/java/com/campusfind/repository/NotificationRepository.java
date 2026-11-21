package com.campusfind.repository;

import com.campusfind.model.Notification;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Notification} persistence operations.
 */
public interface NotificationRepository {

    void save(Notification notification);

    Optional<Notification> findById(String id);

    List<Notification> findByPenerima(String nomorIdentitas);

    int countBelumDibaca(String nomorIdentitas);

    void markAsRead(String id, String nomorIdentitas);

    void markAllAsRead(String nomorIdentitas);

    @Deprecated
    void markAsRead(String id);
}
