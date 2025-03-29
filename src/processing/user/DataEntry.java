package processing.user;

public class DataEntry {
    private String category;
    private String payload;

    public DataEntry(String category, String payload) {
        this.category = category;
        this.payload = payload;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getCategory() {
        return category;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return category + " " + payload;
    }
}
