package com.campusfind;

import com.campusfind.exception.AksesDitolakException;
import com.campusfind.model.Admin;
import com.campusfind.model.HakAkses;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.model.Role;
import com.campusfind.service.AksesService;
import com.campusfind.service.AppSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pengujian Otorisasi Hak Akses")
class AksesServiceTest {

    private AppSession session;
    private AksesService aksesService;

    @BeforeEach
    void setUp() {
        session = new AppSession();
        aksesService = new AksesService(session);
    }

    @Test
    @DisplayName("Pengguna belum login tidak memiliki hak apa pun")
    void tamuTidakPunyaHak() {
        assertFalse(aksesService.boleh(HakAkses.LIHAT_TEMUAN));

        AksesDitolakException ex = assertThrows(AksesDitolakException.class,
                () -> aksesService.wajibPunyaHak(HakAkses.BUAT_LAPORAN));
        assertEquals("Silakan login terlebih dahulu.", ex.getMessage());
    }

    @Test
    @DisplayName("Mahasiswa boleh melapor tetapi ditolak saat memverifikasi")
    void mahasiswaDitolakSaatVerifikasi() {
        session.login(new Mahasiswa("Nadia", "123456789"));

        assertTrue(aksesService.boleh(HakAkses.BUAT_LAPORAN));
        assertDoesNotThrow(() -> aksesService.wajibPunyaHak(HakAkses.BUAT_LAPORAN));

        AksesDitolakException ex = assertThrows(AksesDitolakException.class,
                () -> aksesService.wajibPunyaHak(HakAkses.VERIFIKASI_LAPORAN));
        assertTrue(ex.getMessage().contains("Mahasiswa"));
        assertTrue(ex.getMessage().contains(HakAkses.VERIFIKASI_LAPORAN.getLabel()));
    }

    @Test
    @DisplayName("Petugas lolos verifikasi dan ditolak saat mengakses statistik admin")
    void petugasDitolakSaatStatistik() {
        session.login(new Petugas("Pak Budi", "123456789012345678"));

        assertDoesNotThrow(() -> aksesService.wajibPunyaHak(HakAkses.VERIFIKASI_LAPORAN));

        assertThrows(AksesDitolakException.class,
                () -> aksesService.wajibPunyaHak(HakAkses.LIHAT_STATISTIK));
    }

    @Test
    @DisplayName("wajibRole menolak role yang tidak sesuai")
    void wajibRole() {
        session.login(new Admin("Lukman", "admin01"));

        assertDoesNotThrow(() -> aksesService.wajibRole(Role.ADMIN));

        AksesDitolakException ex = assertThrows(AksesDitolakException.class,
                () -> aksesService.wajibRole(Role.PETUGAS));
        assertTrue(ex.getMessage().contains("Petugas"));
    }

    @Test
    @DisplayName("Setelah logout seluruh hak akses hilang")
    void logoutMenghapusHak() {
        session.login(new Admin("Lukman", "admin01"));
        assertTrue(aksesService.boleh(HakAkses.LIHAT_STATISTIK));

        session.clear();
        assertFalse(aksesService.boleh(HakAkses.LIHAT_STATISTIK));
        assertThrows(AksesDitolakException.class,
                () -> aksesService.wajibLogin());
    }
}
