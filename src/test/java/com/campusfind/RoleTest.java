package com.campusfind;

import com.campusfind.model.Admin;
import com.campusfind.model.HakAkses;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.model.Role;
import com.campusfind.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pengujian Role dan Hak Akses")
class RoleTest {

    @Test
    @DisplayName("Mahasiswa hanya boleh melapor, mengklaim, dan melihat klaim sendiri")
    void mahasiswaPunyaHakTerbatas() {
        User mahasiswa = new Mahasiswa("Nadia", "123456789");

        assertEquals(Role.MAHASISWA, mahasiswa.getRole());
        assertTrue(mahasiswa.punyaHak(HakAkses.BUAT_LAPORAN));
        assertTrue(mahasiswa.punyaHak(HakAkses.AJUKAN_KLAIM));
        assertTrue(mahasiswa.punyaHak(HakAkses.LIHAT_KLAIM_SENDIRI));
        assertTrue(mahasiswa.punyaHak(HakAkses.LIHAT_TEMUAN));

        assertFalse(mahasiswa.punyaHak(HakAkses.VERIFIKASI_LAPORAN));
        assertFalse(mahasiswa.punyaHak(HakAkses.PUTUSKAN_KLAIM));
        assertFalse(mahasiswa.punyaHak(HakAkses.LIHAT_STATISTIK));
    }

    @Test
    @DisplayName("Petugas dapat memverifikasi dan memproses klaim, tetapi tidak membuat laporan")
    void petugasPunyaHakOperasional() {
        User petugas = new Petugas("Pak Budi", "123456789012345678");

        assertEquals(Role.PETUGAS, petugas.getRole());
        assertTrue(petugas.punyaHak(HakAkses.VERIFIKASI_LAPORAN));
        assertTrue(petugas.punyaHak(HakAkses.KELOLA_STATUS_ITEM));
        assertTrue(petugas.punyaHak(HakAkses.PUTUSKAN_KLAIM));
        assertTrue(petugas.punyaHak(HakAkses.SERAH_TERIMA));
        assertTrue(petugas.punyaHak(HakAkses.KELOLA_QR_CODE));

        assertFalse(petugas.punyaHak(HakAkses.BUAT_LAPORAN));
        assertFalse(petugas.punyaHak(HakAkses.AJUKAN_KLAIM));
        assertFalse(petugas.punyaHak(HakAkses.LIHAT_STATISTIK));
    }

    @Test
    @DisplayName("Admin memiliki seluruh hak Petugas ditambah statistik")
    void adminPunyaHakPenuh() {
        User admin = new Admin("Lukman", "admin01");

        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.punyaHak(HakAkses.LIHAT_STATISTIK));

        for (HakAkses hak : Role.PETUGAS.getHakAkses()) {
            assertTrue(admin.punyaHak(hak), "Admin harus memiliki hak: " + hak);
        }
    }

    @Test
    @DisplayName("Polimorfisme: hak akses berbeda per role tanpa instanceof")
    void polimorfismeTanpaInstanceof() {
        List<User> pengguna = List.of(
                new Mahasiswa("Nadia", "123456789"),
                new Petugas("Pak Budi", "123456789012345678"),
                new Admin("Lukman", "admin01")
        );

        long bisaVerifikasi = pengguna.stream()
                .filter(user -> user.punyaHak(HakAkses.VERIFIKASI_LAPORAN))
                .count();
        long bisaMelapor = pengguna.stream()
                .filter(user -> user.punyaHak(HakAkses.BUAT_LAPORAN))
                .count();

        assertEquals(2, bisaVerifikasi);
        assertEquals(1, bisaMelapor);
    }

    @Test
    @DisplayName("Panjang identitas tiap role sesuai aturan NIM/NIP/username")
    void panjangIdentitasPerRole() {
        assertEquals(9, Role.MAHASISWA.getPanjangIdentitas());
        assertEquals(18, Role.PETUGAS.getPanjangIdentitas());
        assertEquals(0, Role.ADMIN.getPanjangIdentitas());
    }

    @Test
    @DisplayName("Role dapat ditebak dari panjang nomor identitas")
    void tebakRoleDariNomor() {
        assertEquals(Role.MAHASISWA, Role.dariNomorIdentitas("123456789"));
        assertEquals(Role.PETUGAS, Role.dariNomorIdentitas("123456789012345678"));
    }

    @Test
    @DisplayName("toString pengguna menampilkan role, bukan data sensitif")
    void toStringMenampilkanRole() {
        User admin = new Admin("Lukman", "admin01");
        assertEquals("Lukman (Admin)", admin.toString());
    }
}
