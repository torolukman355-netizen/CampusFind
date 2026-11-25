package com.campusfind.service;

import com.campusfind.exception.AksesDitolakException;
import com.campusfind.exception.NotFoundException;
import com.campusfind.exception.ValidationException;
import com.campusfind.model.Admin;
import com.campusfind.model.HakAkses;
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
 * Layanan autentikasi dan manajemen pengguna berbasis nomor identitas, email, dan role.
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

    public User login(String nomorIdentitas) {
        return login(nomorIdentitas, null, Role.dariNomorIdentitas(nomorIdentitas));
    }

    public void logout() {
        session.clear();
    }

    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

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

    public Petugas tambahPetugasOlehAdmin(String nama, String nip, String email, String password) {
        pastikanAksesAdmin();
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
     * Mengaktifkan atau menonaktifkan akun pengguna (US-022 & US-030).
     * Admin tidak diizinkan menonaktifkan akunnya sendiri.
     */
    public User ubahStatusAktifUser(String nomorIdentitasTarget, boolean aktif) {
        pastikanAksesAdmin();
        if (userRepository == null) {
            throw new ValidationException("Repositori pengguna tidak tersedia.");
        }
        if (nomorIdentitasTarget == null || nomorIdentitasTarget.isBlank()) {
            throw new ValidationException("Nomor identitas pengguna wajib dipilih.");
        }

        String targetId = nomorIdentitasTarget.trim();
        if (!aktif && session.isLoggedIn()
                && targetId.equalsIgnoreCase(session.getUser().getNomorIdentitas())) {
            throw new ValidationException("Admin tidak dapat menonaktifkan akunnya sendiri.");
        }

        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Pengguna dengan identitas '" + targetId + "' tidak ditemukan."));

        target.setAktif(aktif);
        userRepository.save(target);
        return target;
    }

    /**
     * Mengubah role pengguna yang terdaftar (US-022 & US-030).
     */
    public User ubahRoleUser(String nomorIdentitasTarget, Role roleBaru) {
        pastikanAksesAdmin();
        if (userRepository == null) {
            throw new ValidationException("Repositori pengguna tidak tersedia.");
        }
        if (nomorIdentitasTarget == null || nomorIdentitasTarget.isBlank()) {
            throw new ValidationException("Nomor identitas pengguna wajib dipilih.");
        }
        if (roleBaru == null) {
            throw new ValidationException("Role baru wajib dipilih.");
        }

        String targetId = nomorIdentitasTarget.trim();
        if (session.isLoggedIn()
                && targetId.equalsIgnoreCase(session.getUser().getNomorIdentitas())
                && roleBaru != Role.ADMIN) {
            throw new ValidationException("Admin tidak dapat menurunkan role akunnya sendiri.");
        }

        User lama = userRepository.findById(targetId)
                .orElseThrow(() -> new NotFoundException("Pengguna dengan identitas '" + targetId + "' tidak ditemukan."));

        if (lama.getRole() == roleBaru) {
            return lama;
        }

        User pengganti = buatUser(
                roleBaru,
                lama.getNama(),
                lama.getNomorIdentitas(),
                lama.getEmail(),
                lama.getPasswordHash(),
                lama.isAktif()
        );
        userRepository.save(pengganti);
        return pengganti;
    }

    private void pastikanAksesAdmin() {
        if (session.isLoggedIn() && !session.getUser().punyaHak(HakAkses.MANAJEMEN_PENGGUNA)) {
            throw new AksesDitolakException("Hanya Administrator yang berwenang mengelola pengguna.");
        }
    }

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