-- =====================================================================
-- CampusFind Database Schema (MySQL / MariaDB / SQLite Compatible)
-- DDL untuk Tugas Besar PBOL - CampusFind
-- =====================================================================

CREATE DATABASE IF NOT EXISTS campusfind_db;
USE campusfind_db;

-- 1. Tabel Pengguna (US-001, US-002, US-022)
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    nama VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Petugas', 'Mahasiswa', 'Admin') NOT NULL,
    nomor_induk VARCHAR(50), -- NIP untuk Petugas, NIM untuk Mahasiswa
    aktif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabel Item / Barang (US-007, US-008, US-011, US-012, US-013, US-017, US-021)
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(50) PRIMARY KEY,
    jenis ENUM('HILANG', 'TEMUAN') NOT NULL,
    nama VARCHAR(150) NOT NULL,
    kategori ENUM('ELEKTRONIK', 'NON_ELEKTRONIK') NOT NULL,
    lokasi_kejadian VARCHAR(255) NOT NULL,
    pelapor VARCHAR(100) NOT NULL,
    status ENUM('DILAPORKAN', 'DIVERIFIKASI', 'COCOK', 'DIKLAIM', 'DIKEMBALIKAN', 'DITOLAK') DEFAULT 'DILAPORKAN',
    lokasi_simpan VARCHAR(100) DEFAULT '-', -- US-013 (Khusus Temuan)
    kode_qr VARCHAR(100) DEFAULT '-',       -- US-017 (Khusus Temuan Terverifikasi)
    terkunci BOOLEAN DEFAULT FALSE,         -- US-021 (Terkunci setelah DIKEMBALIKAN)
    
    -- Atribut Khusus Barang Elektronik (US-007)
    merk VARCHAR(100),
    nomor_seri VARCHAR(100),                -- Rahasia dari publik
    
    -- Atribut Khusus Barang Non-Elektronik (US-007)
    bahan VARCHAR(100),
    ciri_fisik TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabel Log Transisi Status (US-012)
CREATE TABLE IF NOT EXISTS log_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    pelaku VARCHAR(100) NOT NULL,
    catatan TEXT,
    waktu TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- 4. Tabel Pengajuan Klaim Mahasiswa (US-019, US-020)
CREATE TABLE IF NOT EXISTS klaim (
    id_klaim VARCHAR(50) PRIMARY KEY,
    item_id VARCHAR(50) NOT NULL,
    pengaju VARCHAR(100) NOT NULL,
    bukti TEXT NOT NULL,
    status_klaim ENUM('DIAJUKAN', 'DISETUJUI', 'DITOLAK') DEFAULT 'DIAJUKAN',
    catatan_petugas TEXT,
    tanggal_pengajuan TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- =====================================================================
-- DUMMY SEED DATA
-- =====================================================================
INSERT INTO users (id, nama, email, password, role, nomor_induk, aktif) VALUES
('U-ADM01', 'Lukman', 'admin@campusfind.ac.id', 'admin1234', 'Admin', '-', TRUE),
('U-PET01', 'Bintang', 'bintang@campusfind.ac.id', 'petugas123', 'Petugas', 'NIP-001', TRUE),
('U-PET02', 'Jepin', 'jepin@campusfind.ac.id', 'petugas123', 'Petugas', 'NIP-002', TRUE),
('U-PET03', 'Fakhri', 'fakhri@campusfind.ac.id', 'petugas123', 'Petugas', 'NIP-003', TRUE),
('U-MHS01', 'Pitya', 'pitya@student.ac.id', 'mhs12345', 'Mahasiswa', 'NIM-101', TRUE),
('U-MHS02', 'Nadia', 'nadia@student.ac.id', 'mhs12345', 'Mahasiswa', 'NIM-102', TRUE)
ON DUPLICATE KEY UPDATE nama=VALUES(nama);

INSERT INTO items (id, jenis, nama, kategori, lokasi_kejadian, pelapor, status, lokasi_simpan, kode_qr, terkunci, merk, nomor_seri, bahan, ciri_fisik) VALUES
('T-101', 'TEMUAN', 'iPhone 13 Hitam', 'ELEKTRONIK', 'Gedung A Lt. 2', 'Budi (Mhs)', 'DILAPORKAN', '-', '-', FALSE, 'Apple', 'IMEI-8839201', NULL, NULL),
('H-102', 'HILANG', 'Dompet Kulit Coklat', 'NON_ELEKTRONIK', 'Kantin Pusat', 'Siti (Mhs)', 'DILAPORKAN', '-', '-', FALSE, NULL, NULL, 'Kulit Asli', 'Inisial S dan KTP a.n Siti'),
('T-055', 'TEMUAN', 'Tumbler Corkcicle Biru', 'NON_ELEKTRONIK', 'Perpustakaan Lt. 1', 'Jepin (Petugas)', 'DIVERIFIKASI', 'Ruang C - 101', 'QR-T-055', FALSE, NULL, NULL, 'Stainless', 'Stiker nama Andi di bawah botol'),
('H-012', 'HILANG', 'Flashdisk Sandisk 32GB', 'ELEKTRONIK', 'Lab Komputer 1', 'Rama (Mhs)', 'DIVERIFIKASI', '-', '-', FALSE, 'Sandisk', 'SN-FD9921', NULL, NULL),
('T-034', 'TEMUAN', 'USB Sandisk Merah Hitam', 'ELEKTRONIK', 'Lab Komputer 1', 'Agnes (Mhs)', 'DIVERIFIKASI', 'Ruang C - 106', 'QR-T-034', FALSE, 'Sandisk', 'SN-FD9921', NULL, NULL)
ON DUPLICATE KEY UPDATE nama=VALUES(nama);

INSERT INTO klaim (id_klaim, item_id, pengaju, bukti, status_klaim) VALUES
('K-201', 'T-055', 'Andi (Mhs)', 'Ada stiker nama Andi di bagian bawah botol', 'DIAJUKAN'),
('K-202', 'T-055', 'Rina (Mhs)', 'Warna biru tua polos', 'DIAJUKAN')
ON DUPLICATE KEY UPDATE pengaju=VALUES(pengaju);
