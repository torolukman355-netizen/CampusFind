# PRD: CampusFind - Smart Lost and Found Kampus

## Introduction

CampusFind adalah aplikasi Java untuk mengelola barang hilang dan barang temuan di lingkungan kampus. Saat ini pelaporan dan pencarian barang biasanya dilakukan lewat pengumuman lisan atau grup chat, sehingga sulit dilacak dan rawan klaim palsu.

CampusFind memberi mahasiswa dan petugas satu sistem untuk:

- mencatat laporan barang hilang dan barang temuan,
- mencocokkan keduanya secara otomatis berdasarkan skor kemiripan (Smart Matching),
- melacak barang fisik lewat QR Code,
- memverifikasi kepemilikan sebelum barang diserahkan.

Proyek ini adalah tugas Pemrograman Berorientasi Objek Lanjutan (Kelompok 4). Aplikasi dibangun sepenuhnya dengan Java sebagai aplikasi desktop berbasis Java Swing, dan rancangan kelas harus menunjukkan penerapan class, object, enkapsulasi, pewarisan (inheritance), polimorfisme, abstract class, dan interface.

## Goals

- Mahasiswa dapat melaporkan barang hilang atau temuan dalam kurang dari 2 menit.
- Sistem menghitung skor kecocokan otomatis antara laporan hilang dan temuan dan memberi tahu pemilik bila ada kandidat cocok.
- Setiap barang temuan punya QR Code unik untuk pelacakan fisik.
- Barang hanya diserahkan setelah petugas memverifikasi bukti kepemilikan.
- Status barang selalu jelas: Dilaporkan, Diverifikasi, Cocok, Diklaim, Dikembalikan.
- Admin dan petugas dapat melihat statistik tingkat kehilangan dan efektivitas pengembalian.
- Rancangan kode menunjukkan class, object, enkapsulasi, inheritance, polimorfisme, abstract class, dan interface sesuai ketentuan mata kuliah.
- Aplikasi memenuhi ketentuan pengembangan: Java murni dengan antarmuka Swing/AWT, event handling, validasi input, dan penanganan kesalahan.

## Constraints (Ketentuan Pengembangan)

Ketentuan dari mata kuliah yang wajib dipenuhi seluruh story:

- Aplikasi wajib dibuat dengan Java.
- Antarmuka pengguna wajib memakai Java Swing atau AWT, bukan antarmuka web.
- Tidak boleh memakai HTML, CSS, JavaScript, Python, PHP, atau bahasa lain untuk membangun aplikasi.
- Database bersifat opsional; bila dipakai, koneksi lewat JDBC dan driver Java.
- Wajib menerapkan class, object, encapsulation, serta konsep OOP lanjutan yang relevan (inheritance, polymorphism, abstract class, interface).
- Wajib mengimplementasikan event handling, validasi input, dan penanganan kesalahan sesuai kebutuhan fitur.

## Pemetaan Konsep OOP

Penilaian utama proyek adalah penerapan OOP. Tabel ini menunjuk di mana setiap konsep diterapkan dan diuji.

| Konsep | Penerapan | Story |
|---|---|---|
| Class dan object | `User`, `Item`, `Lokasi`, `Claim`, `Notification` beserta instansnya | US-003, US-006, US-016, US-019 |
| Encapsulation | Atribut `private` dengan getter/setter bervalidasi, kata sandi ter-hash, `id` bersifat `final` | US-003, US-006 |
| Inheritance | `User` ke `Mahasiswa`/`Petugas`/`Admin`; `Item` ke `BarangElektronik`/`BarangNonElektronik`; `CampusFindException` ke subclass-nya | US-002, US-003, US-007 |
| Abstract class | `User` dan `Item` | US-003, US-006 |
| Interface | `Repository<T, ID>`, `Matchable`, `MatchingStrategy`, `NotificationChannel`, `Comparable<Item>` | US-004, US-006, US-014, US-016 |
| Polymorphism: overriding | `getMenuAkses()`, `tampilDashboard()`, `punyaHak()`, `tampilkanDetail()`, `hitungSkorMatching()`, `toString()` | US-003, US-007, US-010, US-014 |
| Polymorphism: overloading | `buatLaporan(...)`, `kirimNotif(...)`, konstruktor `BarangElektronik` | US-007, US-009, US-016 |
| Komposisi dan agregasi | `Item` memiliki `Lokasi` (komposisi); `Item` dan `Claim` merujuk `User`/`Mahasiswa` (agregasi) | US-006, US-019 |
| Enum dengan perilaku | `StatusBarang` (aturan transisi), `HakAkses`, `StatusKlaim` | US-003, US-012, US-019 |
| Anggota `static` dan `final` | Generator id, `InputValidator` | US-002, US-006 |
| Generics | `Repository<T, ID>` | US-004 |
| Design pattern | Strategy (`MatchingStrategy`), Factory (`ItemFactory`), dependency injection (`MatchingService`) | US-009, US-014, US-015 |
| Exception handling | Hierarki exception kustom | US-002 |
| Event handling | Listener Swing pada setiap layar | US-024 sampai US-031 |

## User Stories

