package ThermalPowerPlantPackage;

class OtherPlant {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;

    OtherPlant(Integer id, String ipAddress, Integer port) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
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

}
