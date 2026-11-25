package com.campusfind.repository;

import java.nio.file.Path;

/**
 * Implementasi {@link RepositoryProvider} menggunakan FileDataStore lokal.
 * Siap digantikan oleh implementasi SQL/JDBC pada pengembangan database lanjutan.
 */
public class FileRepositoryProvider implements RepositoryProvider {

    private final FileDataStore dataStore;
    private final ItemRepository itemRepository;
    private final KlaimRepository klaimRepository;
    private final PosRepository posRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public FileRepositoryProvider() {
        this(Path.of("data"));
    }

    public FileRepositoryProvider(Path dataDir) {
        this.dataStore = new FileDataStore(dataDir);
        this.itemRepository = new FileItemRepository(dataStore);
        this.klaimRepository = new FileKlaimRepository(dataStore);
        this.posRepository = new FilePosRepository(dataStore);
        this.notificationRepository = new FileNotificationRepository(dataStore);
        this.userRepository = new FileUserRepository(dataDir);
    }

    @Override
    public ItemRepository items() {
        return itemRepository;
    }

    @Override
    public KlaimRepository klaim() {
        return klaimRepository;
    }

    @Override
    public PosRepository pos() {
        return posRepository;
    }

    @Override
    public NotificationRepository notifications() {
        return notificationRepository;
    }

    @Override
    public UserRepository users() {
        return userRepository;
    }

    @Override
    public String getBackendType() {
        return "FileDataStore (SQL-Ready DAO Interface)";
    }
}