### US-001: Inisialisasi proyek Java
**Description:** As a developer, I want a project skeleton so that all modules share one structure and build.

**Acceptance Criteria:**
- [ ] Proyek Maven (atau Gradle) dengan Java 17 atau lebih baru; seluruh kode aplikasi ditulis dalam Java (tanpa HTML, CSS, JavaScript, Python, PHP, atau bahasa lain)
- [ ] Package terpisah: `model`, `repository`, `service`, `ui`, `util`, `exception`
- [ ] JUnit terpasang dan satu tes contoh lulus
- [ ] Perintah build dari terminal berhasil tanpa error
- [ ] Typecheck/lint passes

### US-002: Hierarki exception kustom dan validasi input
**Description:** As a developer, I want custom exceptions and a reusable validator so that errors are handled consistently across the app.

**Acceptance Criteria:**
- [ ] Kelas dasar `CampusFindException` (turunan `Exception`) dengan subclass `ValidationException`, `AuthenticationException`, `AccessDeniedException`, `NotFoundException`, dan `StorageException`
- [ ] `ValidationException` dapat membawa pesan per kolom (nama kolom dan pesan)
- [ ] Kelas `InputValidator` bertipe `final` dengan konstruktor privat dan method `static`: wajib isi, panjang maksimum, format email, dan tanggal tidak di masa depan
- [ ] Konstruktor exception di-overload (hanya pesan, serta pesan dan penyebab) dan subclass memanggil `super(...)`
- [ ] Unit test untuk setiap jenis validasi (lulus dan gagal)
- [ ] Typecheck/lint passes

### US-003: Hierarki kelas User, Mahasiswa, Petugas, dan Admin
**Description:** As a developer, I want an abstract User class hierarchy so that role-specific behavior is modeled with inheritance and polymorphism.

**Acceptance Criteria:**
- [ ] Kelas abstrak `User` dengan atribut privat (id, nama, email, kata sandi ter-hash, status aktif) dan getter/setter; setter memvalidasi nilai
- [ ] `User` tidak dapat diinstansiasi langsung; konstruktor `User` dipanggil lewat `super(...)` dari subclass (constructor chaining)
- [ ] Method konkret `login()`, `logout()`, dan `getNama()` pada `User`
- [ ] Method abstrak `getMenuAkses()`, `tampilDashboard()`, dan `punyaHak(HakAkses)` di-override oleh `Mahasiswa`, `Petugas`, dan `Admin` dengan perilaku berbeda
- [ ] Enum `HakAkses` (BUAT_LAPORAN, VERIFIKASI_LAPORAN, PROSES_KLAIM, KELOLA_PENGGUNA, LIHAT_STATISTIK) dan hak tiap role ditetapkan di subclass-nya masing-masing
- [ ] `toString()` (tanpa kata sandi), `equals()`, dan `hashCode()` di-override berdasarkan id
- [ ] Kata sandi tidak pernah disimpan atau dicetak sebagai teks biasa
- [ ] Unit test: `List<User>` berisi satu objek tiap role; memanggil `getMenuAkses()` dan `punyaHak()` lewat referensi `User` menghasilkan hasil berbeda per role tanpa `instanceof`
- [ ] Typecheck/lint passes

### US-004: Penyimpanan data pengguna (interface repository dan JDBC)
**Description:** As a developer, I want a user repository so that accounts persist between runs.

**Acceptance Criteria:**
- [ ] Interface generik `Repository<T, ID>` (simpan, cari berdasarkan id, daftar semua, hapus) dan `UserRepository` sebagai turunannya dengan metode cari berdasarkan email
- [ ] Implementasi `JdbcUserRepository` memakai JDBC dengan driver PostgreSQL (`org.postgresql:postgresql`); koneksi dikelola satu kelas `DatabaseManager` dan skema tabel dibuat dari kode Java saat pertama dijalankan jika belum ada
- [ ] Host, port, nama database, pengguna, dan kata sandi dibaca dari berkas konfigurasi atau environment variable, bukan ditulis di kode
- [ ] Kegagalan koneksi (server mati atau kredensial salah) menghasilkan `StorageException` dengan pesan yang jelas
- [ ] `SQLException` dibungkus menjadi exception kustom (tidak bocor ke lapisan UI)
- [ ] Email harus unik; menyimpan email ganda ditolak dengan pesan jelas
- [ ] Unit test untuk simpan dan cari
- [ ] Typecheck/lint passes

### US-005: Autentikasi login berdasarkan role
**Description:** As a user, I want to log in with my account so that I only access features for my role.

**Acceptance Criteria:**
- [ ] `AuthService.login(email, password)` mengembalikan objek `User` yang sesuai role (Mahasiswa, Petugas, atau Admin)
- [ ] Kredensial salah menghasilkan pesan kesalahan tanpa membocorkan apakah email atau kata sandi yang salah
- [ ] `logout()` mengakhiri sesi aktif
- [ ] Unit test untuk login berhasil, login gagal, dan logout
- [ ] Typecheck/lint passes

### US-006: Model Item abstrak dan status barang
**Description:** As a developer, I want an abstract Item class so that all item types share core attributes and status.

