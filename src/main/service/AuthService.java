package com.campusfind.service;

import com.campusfind.exception.AksesDitolakException;
import com.campusfind.model.Admin;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.model.Role;
import com.campusfind.model.User;
import com.campusfind.repository.UserRepository;
import com.campusfind.validation.IdentitasValidator;
import com.campusfind.validation.ValidationResult;

import java.util.Objects;
import java.util.Optional;

/**
 * Layanan autentikasi berbasis nomor identitas, email, dan role.
 */
public class AuthService {

    private final AppSession session;
    private final UserRepository userRepository;

    public AuthService(AppSession session) {
        this(session, null);
    }

    public AuthService(AppSession session, UserRepository userRepository) {
        this.session = Objects.requireNonNull(session, "session tidak boleh null");
        this.userRepository = userRepository;
    }

    /**
     * Login dengan role eksplisit.
     *
     * @param nomorIdentitas NIM / NIP / nama pengguna admin
     * @param nama           nama tampilan (opsional, akan dibuat otomatis bila kosong)
     * @param role           role yang dipilih
     * @return pengguna yang berhasil login
     * @throws AksesDitolakException jika role tidak dipilih atau identitas tidak valid
     */
    public User login(String nomorIdentitas, String nama, Role role) {
        if (role == null) {
            throw new AksesDitolakException("Role pengguna wajib dipilih.");
        }

        ValidationResult hasil = IdentitasValidator.validate(nomorIdentitas, role);
        if (!hasil.isValid()) {
            throw new AksesDitolakException(hasil.getFirstError());
        }

        String nomor = nomorIdentitas.trim();

        if (userRepository != null) {
            Optional<User> existing = userRepository.findById(nomor);
            if (existing.isPresent()) {
                User user = existing.get();
                if (!user.isAktif()) {
                    throw new AksesDitolakException("Akun Anda telah dinonaktifkan. Silakan hubungi admin.");
                }
                session.login(user);
                return user;
            }
        }

        String namaTampilan = (nama == null || nama.isBlank())
                ? role.getLabel() + " " + nomor
                : nama.trim();

        User user = buatUser(role, namaTampilan, nomor);
        if (userRepository != null) {
            userRepository.save(user);
        }
        session.login(user);
        return user;
    }

