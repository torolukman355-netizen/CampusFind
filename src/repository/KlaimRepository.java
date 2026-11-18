package repository;

import model.Klaim;
import model.enums.StatusKlaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * US-020: Repository data klaim mahasiswa untuk diperiksa petugas.
 */
public class KlaimRepository {
    private static KlaimRepository instance;
    private final List<Klaim> klaims = new ArrayList<>();

    private KlaimRepository() {
        seedDummyData();
    }

    public static synchronized KlaimRepository getInstance() {
        if (instance == null) {
            instance = new KlaimRepository();
        }
        return instance;
    }

    public synchronized List<Klaim> findAll() {
        return new ArrayList<>(klaims);
    }

    public synchronized Optional<Klaim> findById(String idKlaim) {
        return klaims.stream()
                .filter(k -> k.getIdKlaim().equalsIgnoreCase(idKlaim.trim()))
                .findFirst();
    }

    public synchronized List<Klaim> findByStatus(StatusKlaim status) {
        return klaims.stream()
                .filter(k -> k.getStatusKlaim() == status)
                .collect(Collectors.toList());
    }

    public synchronized List<Klaim> findByBarang(String idBarang) {
        return klaims.stream()
                .filter(k -> k.getIdBarang().equalsIgnoreCase(idBarang.trim()))
                .collect(Collectors.toList());
    }

    public synchronized void save(Klaim klaim) {
        int idx = -1;
        for (int i = 0; i < klaims.size(); i++) {
            if (klaims.get(i).getIdKlaim().equalsIgnoreCase(klaim.getIdKlaim())) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            klaims.set(idx, klaim);
        } else {
            klaims.add(klaim);
        }
    }

    public synchronized int countByStatus(StatusKlaim status) {
        return (int) klaims.stream().filter(k -> k.getStatusKlaim() == status).count();
    }

    private void seedDummyData() {
        klaims.add(new Klaim("K-201", "T-055", "Andi (Mhs)", "Ada stiker nama Andi di bagian bawah botol"));
        klaims.add(new Klaim("K-202", "T-055", "Rina (Mhs)", "Warna biru tua polos"));
    }
}
