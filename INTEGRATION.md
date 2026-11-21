# Panduan Integrasi Modul CampusFind (Fakhry / Petugas Dashboard & Fitya / Rekonsiliasi)

Dokumen ini menjelaskan kontrak integrasi teknis antara modul **Petugas (`PetugasDashboardPanel.java`)** dan modul **Kandidat Cocok, Klaim & Notifikasi (`com.campusfind.*`)** tanpa mengubah file asli di `src/`.

---

## 1. Peta Status Laporan & Klaim (`StatusMapper` & `KlaimStatusFormatter`)

Modul petugas menggunakan representasi `String` bebas untuk status laporan dan klaim, sedangkan modul CampusFind menggunakan enum bertipe kuat (`StatusItem` dan `StatusKlaim`).

### 1.1 Tabel Pemetaan Status Laporan Barang

| Status Teman (`String`) | Jenis Laporan | `StatusItem` (CampusFind) | Keterangan & Perilaku Sistem |
| :--- | :--- | :--- | :--- |
| `DILAPORKAN` | `HILANG` / `TEMUAN` | `DICARI` / `TERSEDIA` | Laporan awal mahasiswa. Belum diverifikasi pos satpam. |
| `DIVERIFIKASI` | `TEMUAN` | `StatusItem.TERSEDIA` | Barang temuan resmi berada di pos dan siap dicocokkan atau diklaim. |
| `DIVERIFIKASI` | `HILANG` | `StatusItem.DICARI` | Laporan kehilangan aktif dipantau oleh algoritma rekonsiliasi. |
| `COCOK` | `HILANG` / `TEMUAN` | `DICARI` / `TERSEDIA` | **Penanda dinamis**: hasil evaluasi kecocokan $\ge 70\%$. Bukan status fisik barang. |
| `DIKLAIM` | `TEMUAN` | `StatusItem.DALAM_PROSES_KLAIM` | Klaim telah diajukan/disetujui, barang dikunci dari pengajuan lain. |
| `DIKEMBALIKAN` | `TEMUAN` | `StatusItem.DISERAHKAN` | Barang telah diserahterimakan kepada pemilik sah secara fisik di pos. |

Gunakan kelas adapter: `com.campusfind.integration.StatusMapper.toStatusItem(statusString, jenis)` dan `StatusMapper.toStatusTeman(statusItem)`.

### 1.2 Format Status Klaim Terpadu (`KlaimStatusFormatter`)

Modul petugas menggabungkan status klaim dan alasan penolakan dalam satu string: `"DITOLAK (alasan)"`.
Modul CampusFind memisahkannya menjadi field `StatusKlaim status` dan `String alasanPenolakan`.

Gunakan kelas adapter:
```java
// Modul CampusFind -> Modul Petugas:
String formatted = KlaimStatusFormatter.format(klaim.getStatus(), klaim.getAlasanPenolakan());
// Hasil: "DITOLAK (Ciri fisik remote tidak cocok)" atau "DISETUJUI"

// Modul Petugas -> Modul CampusFind:
StatusKlaim status = KlaimStatusFormatter.parseStatus(statusString);
String alasan = KlaimStatusFormatter.extractAlasan(statusString);
```

---

## 2. Kontrak API `KlaimService` untuk Aksi Petugas

Aksi persetujuan dan penolakan klaim wajib mematuhi aturan bisnis transaksional dan keamanan data:

### 2.1 Persetujuan Klaim (`KlaimService.setujui`)
```java
public void setujui(String klaimId, Petugas aktor);
```
- **Prasyarat**:
  - `klaimId` valid dan klaim saat ini berstatus `StatusKlaim.DIAJUKAN`.
  - `aktor` harus merupakan instance `Petugas` yang terautentikasi (bukan mahasiswa).