**Acceptance Criteria:**
- [ ] Kelas abstrak `Item` dengan atribut privat: id, nama, kategori, warna, lokasi kejadian, waktu kejadian, deskripsi, lokasi penyimpanan, jenis laporan (HILANG atau TEMUAN), status, dan pelapor; setter hanya untuk atribut yang boleh berubah
- [ ] Lokasi kejadian dimodelkan sebagai kelas `Lokasi` (nama tempat, keterangan) yang dimiliki `Item` (komposisi); pelapor adalah referensi ke `User` (agregasi)
- [ ] `id` bersifat `final` dan dibuat oleh generator berbasis anggota `static` sehingga unik dan tidak dapat diubah
- [ ] `Item` mengimplementasikan `Comparable<Item>` (urut dari waktu kejadian terbaru) serta meng-override `toString()`, `equals()`, dan `hashCode()` berdasarkan id
- [ ] Enum `StatusBarang`: DILAPORKAN, DIVERIFIKASI, COCOK, DIKLAIM, DIKEMBALIKAN
- [ ] Method abstrak `tampilkanDetail()` dideklarasikan pada `Item`
- [ ] Interface `Matchable` mendeklarasikan `hitungSkorMatching(Item lain)` dan `Item` mengimplementasikannya
- [ ] Status awal setiap item baru adalah DILAPORKAN
- [ ] Typecheck/lint passes

### US-007: Subclass BarangElektronik dan BarangNonElektronik
**Description:** As a developer, I want specialized item classes so that each item type shows its own details via polymorphism.

**Acceptance Criteria:**
- [ ] `BarangElektronik` menambah atribut merk, nomor seri, dan ciri fisik
- [ ] `BarangNonElektronik` menambah atribut ciri fisik dan bahan atau jenis
- [ ] Keduanya meng-override `tampilkanDetail()` dengan keluaran berbeda
- [ ] Konstruktor di-overload (mis. `BarangElektronik` dengan dan tanpa nomor seri) dan selalu memanggil `super(...)`
- [ ] Keduanya meng-override `toString()` dengan memanggil `super.toString()` lalu menambahkan atributnya
- [ ] Unit test memanggil `tampilkanDetail()` lewat referensi bertipe `Item` untuk kedua subclass
- [ ] Typecheck/lint passes

### US-008: Penyimpanan data barang
**Description:** As a developer, I want an item repository so that reports persist and can be searched.

**Acceptance Criteria:**
- [ ] `ItemRepository` (turunan `Repository<Item, String>`) dengan implementasi JDBC: simpan, cari berdasarkan id, daftar berdasarkan jenis (HILANG atau TEMUAN), daftar berdasarkan pelapor, dan daftar berdasarkan status
- [ ] Hierarki `Item` dipetakan ke tabel dengan kolom tipe sebagai pembeda
- [ ] Subclass `BarangElektronik` dan `BarangNonElektronik` tersimpan dan terbaca kembali dengan tipe yang benar
- [ ] Unit test untuk simpan lalu baca kembali kedua tipe
- [ ] Typecheck/lint passes

### US-009: Mahasiswa membuat laporan barang hilang atau temuan
**Description:** As a mahasiswa, I want to report a lost or found item so that it is recorded in the system.

**Acceptance Criteria:**
- [ ] `Mahasiswa.buatLaporan(DataLaporan)` membuat `Item` dengan deskripsi, kategori, warna, lokasi, dan waktu; method di-overload dengan `buatLaporan(DataLaporan, File lampiranFoto)` yang menambahkan foto
- [ ] Validasi lewat `InputValidator`: nama, kategori, dan lokasi wajib diisi; waktu kejadian tidak boleh di masa depan; pelanggaran melempar `ValidationException` dengan pesan per kolom
- [ ] Pembuatan subclass dilakukan oleh `ItemFactory` (factory method): kategori elektronik menghasilkan `BarangElektronik`, kategori lain `BarangNonElektronik`; pemanggil hanya memegang tipe `Item`
- [ ] Laporan tersimpan dengan status DILAPORKAN dan terhubung ke pelapor
- [ ] Unit test untuk laporan valid dan laporan tidak valid
- [ ] Typecheck/lint passes

### US-010: Dashboard per role (polimorfisme)
**Description:** As a user, I want a dashboard that matches my role so that I see the information relevant to me.

**Acceptance Criteria:**
- [ ] `Mahasiswa` meng-override `tampilDashboard()` untuk menampilkan semua laporan miliknya beserta status terkini; `Petugas` menampilkan antrean verifikasi dan klaim; `Admin` menampilkan ringkasan statistik
- [ ] Mahasiswa tidak dapat melihat laporan milik mahasiswa lain
- [ ] Dashboard kosong menampilkan pesan 'Belum ada laporan'
- [ ] Unit test untuk pembatasan akses data
- [ ] Unit test memanggil `tampilDashboard()` lewat referensi `User` untuk ketiga role dan memastikan keluarannya berbeda
- [ ] Typecheck/lint passes

### US-011: Petugas memverifikasi laporan
**Description:** As a petugas, I want to verify incoming reports so that only valid reports enter matching.

