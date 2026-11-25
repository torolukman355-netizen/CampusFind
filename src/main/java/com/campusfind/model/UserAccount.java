package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entitas akun pengguna persisten untuk autentikasi dan manajemen akun (US-022).
 * Dirancang siap dipetakan ke tabel SQL `users` di masa depan tanpa mengubah kontrak domain.
 */
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum RoleType {
        MAHASISWA("Mahasiswa"),
        SECURITY("Security (Verifikator)"),
        CLEANING_SERVICE("Cleaning Service"),
        ADMIN("Admin");

        private final String label;

        RoleType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final String id;
    private String nama;
    private String nomorIdentitas;
    private String email;
    private String password;
    private RoleType role;
    private boolean aktif;
    private final LocalDateTime dibuatPada;

    public UserAccount(String id, String nama, String nomorIdentitas, String email,
                       String password, RoleType role, boolean aktif, LocalDateTime dibuatPada) {
        this.id = Objects.requireNonNull(id, "ID akun tidak boleh null");
        this.nama = Objects.requireNonNull(nama, "Nama tidak boleh null");
        this.nomorIdentitas = Objects.requireNonNull(nomorIdentitas, "Nomor identitas tidak boleh null");
        this.email = Objects.requireNonNull(email, "Email tidak boleh null");
        this.password = Objects.requireNonNull(password, "Password tidak boleh null");
        this.role = Objects.requireNonNull(role, "Role tidak boleh null");
        this.aktif = aktif;
        this.dibuatPada = dibuatPada != null ? dibuatPada : LocalDateTime.now();
    }

    /**
     * Mengonversi akun menjadi objek domain polimorfik sesuai role (User hierarchy).
     */
    public User toDomainUser() {
        return switch (role) {
            case MAHASISWA -> new Mahasiswa(nama, nomorIdentitas);
            case SECURITY -> new Security(nama, nomorIdentitas);
            case CLEANING_SERVICE -> new CleaningService(nama, nomorIdentitas);
            case ADMIN -> new Admin(nama, nomorIdentitas);
        };
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getNomorIdentitas() { return nomorIdentitas; }
    public void setNomorIdentitas(String nomorIdentitas) { this.nomorIdentitas = nomorIdentitas; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public RoleType getRole() { return role; }
    public void setRole(RoleType role) { this.role = role; }
    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }
    public LocalDateTime getDibuatPada() { return dibuatPada; }
}
