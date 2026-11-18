package model.enums;

public enum StatusKlaim {
    DIAJUKAN("Menunggu Pemeriksaan"),
    DISETUJUI("Klaim Diterima"),
    DITOLAK("Klaim Ditolak");

    private final String label;

    StatusKlaim(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