**Acceptance Criteria:**
- [ ] `Petugas.verifikasiLaporan()` mengubah status dari DILAPORKAN menjadi DIVERIFIKASI, atau menolak dengan alasan tertulis
- [ ] Laporan yang ditolak tidak ikut dalam pencocokan
- [ ] Otorisasi memakai `punyaHak(HakAkses.VERIFIKASI_LAPORAN)` tanpa `instanceof`; pengguna tanpa hak mendapat `AccessDeniedException`
- [ ] Unit test untuk verifikasi, penolakan, dan akses tidak sah
- [ ] Typecheck/lint passes

### US-012: Aturan perubahan status barang
**Description:** As a developer, I want valid status transitions so that item status cannot jump to an impossible state.

**Acceptance Criteria:**
- [ ] `Petugas.updateStatus()` hanya mengizinkan urutan: DILAPORKAN, DIVERIFIKASI, COCOK, DIKLAIM, DIKEMBALIKAN
- [ ] Aturan transisi berada di dalam enum `StatusBarang` (field, konstruktor, serta method `bolehBerubahKe(StatusBarang)` dan `berikutnya()`), bukan di blok if/else pada pemanggil
- [ ] Transisi yang melompat atau mundur ditolak dengan pesan kesalahan
- [ ] Setiap perubahan status mencatat waktu dan pelaku
- [ ] Unit test untuk setiap transisi valid dan minimal tiga transisi tidak valid
- [ ] Typecheck/lint passes

### US-013: Lokasi penyimpanan barang temuan
**Description:** As a petugas, I want to record where a found item is stored so that owners know where to collect it.

**Acceptance Criteria:**
- [ ] Petugas dapat mengisi atau mengubah lokasi penyimpanan pada item berjenis TEMUAN
- [ ] Lokasi penyimpanan tampil di `tampilkanDetail()`
- [ ] Mengisi lokasi penyimpanan pada item berjenis HILANG ditolak
- [ ] Unit test untuk kedua kasus
- [ ] Typecheck/lint passes

### US-014: Perhitungan skor kecocokan
**Description:** As a developer, I want a matching score between two items so that the system can rank likely matches.

**Acceptance Criteria:**
- [ ] Interface `MatchingStrategy` (method `hitungSkor(Item a, Item b)`) dengan dua implementasi: `WeightedAttributeStrategy` (nama, kategori, warna, lokasi, waktu berbobot) dan `KeywordOnlyStrategy` (kemiripan kata kunci pada nama dan deskripsi)
- [ ] `Item.hitungSkorMatching(Item lain)` (dari `Matchable`) mengembalikan skor 0 sampai 100 dengan mendelegasikan ke `MatchingStrategy` yang diberikan lewat konstruktor atau setter (pola Strategy)
- [ ] `BarangElektronik` meng-override `hitungSkorMatching` untuk menambah bonus bila merk dan nomor seri sama, dengan memanggil `super.hitungSkorMatching(...)`
- [ ] Skor berasal dari kesamaan nama, kategori, warna, lokasi, dan waktu dengan bobot yang dapat dikonfigurasi (file konfigurasi atau konstanta bernama)
- [ ] Pasangan identik menghasilkan skor mendekati 100; pasangan tanpa kesamaan menghasilkan skor mendekati 0
- [ ] Perbandingan nama tidak membedakan huruf besar dan kecil
- [ ] Item sejenis (HILANG dengan HILANG) tidak dihitung
- [ ] Unit test dengan minimal lima pasangan contoh dan skor yang diharapkan
- [ ] Unit test membuktikan dua strategi dapat ditukar pada objek `Item` yang sama dan menghasilkan skor berbeda
- [ ] Typecheck/lint passes

### US-015: Layanan pencocokan otomatis
**Description:** As a mahasiswa, I want the system to find found-items that match my lost-item report so that I do not search manually.

**Acceptance Criteria:**
- [ ] Saat laporan berstatus DIVERIFIKASI, `MatchingService` membandingkannya dengan semua laporan lawan jenis yang berstatus DIVERIFIKASI
- [ ] Pasangan dengan skor di atas ambang batas (dapat dikonfigurasi, default 70) disimpan sebagai kandidat, diurutkan dari skor tertinggi
- [ ] Status kedua item menjadi COCOK hanya setelah petugas mengonfirmasi kandidat
- [ ] `MatchingService` menerima `MatchingStrategy` lewat konstruktor (dependency injection) sehingga strategi dapat diganti tanpa mengubah kelas layanan
- [ ] Unit test untuk kasus ada kandidat, tidak ada kandidat, dan skor tepat di ambang batas
- [ ] Typecheck/lint passes

### US-016: Notifikasi sistem
**Description:** As a mahasiswa, I want notifications when something changes so that I do not miss a match or claim result.

**Acceptance Criteria:**
- [ ] Kelas `Notification` (penerima, pesan, waktu, sudah dibaca) dan method `kirimNotif()`
- [ ] `kirimNotif()` di-overload: `kirimNotif(User, String)` dan `kirimNotif(User, String, Item)` untuk notifikasi yang terkait barang
- [ ] Pengiriman lewat interface `NotificationChannel` dengan implementasi `InAppNotificationChannel` sehingga kanal lain dapat ditambah tanpa mengubah pemanggil
- [ ] Notifikasi otomatis dibuat saat: ada kandidat cocok, laporan diverifikasi atau ditolak, klaim disetujui atau ditolak, dan barang dikembalikan
- [ ] Pengguna dapat melihat notifikasinya sendiri dan menandainya sudah dibaca
- [ ] Unit test untuk pembuatan dan pembatasan akses notifikasi
- [ ] Typecheck/lint passes