- **Efek Samping**:
  1. Status klaim target berubah menjadi `StatusKlaim.DISETUJUI`.
  2. Status item temuan terkunci menjadi `StatusItem.DALAM_PROSES_KLAIM`.
  3. Seluruh klaim aktif lain (`DIAJUKAN`) pada barang temuan yang sama **otomatis ditolak** dengan alasan baku:
     `"Otomatis: klaim lain untuk barang ini telah disetujui"`.
  4. Notifikasi in-app tipe `KLAIM_DISETUJUI` dikirim ke pemenang klaim.
  5. Notifikasi in-app tipe `KLAIM_DITOLAK` dikirim ke seluruh pengaju klaim lainnya yang otomatis tertolak.

### 2.2 Penolakan Klaim (`KlaimService.tolak`)
```java
public void tolak(String klaimId, String alasan, Petugas aktor);
```
- **Prasyarat**:
  - `klaimId` valid dan klaim saat ini berstatus `StatusKlaim.DIAJUKAN`.
  - `alasan` wajib diisi, tidak boleh kosong, dan minimal 5 karakter.
  - `aktor` harus merupakan instance `Petugas`.
- **Efek Samping**:
  1. Status klaim berubah menjadi `StatusKlaim.DITOLAK`.
  2. Alasan penolakan disimpan di `klaim.setAlasanPenolakan(alasan)`.
  3. Status item temuan dikembalikan menjadi `StatusItem.TERSEDIA` sehingga dapat diklaim kembali oleh pihak lain.
  4. Notifikasi in-app tipe `KLAIM_DITOLAK` dikirimkan ke pengaju klaim memuat alasan penolakan.

---

## 3. Usulan Perubahan Kode (Proposed Diffs) pada `src/PetugasDashboardPanel.java`

Berikut adalah titik-titik pada file teman yang direkomendasikan untuk mendelegasikan logika ke `KlaimService` dan `KandidatService`:

### 3.1 Pada `aksiSetujuiKlaim()` (Garis ~Baris 481)
```diff
--- src/PetugasDashboardPanel.java (Original)
+++ src/PetugasDashboardPanel.java (Usulan Terintegrasi)
@@ -490,25 +490,14 @@
-        if (item != null) {
-            if (item.terkunci) {
-                JOptionPane.showMessageDialog(this, "Barang ini sudah dikembalikan dan terkunci!", "Ditolak", JOptionPane.ERROR_MESSAGE);
-                return;
-            }
-            item.ubahStatus("DIKLAIM", namaPetugas);
-        }
-
-        // Setujui klaim terpilih & otomatis tolak klaim lain pada barang yang sama
-        for (KlaimRecord k : masterKlaims) {
-            if (k.idBarang.equals(klaimDipilih.idBarang) && "DIAJUKAN".equals(k.statusKlaim)) {
-                if (k.idKlaim.equals(klaimDipilih.idKlaim)) {
-                    k.statusKlaim = "DISETUJUI";
-                } else {
-                    k.statusKlaim = "DITOLAK (Otomatis: Klaim lain disetujui)";
-                }
-            }
-        }
+        try {
+            Petugas petugasAktor = new Petugas(nipPetugas, namaPetugas, posIdPetugas);
+            klaimService.setujui(klaimDipilih.idKlaim, petugasAktor);
+            // Sinkronkan state model lokal teman
+            klaimDipilih.statusKlaim = "DISETUJUI";
+            item.ubahStatus("DIKLAIM", namaPetugas);
+        } catch (Exception ex) {
+            JOptionPane.showMessageDialog(this, "Gagal menyetujui klaim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
+            return;
+        }
```

### 3.2 Pada `aksiTolakKlaim()` (Garis ~Baris 515)
```diff
--- src/PetugasDashboardPanel.java (Original)
+++ src/PetugasDashboardPanel.java (Usulan Terintegrasi)
@@ -524,10 +524,15 @@
             return;
         }
 
         String idKlaim = (String) modelKlaim.getValueAt(row, 0);
-        KlaimRecord klaim = findKlaimById(idKlaim);
-        if (klaim != null) {
-            klaim.statusKlaim = "DITOLAK (" + alasan + ")";
-        }
+        try {
+            Petugas petugasAktor = new Petugas(nipPetugas, namaPetugas, posIdPetugas);
+            klaimService.tolak(idKlaim, alasan.trim(), petugasAktor);
+            KlaimRecord klaim = findKlaimById(idKlaim);
+            if (klaim != null) {
+                klaim.statusKlaim = KlaimStatusFormatter.format(StatusKlaim.DITOLAK, alasan);
+            }
+        } catch (Exception ex) {
+            JOptionPane.showMessageDialog(this, "Gagal menolak klaim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
+            return;
+        }
```

