package AdministrationServer;


public class DataPoint implements Comparable<DataPoint> {
    private final Long timestamp; // ms dalla mezzanotte del giorno attuale
    private final Integer value; // grammi di CO2

    public DataPoint(Long timestamp, Integer value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public DataPoint(Long timestamp) {
        this.timestamp = timestamp;
        this.value = null;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public int compareTo(DataPoint other) {
        if (this.timestamp < other.timestamp) return -1;
        if (this.timestamp > other.timestamp) return 1;
        else return 0;
    }

}