### US-017: Pembuatan QR Code unik untuk barang
**Description:** As a petugas, I want a unique QR Code per found item so that I can track the physical item.

**Acceptance Criteria:**
- [ ] Setiap item TEMUAN yang diverifikasi mendapat kode unik (tidak ada duplikat)
- [ ] `QRCodeService` menghasilkan gambar QR (pustaka seperti ZXing) dan menyimpannya sebagai file PNG
- [ ] Isi QR berupa kode unik item, bukan data pribadi
- [ ] Unit test: dua item menghasilkan dua kode berbeda dan QR dapat di-decode kembali ke kode yang sama
- [ ] Typecheck/lint passes

### US-018: Pencarian barang lewat kode QR
**Description:** As a petugas, I want to look up an item from its QR code so that I can see its status quickly.

**Acceptance Criteria:**
- [ ] Memasukkan kode atau membaca gambar QR menampilkan detail item lewat `tampilkanDetail()` dan statusnya
- [ ] Kode tidak dikenal menampilkan pesan 'Barang tidak ditemukan'
- [ ] Unit test untuk kode valid dan tidak valid
- [ ] Typecheck/lint passes

### US-019: Mahasiswa mengajukan klaim dengan bukti kepemilikan
**Description:** As a mahasiswa, I want to claim a found item with proof of ownership so that I can get it back.

**Acceptance Criteria:**
- [ ] Kelas `Claim` (id, item, pengaju, bukti kepemilikan berupa teks dan atau lampiran, waktu, status klaim) yang memiliki referensi ke `Item` dan `Mahasiswa` (asosiasi atau agregasi)
- [ ] Status klaim dimodelkan sebagai enum `StatusKlaim` (DIAJUKAN, DISETUJUI, DITOLAK)
- [ ] `Mahasiswa.ajukanKlaim()` hanya berlaku untuk item berstatus COCOK atau DIVERIFIKASI berjenis TEMUAN
- [ ] Bukti kepemilikan wajib diisi; klaim tanpa bukti ditolak
- [ ] Satu mahasiswa tidak dapat mengajukan dua klaim aktif untuk item yang sama
- [ ] Unit test untuk klaim valid, tanpa bukti, dan klaim ganda
- [ ] Typecheck/lint passes

### US-020: Petugas memeriksa dan memutuskan klaim
**Description:** As a petugas, I want to review a claim and its evidence so that items go only to the true owner.

**Acceptance Criteria:**
- [ ] `Petugas.cekKlaim()` menampilkan klaim beserta bukti dan detail item
- [ ] `Claim.prosesKlaim()` dan `ubahStatusKlaim()` mendukung status: DIAJUKAN, DISETUJUI, DITOLAK
- [ ] Klaim DISETUJUI mengubah status item menjadi DIKLAIM; klaim lain pada item yang sama otomatis DITOLAK
- [ ] Penolakan wajib mencantumkan alasan
- [ ] Notifikasi terkirim kepada pengaju klaim
- [ ] Unit test untuk setujui, tolak, dan klaim bersaing pada satu item
- [ ] Typecheck/lint passes

### US-021: Pencatatan serah terima barang
**Description:** As a petugas, I want to record the handover so that there is proof the item was returned.

**Acceptance Criteria:**
- [ ] Petugas dapat menandai item DIKLAIM sebagai DIKEMBALIKAN dengan mencatat tanggal, nama penerima, dan petugas yang menyerahkan
- [ ] Item yang belum berstatus DIKLAIM tidak dapat dikembalikan
- [ ] Item DIKEMBALIKAN tidak dapat diklaim atau diubah lagi
- [ ] Notifikasi terkirim kepada penerima
- [ ] Unit test untuk serah terima valid dan tidak valid
- [ ] Typecheck/lint passes

### US-022: Admin mengelola pengguna
**Description:** As an admin, I want to manage user accounts so that only legitimate users have access.

**Acceptance Criteria:**
- [ ] Admin dapat menambah, mengubah role, menonaktifkan, dan melihat daftar pengguna
- [ ] Pengguna nonaktif tidak dapat login
- [ ] Mahasiswa dan Petugas tidak dapat mengakses fungsi ini
- [ ] Unit test untuk setiap operasi dan pembatasan akses
- [ ] Typecheck/lint passes

### US-023: Layanan statistik
**Description:** As an admin, I want loss and return statistics so that I can judge how well the service works.

**Acceptance Criteria:**
- [ ] `StatistikService` menghitung jumlah laporan per kategori, per periode (bulan), dan per jenis (HILANG dan TEMUAN)
- [ ] Menghitung tingkat pengembalian: jumlah DIKEMBALIKAN dibagi jumlah laporan HILANG terverifikasi, dalam persen
- [ ] Menghitung rata-rata waktu dari laporan hingga pengembalian
- [ ] Data kosong menghasilkan nol, bukan pembagian dengan nol
- [ ] Unit test dengan dataset kecil yang hasilnya diketahui
- [ ] Typecheck/lint passes