    /**
     * Login berbasis alamat email kampus.
     *
     * @param email    alamat email pengguna
     * @param password kata sandi (opsional untuk demo)
     * @return pengguna yang berhasil masuk
     */
    public User loginWithEmail(String email, String password) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new AksesDitolakException("Format email tidak valid.");
        }

        String trimmed = email.trim();
        if (userRepository != null) {
            Optional<User> existing = userRepository.findByEmail(trimmed);
            if (existing.isPresent()) {
                User user = existing.get();
                if (!user.isAktif()) {
                    throw new AksesDitolakException("Akun Anda telah dinonaktifkan. Silakan hubungi admin.");
                }
                session.login(user);
                return user;
            }
        }

        // Tentukan role berdasarkan pola email bila pengguna belum ada di repositori
        String lower = trimmed.toLowerCase();
        Role role;
        String id;
        String nama;

        if (lower.contains("admin") || lower.contains("hendra")) {
            role = Role.ADMIN;
            id = "admin";
            nama = "Hendra Admin";
        } else if (lower.contains("petugas") || lower.contains("rina")) {
            role = Role.PETUGAS;
            id = "198501012010122001";
            nama = "Rina Wulandari";
        } else {
            role = Role.MAHASISWA;
            id = "123456789";
            nama = "Budi Santoso";
            int atIdx = lower.indexOf('@');
            if (atIdx > 0 && !lower.contains("budi")) {
                nama = lower.substring(0, 1).toUpperCase() + lower.substring(1, atIdx);
            }
        }

        User user = buatUser(role, nama, id, trimmed, password, true);
        if (userRepository != null) {
            userRepository.save(user);
        }
        session.login(user);
        return user;
    }

    /**
     * Login tanpa menyebut role: role ditebak dari panjang nomor identitas.
     */
    public User login(String nomorIdentitas) {
        return login(nomorIdentitas, null, Role.dariNomorIdentitas(nomorIdentitas));
    }

    /**
     * Mengakhiri sesi aktif.
     */
    public void logout() {
        session.clear();
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    /**
     * Mendaftar akun baru khusus untuk Mahasiswa.
     * Hanya Mahasiswa yang dapat mendaftar mandiri secara publik.
     * Petugas dan Admin mendapatkan akses langsung dari Administrator kampus.
     *
     * @param nama     nama lengkap mahasiswa
     * @param nim      NIM 9 digit angka
     * @param email    alamat email mahasiswa (@student.kampus.ac atau sejenis)
     * @param password kata sandi akun (minimal 6 karakter)
     * @return akun Mahasiswa yang berhasil terdaftar dan masuk ke sesi
     * @throws AksesDitolakException jika data tidak valid atau sudah terdaftar
     */
    public Mahasiswa daftarMahasiswa(String nama, String nim, String email, String password) {
        if (nama == null || nama.trim().isBlank()) {
            throw new AksesDitolakException("Nama lengkap wajib diisi.");
        }
        if (nim == null || nim.trim().isBlank()) {
            throw new AksesDitolakException("NIM wajib diisi.");
        }
        ValidationResult hasilNim = IdentitasValidator.validate(nim, Role.MAHASISWA);
        if (!hasilNim.isValid()) {
            throw new AksesDitolakException(hasilNim.getFirstError());
        }

        if (email == null || email.trim().isBlank() || !email.contains("@") || !email.contains(".")) {
            throw new AksesDitolakException("Format email tidak valid.");
        }

        if (password == null || password.length() < 6) {
            throw new AksesDitolakException("Kata sandi minimal 6 karakter.");
        }

        String nimClean = nim.trim();
        String emailClean = email.trim().toLowerCase();
        String namaClean = nama.trim();

        if (userRepository != null) {
            if (userRepository.existsByNomorIdentitas(nimClean) || userRepository.findById(nimClean).isPresent()) {
                throw new AksesDitolakException("NIM " + nimClean + " sudah terdaftar.");
            }
            if (userRepository.existsByEmail(emailClean) || userRepository.findByEmail(emailClean).isPresent()) {
                throw new AksesDitolakException("Email " + emailClean + " sudah terdaftar.");
            }
        }

        Mahasiswa mhs = new Mahasiswa(namaClean, nimClean, emailClean, password, true);
        if (userRepository != null) {
            userRepository.save(mhs);
        }
        session.login(mhs);
        return mhs;
    }

    /**
     * Menambahkan akun Petugas baru oleh Administrator.
     * Petugas tidak dapat mendaftar mandiri, akses diberikan oleh Admin.
     *
     * @param nama     nama lengkap petugas
     * @param nip      NIP 18 digit angka
     * @param email    email dinas petugas
     * @param password kata sandi awal (minimal 6 karakter)
     * @return akun Petugas yang berhasil didaftarkan
     * @throws AksesDitolakException jika identitas tidak valid atau bukan admin
     */
    public Petugas tambahPetugasOlehAdmin(String nama, String nip, String email, String password) {
        if (session.isLoggedIn() && session.getUser().getRole() != Role.ADMIN) {
            throw new AksesDitolakException("Hanya Administrator yang berwenang menambahkan akun Petugas.");
        }
        if (nama == null || nama.trim().isBlank()) {
            throw new AksesDitolakException("Nama petugas wajib diisi.");
        }
        if (nip == null || nip.trim().isBlank()) {
            throw new AksesDitolakException("NIP petugas wajib diisi.");
        }
        ValidationResult hasilNip = IdentitasValidator.validate(nip, Role.PETUGAS);
        if (!hasilNip.isValid()) {
            throw new AksesDitolakException(hasilNip.getFirstError());
        }
        if (email == null || email.trim().isBlank() || !email.contains("@") || !email.contains(".")) {
            throw new AksesDitolakException("Format email tidak valid.");
        }
        if (password == null || password.length() < 6) {
            throw new AksesDitolakException("Kata sandi minimal 6 karakter.");
        }

        String nipClean = nip.trim();
        String emailClean = email.trim().toLowerCase();
        String namaClean = nama.trim();

        if (userRepository != null) {
            if (userRepository.existsByNomorIdentitas(nipClean) || userRepository.findById(nipClean).isPresent()) {
                throw new AksesDitolakException("NIP " + nipClean + " sudah terdaftar.");
            }
            if (userRepository.existsByEmail(emailClean) || userRepository.findByEmail(emailClean).isPresent()) {
                throw new AksesDitolakException("Email " + emailClean + " sudah terdaftar.");
            }
        }

        Petugas petugas = new Petugas(namaClean, nipClean, emailClean, password, true);
        if (userRepository != null) {
            userRepository.save(petugas);
        }
        return petugas;
    }

    /**
     * @return pengguna yang sedang aktif, bila ada
     */
    public Optional<User> penggunaAktif() {
        return Optional.ofNullable(session.getUser());
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    private User buatUser(Role role, String nama, String nomor) {
        return switch (role) {
            case MAHASISWA -> new Mahasiswa(nama, nomor);
            case PETUGAS -> new Petugas(nama, nomor);
            case ADMIN -> new Admin(nama, nomor);
        };
    }

    private User buatUser(Role role, String nama, String nomor, String email, String password, boolean aktif) {
        return switch (role) {
            case MAHASISWA -> new Mahasiswa(nama, nomor, email, password, aktif);
            case PETUGAS -> new Petugas(nama, nomor, email, password, aktif);
            case ADMIN -> new Admin(nama, nomor, email, password, aktif);
        };
    }
}
