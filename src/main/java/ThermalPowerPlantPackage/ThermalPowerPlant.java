package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import SimulatorsPackage.PollutionSensor;

import java.util.ArrayList;


class ThermalPowerPlant {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;
    private final Window window;
    private final ArrayList<OtherPlant> otherPlants;

    ThermalPowerPlant(Integer plantId, String clientAddress, Integer clientPort, String serverAddress, Integer serverPort) throws IdAlreadyExistsException {
        this.id = plantId;
        this.ipAddress = clientAddress;
        this.port = clientPort;
        otherPlants = new ArrayList<>(new ThermalPlantPresenter(this, serverAddress, serverPort).publishPlant()); // prova a registrare la nuova centrale
        this.window = new Window(this, 8, (float) 0.5);
        (new PollutionSensor(window)).start(); // avvia il simulatore di inquinamento
    }

    ThermalPowerPlant(Integer id, Integer port) throws IdAlreadyExistsException {
        this(id, "localhost", port, "localhost", 8080);
    }

    synchronized Integer getPort() {
        return port;
    }

    synchronized Integer getId() {
        return id;
    }

    synchronized String getIpAddress() {
        return ipAddress;
    }

}