### US-024: Kerangka aplikasi Swing dan layar login
**Description:** As a user, I want a Swing login screen and a menu that matches my role so that I only see what I can use.

**Acceptance Criteria:**
- [ ] Aplikasi dijalankan lewat `main()` yang membuat jendela di `SwingUtilities.invokeLater`
- [ ] `JFrame` utama memakai `CardLayout` untuk berpindah antara layar login dan layar per role
- [ ] Layar login: `JTextField` email, `JPasswordField` kata sandi, dan `JButton` Masuk dengan `ActionListener`; menekan Enter juga memicu login
- [ ] Input divalidasi dengan `InputValidator` sebelum login; pesan kesalahan tampil di bawah kolom, kegagalan autentikasi tampil lewat `JOptionPane`
- [ ] Setelah login, `JTabbedPane` atau `JMenuBar` hanya menampilkan menu sesuai role: Mahasiswa (laporan, dashboard, klaim), Petugas (verifikasi, klaim, QR, serah terima), Admin (pengguna, statistik)
- [ ] Menu Logout mengembalikan ke layar login dan mengosongkan kolom kata sandi
- [ ] `Thread.setDefaultUncaughtExceptionHandler` menampilkan dialog kesalahan umum dan mencatat detailnya ke log
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-025: Layar pelaporan mahasiswa (Swing)
**Description:** As a mahasiswa, I want a report form so that I can submit lost or found items without code.

**Acceptance Criteria:**
- [ ] Formulir dengan `JComboBox` jenis (hilang atau temuan) dan kategori, `JTextField` nama, warna, dan lokasi, `JTextArea` deskripsi, serta `JSpinner` untuk tanggal dan waktu
- [ ] Memilih kategori elektronik menampilkan kolom merk dan nomor seri lewat `ItemListener`; kategori lain menyembunyikannya
- [ ] Tombol Kirim memvalidasi semua kolom; kolom yang salah ditandai dengan pesan di sampingnya dan fokus berpindah ke kolom pertama yang salah
- [ ] Laporan yang berhasil menampilkan dialog konfirmasi dan mengosongkan formulir
- [ ] Kesalahan penyimpanan menampilkan pesan yang dapat dipahami, bukan jejak tumpukan (stack trace)
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-026: Dashboard dan notifikasi mahasiswa (Swing)
**Description:** As a mahasiswa, I want a dashboard with my reports, match candidates, and notifications so that I can follow progress.

**Acceptance Criteria:**
- [ ] `JTable` dengan `AbstractTableModel` menampilkan laporan milik pengguna: nama, jenis, status, dan tanggal
- [ ] Memilih baris (`ListSelectionListener`) menampilkan detail lewat `tampilkanDetail()` di panel samping
- [ ] Laporan HILANG yang punya kandidat cocok menampilkan daftar kandidat dengan skor
- [ ] Panel notifikasi menampilkan jumlah notifikasi belum dibaca dan tombol Tandai sudah dibaca
- [ ] Dashboard kosong menampilkan 'Belum ada laporan'
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-027: Layar klaim mahasiswa (Swing)
**Description:** As a mahasiswa, I want to submit a claim from the screen so that I can request my item back.

**Acceptance Criteria:**
- [ ] Dari kandidat cocok atau detail barang, tombol Ajukan klaim membuka dialog `JDialog` dengan `JTextArea` bukti kepemilikan
- [ ] Bukti kosong ditolak dengan pesan validasi; klaim ganda ditolak dengan pesan dari `ValidationException`
- [ ] Daftar klaim milik pengguna menampilkan status (DIAJUKAN, DISETUJUI, DITOLAK) dan alasan penolakan bila ada
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-028: Layar petugas untuk verifikasi laporan dan klaim (Swing)
**Description:** As a petugas, I want work screens for verification and claims so that I can process items efficiently.

**Acceptance Criteria:**
- [ ] `JTable` laporan menunggu verifikasi dengan tombol Terima dan Tolak; Tolak membuka dialog alasan yang wajib diisi
- [ ] `JTable` klaim menampilkan bukti kepemilikan dan detail barang; tombol Setujui dan Tolak dengan konfirmasi `JOptionPane`
- [ ] Proses yang lambat (mis. pencocokan setelah verifikasi) berjalan di `SwingWorker` dan antarmuka tidak membeku
- [ ] Setelah aksi, tabel diperbarui otomatis tanpa memuat ulang aplikasi
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-029: Layar QR Code dan serah terima petugas (Swing)
**Description:** As a petugas, I want to view QR codes and record handovers from the screen so that physical tracking is easy.

