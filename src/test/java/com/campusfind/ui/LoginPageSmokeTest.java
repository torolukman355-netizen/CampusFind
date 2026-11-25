package com.campusfind.ui;

import com.campusfind.model.Role;
import com.campusfind.service.AppSession;
import com.campusfind.service.AuthService;
import com.campusfind.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Smoke Test Halaman Login")
class LoginPageSmokeTest {

    @Test
    @DisplayName("Halaman Login berhasil diinisialisasi dengan komponen-komponennya")
    void testLoginPageInitialization() {
        AppSession session = new AppSession();
        AuthService authService = new AuthService(session);

        LoginPage loginPage = new LoginPage(null, authService, session);
        assertNotNull(loginPage);
        assertTrue(loginPage.getComponentCount() > 0);

        // Uji perpindahan ke kartu registrasi Mahasiswa dan kembali ke kartu login
        loginPage.showRegisterCard();
        loginPage.showLoginCard();
    }
}
