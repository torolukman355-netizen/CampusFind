# Setup Database (Docker + DBeaver)

Database PostgreSQL untuk CampusFind dijalankan lewat Docker, sehingga tidak perlu
install server PostgreSQL di laptop.

## 1. Jalankan database

Dari folder project:

```bash
docker compose up -d
```

Cek status (harus `healthy`):

```bash
docker ps --filter name=campusfind-postgres
```

Matikan database (data tetap tersimpan di volume):

```bash
docker compose down
```

Hapus database beserta seluruh datanya:

```bash
docker compose down -v
```

Melihat log:

```bash
docker logs -f campusfind-postgres
```

## 2. Kredensial

| Item     | Nilai                                    |
| :------- | :--------------------------------------- |
| Host     | `localhost`                              |
| Port     | `5432`                                   |
| Database | `campusfind`                             |
| User     | `campusfind`                             |
| Password | `campusfind`                             |
| JDBC URL | `jdbc:postgresql://localhost:5432/campusfind` |

Nilai ini sudah ada di `database.properties` (diabaikan git). Ubah di
`docker-compose.yml` bila ingin kredensial lain.

## 3. Uji koneksi dari aplikasi

```bash
mvn -q compile exec:java -Dexec.mainClass=com.campusfind.tools.DatabaseConnectionCheck
```

## 4. Buka di DBeaver

1. Buka aplikasi **DBeaver**.
2. Klik ikon **New Database Connection** (ikon steker + di toolbar), atau menu
   **Database → New Database Connection**.
3. Pilih **PostgreSQL** → **Next**.
4. Isi form koneksi:
   - **Host**: `localhost`
   - **Port**: `5432`
   - **Database**: `campusfind`
   - **Username**: `campusfind`
   - **Password**: `campusfind`
   - Centang **Save password** bila tidak ingin mengetik ulang.
5. Klik **Test Connection**. Bila DBeaver menawarkan mengunduh driver PostgreSQL,
   klik **Download** lalu tunggu selesai. Harus muncul pesan *Connected*.
6. Klik **Finish**.
7. Di panel kiri, buka `campusfind` → **Databases** → `campusfind` → **Schemas** →
   `public` → **Tables**.

Membuat/menjalankan SQL: klik kanan database `campusfind` → **SQL Editor** →
**New SQL Editor**, lalu tulis query dan tekan `Ctrl/Cmd + Enter`.

## 5. Kembali ke PostgreSQL Homebrew (opsional)

Bila ingin memakai PostgreSQL yang terinstall via Homebrew lagi:

```bash
docker compose down
brew services start postgresql@16
```