**Acceptance Criteria:**
- [ ] Gambar QR item tampil di `JLabel` lewat `ImageIcon`, dengan tombol Simpan sebagai PNG (`JFileChooser`)
- [ ] Kolom kode QR dan tombol Cari menampilkan detail serta status item; tombol Buka gambar QR membaca kode dari berkas gambar
- [ ] Formulir serah terima (nama penerima, tanggal) untuk item DIKLAIM, dengan validasi dan konfirmasi sebelum disimpan
- [ ] Kode tidak dikenal dan berkas gambar tidak valid menampilkan pesan yang jelas
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-030: Layar admin untuk manajemen pengguna (Swing)
**Description:** As an admin, I want a user management screen so that I can control who has access.

**Acceptance Criteria:**
- [ ] `JTable` daftar pengguna dengan role dan status aktif
- [ ] Dialog tambah dan ubah pengguna dengan validasi (email unik, kata sandi minimum 8 karakter)
- [ ] Tombol Nonaktifkan meminta konfirmasi; admin tidak dapat menonaktifkan akunnya sendiri
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-031: Layar statistik admin (Swing)
**Description:** As an admin, I want charts of loss and return statistics so that I can judge the service.

**Acceptance Criteria:**
- [ ] Grafik batang jumlah laporan per kategori dan per bulan, digambar dengan `Graphics2D` pada subclass `JPanel` (atau JFreeChart bila disetujui)
- [ ] Angka tingkat pengembalian dan rata-rata waktu pengembalian tampil di samping grafik
- [ ] Grafik menampilkan keadaan kosong yang jelas bila belum ada data
- [ ] Grafik tergambar ulang saat jendela diubah ukurannya
- [ ] Aplikasi dapat dijalankan dan layar dicek manual (smoke test)
- [ ] Typecheck/lint passes

### US-032: Dokumentasi penerapan konsep OOP
**Description:** As a student, I want a class diagram and a concept-to-code mapping so that the OOP design can be shown and graded clearly.

**Acceptance Criteria:**
- [ ] Diagram kelas UML (berkas gambar atau PDF) memuat hierarki `User`, hierarki `Item`, hierarki exception, semua interface, enum, serta hubungan komposisi dan agregasi
- [ ] Tabel pemetaan di README atau laporan: konsep OOP, nama kelas atau method, berkas, dan story, untuk setiap baris pada bagian 'Pemetaan Konsep OOP' di PRD ini
- [ ] Javadoc pada `User`, `Item`, semua interface publik, dan semua enum; perintah pembuatan Javadoc berjalan tanpa error
- [ ] Setiap konsep dalam tabel punya minimal satu unit test atau layar yang dapat didemonstrasikan
- [ ] Typecheck/lint passes

## Functional Requirements

- FR-1: Sistem harus mendukung tiga role: Mahasiswa, Petugas, dan Admin, dengan hak akses terpisah.
- FR-2: Sistem harus mengautentikasi pengguna berdasarkan email dan kata sandi ter-hash.
- FR-3: Mahasiswa harus dapat membuat laporan barang hilang maupun temuan dengan nama, kategori, warna, lokasi, waktu, dan deskripsi.
- FR-4: Sistem harus membedakan barang elektronik (merk, nomor seri) dan non-elektronik, dan menampilkan detail sesuai tipenya.
- FR-5: Petugas harus memverifikasi atau menolak setiap laporan sebelum laporan ikut dalam pencocokan.
- FR-6: Sistem harus menghitung skor kecocokan 0 sampai 100 antara laporan HILANG dan TEMUAN berdasarkan nama, kategori, warna, lokasi, dan waktu.
- FR-7: Sistem harus menampilkan kandidat kecocokan di atas ambang batas kepada pemilik laporan dan petugas.
- FR-8: Setiap barang temuan yang diverifikasi harus memiliki QR Code dengan kode unik.
- FR-9: Mahasiswa harus dapat mengajukan klaim dengan bukti kepemilikan; petugas harus menyetujui atau menolaknya secara manual.
- FR-10: Status barang harus mengikuti urutan Dilaporkan, Diverifikasi, Cocok, Diklaim, Dikembalikan dan tidak boleh melompat.
- FR-11: Sistem harus mengirim notifikasi otomatis untuk kandidat cocok, hasil verifikasi, hasil klaim, dan pengembalian.
- FR-12: Sistem harus mencatat serah terima (tanggal, penerima, petugas).
- FR-13: Sistem harus menyajikan statistik laporan per kategori dan periode serta tingkat pengembalian.
- FR-14: Kode harus menerapkan class dan object, enkapsulasi (atribut privat), inheritance (`User`, `Item`), abstract class (`User`, `Item`), interface (`Repository`, `Matchable`, `MatchingStrategy`, `NotificationChannel`), polimorfisme (overriding dan overloading), komposisi dan agregasi, enum dengan perilaku, serta generics, sesuai bagian 'Pemetaan Konsep OOP'.
- FR-20: Penerapan OOP harus terdokumentasi dengan diagram kelas dan tabel pemetaan konsep ke kode (US-032).
- FR-15: Seluruh kode aplikasi harus ditulis dalam Java; antarmuka pengguna harus memakai Swing atau AWT.
- FR-16: Setiap layar harus menangani event pengguna (klik tombol, perubahan pilihan, pemilihan baris, tombol Enter) lewat listener.
- FR-17: Semua input pengguna harus divalidasi sebelum diproses, dengan pesan kesalahan per kolom yang jelas.
- FR-18: Kesalahan harus ditangani dengan exception kustom dan ditampilkan sebagai pesan yang dapat dipahami pengguna, tanpa stack trace.
- FR-19: Bila database dipakai, akses data harus lewat JDBC dan terpisah dari UI melalui pola repository.

