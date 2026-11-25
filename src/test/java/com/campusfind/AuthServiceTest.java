package com.campusfind;

import com.campusfind.exception.AksesDitolakException;
import com.campusfind.model.Role;
import com.campusfind.model.User;
import com.campusfind.service.AppSession;
import com.campusfind.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pengujian Autentikasi Berbasis Role")
class AuthServiceTest {

    private AppSession session;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        session = new AppSession();
        authService = new AuthService(session);
    }

    @Test
    @DisplayName("Login Mahasiswa dengan NIM 9 digit berhasil")
    void loginMahasiswa() {
        User user = authService.login("123456789", "Nadia", Role.MAHASISWA);

        assertEquals(Role.MAHASISWA, user.getRole());
        assertEquals("Nadia", user.getNama());
        assertEquals("123456789", user.getNomorIdentitas());
        assertTrue(session.isLoggedIn());
        assertEquals(Role.MAHASISWA, session.getRole());
    }

    @Test
    @DisplayName("Login Petugas dengan NIP 18 digit berhasil")
    void loginPetugas() {
        User user = authService.login("123456789012345678", "Pak Budi", Role.PETUGAS);

        assertEquals(Role.PETUGAS, user.getRole());
        assertEquals(Role.PETUGAS, session.getRole());
    }

    @Test
    @DisplayName("Login Admin memakai nama pengguna")
    void loginAdmin() {
        User user = authService.login("admin01", "Lukman", Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
        assertEquals("admin01", user.getNomorIdentitas());
        assertTrue(session.punyaHak(com.campusfind.model.HakAkses.LIHAT_STATISTIK));
    }

    @Test
    @DisplayName("NIM yang tidak sesuai role ditolak dan sesi tetap kosong")
    void nimSalahDitolak() {
        AksesDitolakException ex = assertThrows(AksesDitolakException.class,
                () -> authService.login("123456789", "Nadia", Role.PETUGAS));

        assertEquals("NIP Petugas harus 18 digit.", ex.getMessage());
        assertFalse(session.isLoggedIn());
    }

    @Test
    @DisplayName("Role kosong ditolak")
    void roleKosongDitolak() {
        assertThrows(AksesDitolakException.class,
                () -> authService.login("123456789", "Nadia", null));
        assertFalse(session.isLoggedIn());
    }

    @Test
    @DisplayName("Login tanpa role menebak role dari panjang identitas")
    void loginTanpaRole() {
        authService.login("123456789012345678");
        assertEquals(Role.PETUGAS, session.getRole());
    }

    @Test
    @DisplayName("Nama tampilan dibuat otomatis bila tidak diisi")
    void namaOtomatis() {
        User user = authService.login("123456789", "   ", Role.MAHASISWA);
        assertEquals("Mahasiswa 123456789", user.getNama());
    }

    @Test
    @DisplayName("Logout mengosongkan sesi")
    void logout() {
        authService.login("123456789", "Nadia", Role.MAHASISWA);
        authService.logout();

        assertFalse(authService.isLoggedIn());
        assertFalse(session.isLoggedIn());
        assertNull(session.getRole());
        assertNull(session.getNomorIdentitas());
        assertTrue(authService.penggunaAktif().isEmpty());
    }

    @Test
    @DisplayName("Pendaftaran akun Mahasiswa berhasil dengan data valid")
    void daftarMahasiswaBerhasil() {
        com.campusfind.repository.FileUserRepository userRepo = new com.campusfind.repository.FileUserRepository();
        AuthService serviceWithRepo = new AuthService(session, userRepo);

        com.campusfind.model.Mahasiswa mhs = serviceWithRepo.daftarMahasiswa(
                "Ahmad Dahlan",
                "230192837",
                "ahmad@student.kampus.ac",
                "secret123"
        );

        assertEquals("Ahmad Dahlan", mhs.getNama());
        assertEquals("230192837", mhs.getNomorIdentitas());
        assertEquals("ahmad@student.kampus.ac", mhs.getEmail());
        assertEquals(Role.MAHASISWA, mhs.getRole());
        assertTrue(session.isLoggedIn());
        assertEquals(Role.MAHASISWA, session.getRole());

        // Verifikasi tersimpan di repositori
        assertTrue(userRepo.findById("230192837").isPresent());
    }

    @Test
    @DisplayName("Pendaftaran Mahasiswa gagal jika NIM bukan 9 digit angka")
    void daftarMahasiswaNimTidakValid() {
        com.campusfind.repository.FileUserRepository userRepo = new com.campusfind.repository.FileUserRepository();
        AuthService serviceWithRepo = new AuthService(session, userRepo);

        // Kurang dari 9 digit
        AksesDitolakException ex1 = assertThrows(AksesDitolakException.class, () ->
                serviceWithRepo.daftarMahasiswa("Siti", "12345", "siti@student.kampus.ac", "password123"));
        assertEquals("NIM Mahasiswa harus 9 digit.", ex1.getMessage());

        // Mengandung huruf
        AksesDitolakException ex2 = assertThrows(AksesDitolakException.class, () ->
                serviceWithRepo.daftarMahasiswa("Siti", "12345678A", "siti@student.kampus.ac", "password123"));
        assertEquals("Hanya boleh berisi angka.", ex2.getMessage());
    }

    @Test
    @DisplayName("Pendaftaran Mahasiswa gagal jika NIM atau Email sudah terdaftar")
    void daftarMahasiswaDuplikatDitolak() {
        com.campusfind.repository.FileUserRepository userRepo = new com.campusfind.repository.FileUserRepository();
        AuthService serviceWithRepo = new AuthService(session, userRepo);

        serviceWithRepo.daftarMahasiswa("Mahasiswa 1", "111222333", "user1@student.kampus.ac", "rahasia1");

        // Duplikat NIM
        AksesDitolakException exNim = assertThrows(AksesDitolakException.class, () ->
                serviceWithRepo.daftarMahasiswa("Mahasiswa 2", "111222333", "user2@student.kampus.ac", "rahasia2"));
        assertTrue(exNim.getMessage().contains("sudah terdaftar"));

        // Duplikat Email
        AksesDitolakException exEmail = assertThrows(AksesDitolakException.class, () ->
                serviceWithRepo.daftarMahasiswa("Mahasiswa 3", "999888777", "user1@student.kampus.ac", "rahasia3"));
        assertTrue(exEmail.getMessage().contains("sudah terdaftar"));
    }

    @Test
    @DisplayName("Admin dapat memberikan akses akun Petugas baru dengan NIP 18 digit")
    void tambahPetugasOlehAdminBerhasil() {
        com.campusfind.repository.FileUserRepository userRepo = new com.campusfind.repository.FileUserRepository();
        AuthService serviceWithRepo = new AuthService(session, userRepo);

        // Login sebagai Admin
        serviceWithRepo.login("admin01", "Pak Admin", Role.ADMIN);

        com.campusfind.model.Petugas petugas = serviceWithRepo.tambahPetugasOlehAdmin(
                "Slamet Riyadi",
                "198701012015031003",
                "slamet@kampus.ac",
                "petugas123"
        );

        assertEquals("Slamet Riyadi", petugas.getNama());
        assertEquals("198701012015031003", petugas.getNomorIdentitas());
        assertEquals("slamet@kampus.ac", petugas.getEmail());
        assertEquals(Role.PETUGAS, petugas.getRole());

        // Verifikasi tersimpan di repositori
        assertTrue(userRepo.findById("198701012015031003").isPresent());
    }

    @Test
    @DisplayName("Bukan Admin ditolak saat mencoba menambah akun Petugas")
    void tambahPetugasOlehBukanAdminDitolak() {
        com.campusfind.repository.FileUserRepository userRepo = new com.campusfind.repository.FileUserRepository();
        AuthService serviceWithRepo = new AuthService(session, userRepo);

        // Login sebagai Mahasiswa
        serviceWithRepo.login("123456789", "Budi", Role.MAHASISWA);

        AksesDitolakException ex = assertThrows(AksesDitolakException.class, () ->
                serviceWithRepo.tambahPetugasOlehAdmin(
                        "Petugas Palsu",
                        "199001012020121001",
                        "palsu@kampus.ac",
                        "pass123"
                ));
        assertTrue(ex.getMessage().contains("Hanya Administrator"));
    }
}
