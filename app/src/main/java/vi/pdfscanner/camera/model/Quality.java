package vi.pdfscanner.camera.model;

public enum Quality {
    HIGH(0, "High"), MEDIUM(1, "Medium"), LOW(2, "Low");

    private int id;

    private String name;

    Quality(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public static Quality getQualityById(int id) {
        for (Quality mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

}
