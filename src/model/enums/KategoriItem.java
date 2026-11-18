package model.enums;

public enum KategoriItem {
    ELEKTRONIK("Elektronik"),
    NON_ELEKTRONIK("Non-Elektronik");

    private final String label;

    KategoriItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
