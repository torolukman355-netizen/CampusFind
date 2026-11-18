package repository;

import model.Admin;
import model.Mahasiswa;
import model.Petugas;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * US-022: Repository Pengguna untuk login dan manajemen akun oleh Admin.
 */
public class UserRepository {
    private static UserRepository instance;
    private final List<User> users = new ArrayList<>();

    private UserRepository() {
        seedDummyData();
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public synchronized List<User> findAll() {
        return new ArrayList<>(users);
    }

    public synchronized Optional<User> findById(String id) {
        return users.stream().filter(u -> u.getId().equalsIgnoreCase(id.trim())).findFirst();
    }

    public synchronized Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equalsIgnoreCase(email.trim())).findFirst();
    }

    public synchronized void save(User user) {
        users.removeIf(u -> u.getId().equalsIgnoreCase(user.getId()));
        users.add(user);
    }

    private void seedDummyData() {
        users.add(new Admin("U-ADM01", "Lukman", "admin@campusfind.ac.id", "admin1234"));
        users.add(new Petugas("U-PET01", "Bintang", "bintang@campusfind.ac.id", "petugas123", "NIP-001"));
        users.add(new Petugas("U-PET02", "Jepin", "jepin@campusfind.ac.id", "petugas123", "NIP-002"));
        users.add(new Petugas("U-PET03", "Fakhri", "fakhri@campusfind.ac.id", "petugas123", "NIP-003"));
        users.add(new Mahasiswa("U-MHS01", "Pitya", "pitya@student.ac.id", "mhs12345", "NIM-101"));
        users.add(new Mahasiswa("U-MHS02", "Nadia", "nadia@student.ac.id", "mhs12345", "NIM-102"));
    }
}
