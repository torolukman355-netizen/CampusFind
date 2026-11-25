package com.campusfind;

import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.model.StatusItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Pengujian ItemFactory")
class ItemFactoryTest {

    @Test
    @DisplayName("Kategori ELEKTRONIK menghasilkan instance BarangElektronik")
    void testElektronik() {
        Item item = ItemFactory.create(
                "ARC-0001",
                JenisLaporan.TEMUAN,
                "Laptop Asus",
                KategoriBarang.ELEKTRONIK,
                "Laptop warna hitam",
                "Perpustakaan Lt. 2",
                LocalDate.now(),
                "POS-0001",
                null,
                "Stiker apel di cover",
                StatusItem.TERSEDIA,
                LocalDateTime.now(),
                "Budi",
                "123456789", // 9 digit
                "Asus",
                "SN-12345"
        );

        assertInstanceOf(BarangElektronik.class, item);
        assertInstanceOf(Mahasiswa.class, item.getPelapor());
    }

    @Test
    @DisplayName("Kategori selain ELEKTRONIK menghasilkan instance BarangNonElektronik")
    void testNonElektronik() {
        Item item = ItemFactory.create(
                "ARC-0002",
                JenisLaporan.HILANG,
                "KTM Hilang",
                KategoriBarang.NON_ELEKTRONIK,
                "KTM atas nama Siti",
                "Kantin Pusat",
                LocalDate.now(),
                null,
                null,
                null,
                StatusItem.DICARI,
                LocalDateTime.now(),
                "Siti",
                "123456789012345678", // 18 digit
                "Biru",
                "Plastik"
        );

        assertInstanceOf(BarangNonElektronik.class, item);
        assertInstanceOf(Petugas.class, item.getPelapor());
    }

    @Test
    @DisplayName("Nomor identitas 9 digit menghasilkan pelapor bertipe Mahasiswa")
    void testMahasiswa9Digit() {
        Item item = ItemFactory.create(
                "ARC-0003",
                JenisLaporan.TEMUAN,
                "Buku Catatan",
                KategoriBarang.NON_ELEKTRONIK,
                "Buku bersampul cokelat",
                "Gedung A R.101",
                LocalDate.now(),
                "POS-0001",
                null,
                null,
                StatusItem.TERSEDIA,
                LocalDateTime.now(),
                "Ahmad",
                "502521100", // 9 digit
                "Cokelat",
                "Kertas"
        );

        assertInstanceOf(Mahasiswa.class, item.getPelapor());
    }

    @Test
    @DisplayName("Nomor identitas 18 digit menghasilkan pelapor bertipe Petugas")
    void testPetugas18Digit() {
        Item item = ItemFactory.create(
                "ARC-0004",
                JenisLaporan.TEMUAN,
                "Kunci Motor",
                KategoriBarang.NON_ELEKTRONIK,
                "Kunci Honda gantungan hitam",
                "Parkir Rektorat",
                LocalDate.now(),
                "POS-0001",
                null,
                null,
                StatusItem.TERSEDIA,
                LocalDateTime.now(),
                "Pak Joko",
                "198001012005011001", // 18 digit
                "Hitam",
                "Besi"
        );

        assertInstanceOf(Petugas.class, item.getPelapor());
    }
}
