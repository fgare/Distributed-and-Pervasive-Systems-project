package ThermalPowerPlantPackage;

public class PlantInfo implements Comparable<PlantInfo> {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;

    public PlantInfo(Integer id, String ipAddress, Integer port) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public PlantInfo(Integer id, Integer port) {
        this(id, "localhost", port);
    }

    public Integer getId() {
        return id;
    }

    public Integer getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public int compareTo(PlantInfo o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return id + " " + ipAddress + " " + port;
    }

}
