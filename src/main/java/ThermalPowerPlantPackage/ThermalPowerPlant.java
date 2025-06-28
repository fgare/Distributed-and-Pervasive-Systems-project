package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import SimulatorsPackage.PollutionSensor;
import ThermalPowerPlantPackage.Pollution.Window;

import java.util.Set;
import java.util.TreeSet;


public class ThermalPowerPlant implements Comparable<ThermalPowerPlant> {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;
    private final Window window;
    private final TreeSet<OtherPlant> otherPlants;

    public ThermalPowerPlant(Integer plantId, String clientAddress, Integer clientPort, String serverAddress, Integer serverPort) throws IdAlreadyExistsException {
        this.id = plantId;
        this.ipAddress = clientAddress;
        this.port = clientPort;
        otherPlants = new TreeSet<>(new ThermalPlantPresenter(this, serverAddress, serverPort).publishPlant()); // prova a registrare la nuova centrale sul server
        //TODO segnalare ad altre centrali
        this.window = new Window(this, 8, (float) 0.5);
        (new PollutionSensor(window)).start(); // avvia il simulatore di inquinamento
    }

    public ThermalPowerPlant(Integer id, Integer port) throws IdAlreadyExistsException {
        this(id, "localhost", port, "localhost", 8080);
    }

    public synchronized Integer getPort() {
        return port;
    }

    public synchronized Integer getId() {
        return id;
    }

    public synchronized String getIpAddress() {
        return ipAddress;
    }

    public synchronized Set<OtherPlant> getOtherPlants() {
        return otherPlants;
    }

    public synchronized boolean addOtherPlant(OtherPlant otherPlant) {
        return otherPlants.add(otherPlant);
    }

    public synchronized OtherPlant getPlantAsOtherPlant() {
        return new OtherPlant(id, ipAddress, port);
    }

    @Override
    public int compareTo(ThermalPowerPlant o) {
        return this.id.compareTo(o.id);
    }
}
