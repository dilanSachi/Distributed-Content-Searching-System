import java.io.Serializable;

public class File implements Serializable {

    private String filename;
    private String value;

    public File (String filename, String value) {
        this.filename = filename;
        this.value = value;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