### 3.3 Pada `jalankanSmartMatchingBackground()` (Garis ~Baris 435)
```diff
--- src/PetugasDashboardPanel.java (Original)
+++ src/PetugasDashboardPanel.java (Usulan Terintegrasi)
@@ -442,16 +442,15 @@
-                Thread.sleep(1200); // Simulasi pencocokan skor >= 70
-
-                for (ItemRecord existing : masterItems) {
-                    if (!existing.id.equals(itemBaru.id)
-                            && "DIVERIFIKASI".equals(existing.status)
-                            && !existing.jenis.equalsIgnoreCase(itemBaru.jenis)
-                            && existing.kategori.equalsIgnoreCase(itemBaru.kategori)) {
-                        ItemRecord hilang = "Hilang".equalsIgnoreCase(itemBaru.jenis) ? itemBaru : existing;
-                        ItemRecord temuan = "Temuan".equalsIgnoreCase(itemBaru.jenis) ? itemBaru : existing;
-                        return new MatchRecord(hilang.id, hilang.nama, temuan.id, temuan.nama, 85);
-                    }
-                }
-                return null;
+                // Gunakan CandidateMatchingStrategy dan KandidatService asli CampusFind:
+                List<KandidatCocokItem> kandidat = kandidatService.cariKandidat(itemBaru.id, itemBaru.pelaporNim);
+                if (!kandidat.isEmpty()) {
+                    KandidatCocokItem top = kandidat.get(0);
+                    return new MatchRecord(
+                            itemBaru.id, itemBaru.nama,
+                            top.getItemTemuan().getId(), top.getItemTemuan().getNama(),
+                            top.getSkor()
+                    );
+                }
+                return null;
```

---

## 4. Evaluasi Kesenjangan Arsitektural & Rekomendasi Mitigasi

1. **Stub Skor Pencocokan Tetap (85 vs Evaluasi Pembobotan):**
   - *Kondisi saat ini*: `PetugasDashboardPanel` menetapkan skor tetap 85 jika kategori cocok.
   - *Rekomendasi*: Integrasikan dengan `WeightedAttributeStrategy` yang menghitung nama barang (30%), deskripsi (20%), lokasi (20%), kronologi waktu (20%), dan atribut spesifik (10%), serta override 100% untuk nomor seri identik.
2. **Penggabungan Status dan Alasan (`String` gabungan):**
   - *Kondisi saat ini*: String `"DITOLAK (alasan)"` rawan korupsi jika alasan memuat karakter kurung `)` atau tanda kutip.
   - *Rekomendasi*: Gunakan `KlaimStatusFormatter` sebagai adapter sementara, dan buat field terpisah `alasanPenolakan` di masa mendatang.
3. **Pembangkitan ID Laporan Berisiko Tabrakan:**
   - *Kondisi saat ini*: ID dibuat dari `System.currentTimeMillis() % 10000`, yang menghasilkan domain angka sempit 0–9999.
   - *Rekomendasi*: Gunakan generator terurut `ARC-%04d` atau UUID seperti pada `FileItemRepository.nextId()`.
4. **Identitas Pengguna pada Model Laporan Teman:**
   - *Kondisi saat ini*: `LaporanMahasiswaItem` hanya mencatat nama mahasiswa tanpa NIM.
   - *Rekomendasi*: Tambahkan field `nomorIdentitas` (NIM) agar pengiriman notifikasi in-app dapat ditargetkan tepat sasaran kepada pelapor barang.
