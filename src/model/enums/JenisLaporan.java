package model.enums;

public enum JenisLaporan {
    HILANG("Barang Hilang"),
    TEMUAN("Barang Temuan");

    private final String label;

    JenisLaporan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
