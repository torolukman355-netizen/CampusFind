package com.campusfind;

import com.campusfind.model.Admin;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.FileKlaimRepository;
import com.campusfind.repository.FilePosRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.ui.pages.AdminPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Smoke Test Dashboard Admin")
class AdminPageSmokeTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Dashboard Admin dapat dibangun dengan data kosong")
    void dashboardAdminDapatDibangun() {
        FileDataStore dataStore = new FileDataStore(tempDir);
        ItemService itemService = new ItemService(
                new FileItemRepository(dataStore),
                new FilePosRepository(dataStore),
                Clock.systemDefaultZone());
        KlaimRepository klaimRepository = new FileKlaimRepository(dataStore);

        AppSession session = new AppSession();
        session.login(new Admin("Lukman", "admin01"));

        AdminPage page = new AdminPage(null, itemService, klaimRepository, session);

        assertEquals(1, page.getComponentCount(), "Halaman admin harus memiliki area konten");
    }
}
