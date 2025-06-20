package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import SimulatorsPackage.PollutionSensor;


class ThermalPowerPlant {
    private final Integer id;
    private final String ipAddress;
    private final Integer port;
    private final Window window;

    ThermalPowerPlant(Integer id, String ipAddress, Integer port, String serverAddress, Integer serverPort) throws IdAlreadyExistsException {
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        new ThermalInsertionHandler(this, serverAddress, serverPort).publishPlant(); // prova a registrare la nuova centrale
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
