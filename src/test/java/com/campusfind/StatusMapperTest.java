package com.campusfind;

import com.campusfind.integration.StatusMapper;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.StatusItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Pengujian StatusMapper (Adapter Integrasi Status Teman)")
class StatusMapperTest {

    @Test
    @DisplayName("Pemetaan status teman DIVERIFIKASI sesuai jenis laporan")
    void testToStatusItemDiverifikasi() {
        assertEquals(StatusItem.TERSEDIA, StatusMapper.toStatusItem("DIVERIFIKASI", JenisLaporan.TEMUAN));
        assertEquals(StatusItem.DICARI, StatusMapper.toStatusItem("DIVERIFIKASI", JenisLaporan.HILANG));
    }

    @Test
    @DisplayName("Pemetaan status teman DIKLAIM dan DIKEMBALIKAN")
    void testToStatusItemProsesDanSelesai() {
        assertEquals(StatusItem.DALAM_PROSES_KLAIM, StatusMapper.toStatusItem("DIKLAIM", JenisLaporan.TEMUAN));
        assertEquals(StatusItem.DISERAHKAN, StatusMapper.toStatusItem("DIKEMBALIKAN", JenisLaporan.TEMUAN));
    }

    @Test
    @DisplayName("Pemetaan status null atau blank kembali ke status default")
    void testToStatusItemNullOrBlank() {
        assertEquals(StatusItem.TERSEDIA, StatusMapper.toStatusItem(null, JenisLaporan.TEMUAN));
        assertEquals(StatusItem.DICARI, StatusMapper.toStatusItem("", JenisLaporan.HILANG));
    }

    @Test
    @DisplayName("Pemetaan balik StatusItem ke status modul teman")
    void testToStatusTeman() {
        assertEquals("DIVERIFIKASI", StatusMapper.toStatusTeman(StatusItem.TERSEDIA));
        assertEquals("DIVERIFIKASI", StatusMapper.toStatusTeman(StatusItem.DICARI));
        assertEquals("DIKLAIM", StatusMapper.toStatusTeman(StatusItem.DALAM_PROSES_KLAIM));
        assertEquals("DIKEMBALIKAN", StatusMapper.toStatusTeman(StatusItem.DISERAHKAN));
        assertEquals("DIVERIFIKASI", StatusMapper.toStatusTeman(null));
    }
}
