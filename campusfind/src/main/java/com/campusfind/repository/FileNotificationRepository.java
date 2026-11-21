package com.campusfind.repository;

import com.campusfind.model.DataSnapshot;
import com.campusfind.model.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * File-backed implementation of {@link NotificationRepository}.
 * All operations delegate to the {@link DataSnapshot} managed by {@link FileDataStore}.
 */
public class FileNotificationRepository implements NotificationRepository {

    private static final Logger LOG = Logger.getLogger(FileNotificationRepository.class.getName());

    private final FileDataStore dataStore;

    public FileNotificationRepository(FileDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void save(Notification notification) {
        DataSnapshot snap = dataStore.getSnapshot();
        snap.getNotifikasi().add(notification);
        dataStore.saveSnapshot();
        LOG.fine("Notifikasi disimpan: " + notification.getId());
    }

    @Override
    public Optional<Notification> findById(String id) {
        DataSnapshot snap = dataStore.getSnapshot();
        for (Notification n : snap.getNotifikasi()) {
            if (n.getId().equals(id)) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Notification> findByPenerima(String nomorIdentitas) {
        DataSnapshot snap = dataStore.getSnapshot();
        List<Notification> result = new ArrayList<>();
        for (Notification n : snap.getNotifikasi()) {
            if (n.getPenerimaNomorIdentitas().equals(nomorIdentitas)) {
                result.add(n);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public int countBelumDibaca(String nomorIdentitas) {
        DataSnapshot snap = dataStore.getSnapshot();
        int count = 0;
        for (Notification n : snap.getNotifikasi()) {
            if (n.getPenerimaNomorIdentitas().equals(nomorIdentitas) && !n.isDibaca()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void markAsRead(String id, String nomorIdentitas) {
        DataSnapshot snap = dataStore.getSnapshot();
        for (Notification n : snap.getNotifikasi()) {
            if (n.getId().equals(id)) {
                if (!n.getPenerimaNomorIdentitas().equals(nomorIdentitas)) {
                    throw new SecurityException("Anda tidak memiliki akses untuk menandai notifikasi pengguna lain.");
                }
                n.setDibaca(true);
                dataStore.saveSnapshot();
                LOG.fine("Notifikasi ditandai dibaca: " + id + " oleh pemilik " + nomorIdentitas);
                return;
            }
        }
    }

    @Override
    public void markAllAsRead(String nomorIdentitas) {
        DataSnapshot snap = dataStore.getSnapshot();
        boolean changed = false;
        for (Notification n : snap.getNotifikasi()) {
            if (n.getPenerimaNomorIdentitas().equals(nomorIdentitas) && !n.isDibaca()) {
                n.setDibaca(true);
                changed = true;
            }
        }
        if (changed) {
            dataStore.saveSnapshot();
            LOG.fine("Semua notifikasi ditandai dibaca untuk penerima: " + nomorIdentitas);
        }
    }

    @Override
    @Deprecated
    public void markAsRead(String id) {
        DataSnapshot snap = dataStore.getSnapshot();
        for (Notification n : snap.getNotifikasi()) {
            if (n.getId().equals(id)) {
                n.setDibaca(true);
                dataStore.saveSnapshot();
                LOG.fine("Notifikasi ditandai dibaca (legacy): " + id);
                return;
            }
        }
    }
}
