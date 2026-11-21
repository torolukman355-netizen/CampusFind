package com.campusfind;

import com.campusfind.integration.KlaimStatusFormatter;
import com.campusfind.model.StatusKlaim;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian KlaimStatusFormatter (Adapter Status Gabungan Teman)")
class KlaimStatusFormatterTest {

    @Test
    @DisplayName("Format status DITOLAK memuat alasan dalam tanda kurung")
    void testFormatDitolak() {
        String res = KlaimStatusFormatter.format(StatusKlaim.DITOLAK, "Ciri fisik tidak cocok");
        assertEquals("DITOLAK (Ciri fisik tidak cocok)", res);
    }

    @Test
    @DisplayName("Format status non-DITOLAK mengembalikan nama status enum")
    void testFormatNonDitolak() {
        assertEquals("DISETUJUI", KlaimStatusFormatter.format(StatusKlaim.DISETUJUI, null));
        assertEquals("DIAJUKAN", KlaimStatusFormatter.format(StatusKlaim.DIAJUKAN, null));
    }

    @Test
    @DisplayName("Ekstraksi alasan dari string DITOLAK (alasan)")
    void testExtractAlasan() {
        assertEquals("Bukti serial number salah",
                KlaimStatusFormatter.extractAlasan("DITOLAK (Bukti serial number salah)"));
        assertNull(KlaimStatusFormatter.extractAlasan("DISETUJUI"));
        assertNull(KlaimStatusFormatter.extractAlasan("DITOLAK"));
        assertNull(KlaimStatusFormatter.extractAlasan(null));
    }

    @Test
    @DisplayName("Parsing string teman ke StatusKlaim enum")
    void testParseStatus() {
        assertEquals(StatusKlaim.DITOLAK, KlaimStatusFormatter.parseStatus("DITOLAK (alasan apapun)"));
        assertEquals(StatusKlaim.DISETUJUI, KlaimStatusFormatter.parseStatus("DISETUJUI"));
        assertEquals(StatusKlaim.DIBATALKAN, KlaimStatusFormatter.parseStatus("DIBATALKAN"));
        assertEquals(StatusKlaim.SELESAI, KlaimStatusFormatter.parseStatus("SELESAI"));
        assertEquals(StatusKlaim.DIAJUKAN, KlaimStatusFormatter.parseStatus("DIAJUKAN"));
        assertEquals(StatusKlaim.DIAJUKAN, KlaimStatusFormatter.parseStatus(null));
    }
}