## Non-Goals (Out of Scope)

- Tidak ada pencocokan berbasis gambar atau pengenalan wajah.
- Tidak ada notifikasi lewat email, SMS, atau aplikasi chat; notifikasi hanya di dalam aplikasi.
- Tidak ada persetujuan klaim otomatis; keputusan klaim selalu manual oleh petugas.
- Tidak ada antarmuka berbasis web dan tidak ada kode HTML, CSS, JavaScript, Python, PHP, atau bahasa non-Java lain.
- Tidak ada aplikasi mobile native.
- Tidak ada integrasi dengan sistem akademik kampus atau single sign-on.
- Tidak ada pembayaran, hadiah, atau imbalan penemuan.
- Tidak ada pelacakan lokasi real-time barang.

## Design Considerations

- Tampilan sederhana, satu menu utama per role, dibangun dengan komponen Swing standar.
- Ukuran jendela minimum ditetapkan dan tata letak tetap rapi saat jendela diubah ukurannya (`LayoutManager`, bukan posisi absolut).
- Urutan tab (focus traversal) logis dan tombol utama dapat dipicu dari keyboard (Enter, mnemonic).
- Tampilkan status barang dengan label dan warna yang konsisten.
- Detail barang menyembunyikan informasi yang dapat memudahkan klaim palsu (mis. nomor seri lengkap) dari pengguna yang bukan pelapor.
- Kandidat kecocokan tampil dengan skor dan alasan singkat (atribut yang cocok).

## Technical Considerations

- Bahasa: Java 17 atau lebih baru; antarmuka dengan Swing/AWT; build dengan Maven atau Gradle; pengujian dengan JUnit.
- Penyimpanan: PostgreSQL lewat JDBC dengan driver PostgreSQL. Akses data dibungkus pola repository sehingga logika bisnis tidak bergantung pada PostgreSQL.
- Pengujian repository memakai database PostgreSQL terpisah (mis. `campusfind_test`) agar data asli tidak tersentuh; tes service memakai repository tiruan di memori.
- Kredensial database tidak boleh masuk ke repositori kode (gunakan berkas konfigurasi yang diabaikan git atau environment variable).
- Pola repository memisahkan penyimpanan dari logika bisnis sehingga penyimpanan mudah diganti.
- Pustaka QR Code: ZXing (pustaka Java) atau setara.
- Operasi yang lambat dijalankan di `SwingWorker`; seluruh pembaruan komponen UI terjadi di Event Dispatch Thread.
- Logika bisnis (service) tidak boleh bergantung pada kelas Swing agar dapat diuji dengan JUnit tanpa UI.
- Kata sandi disimpan sebagai hash (mis. BCrypt), bukan teks biasa.
- Bobot dan ambang batas pencocokan berada di konfigurasi, bukan tertanam di kode.
- Diagram kelas (UML) sebaiknya dihasilkan dari kode akhir sebagai bahan laporan tugas.

## Success Metrics

- Seluruh skenario utama berjalan dari ujung ke ujung: lapor hilang, lapor temuan, cocok, klaim, verifikasi, serah terima.
- Pada dataset uji berisi minimal 20 pasangan barang, pencocokan menempatkan pasangan yang benar di tiga kandidat teratas untuk setidaknya 80% kasus.
- Laporan baru dapat dibuat dalam waktu kurang dari 2 menit.
- Tidak ada klaim yang dapat disetujui tanpa bukti kepemilikan.
- Semua unit test lulus dan setiap pilar OOP terlihat jelas di kode dan laporan.
- Setiap baris pada tabel 'Pemetaan Konsep OOP' menunjuk ke kode nyata yang dapat didemonstrasikan lewat unit test atau layar.
- Perilaku berbeda per role dan per tipe barang tercapai lewat polimorfisme, bukan lewat `instanceof` atau rantai if/else.
- Dua strategi pencocokan dapat ditukar tanpa mengubah `MatchingService`.
- Setiap layar Swing memiliki event handling dan validasi input yang dapat didemonstrasikan.
- Input tidak valid dan kegagalan database tidak pernah membuat aplikasi berhenti atau menampilkan stack trace kepada pengguna.

## Open Questions

- Apakah pustaka grafik JFreeChart boleh dipakai, atau grafik harus digambar sendiri dengan Java2D?
- Apakah ada Admin yang terpisah dari Petugas dalam hal hak akses (PDF menyebut role Admin pada autentikasi, tetapi tidak membuat kelasnya)?
- Berapa lama barang temuan disimpan sebelum dianggap tidak diklaim, dan apa yang terjadi setelahnya?
- Apakah pemindaian QR harus memakai kamera? Rencana saat ini hanya input kode dan pembacaan berkas gambar karena kamera di Swing membutuhkan pustaka tambahan.
- Berapa bobot awal tiap atribut pada skor kecocokan (nama, kategori, warna, lokasi, waktu)?
