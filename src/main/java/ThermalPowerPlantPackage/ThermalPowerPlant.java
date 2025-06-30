package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import SimulatorsPackage.PollutionSensor;
import ThermalPowerPlantPackage.Pollution.Window;
import ThermalPowerPlantPackage.gRPC.GrpcServerStarter;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;


public class ThermalPowerPlant extends PlantInfo {
    private final Window window;
    private final TreeSet<PlantInfo> otherPlants;


    public ThermalPowerPlant(Integer plantId, String clientAddress, Integer clientPort, String serverAddress, Integer serverPort) throws IdAlreadyExistsException, IOException, InterruptedException {
        super(plantId, clientAddress, clientPort);
        otherPlants = new TreeSet<>(new ThermalPlantPresenter(this, serverAddress, serverPort).publishPlant()); // prova a registrare la nuova centrale sul server

        // avvia server Grpc
        new GrpcServerStarter(this).startPresentationServer();
        new GrpcServerStarter(this).startElectionServer();
        new ThermalPlantPresenter(this, serverAddress, serverPort).presentToOtherPlants();

        this.window = new Window(this, 8, (float) 0.5);
        (new PollutionSensor(window)).start(); // avvia il simulatore di inquinamento
    }

    public ThermalPowerPlant(Integer id, Integer port) throws IdAlreadyExistsException, IOException, InterruptedException {
        this(id, "localhost", port, "localhost", 8080);
    }

    public synchronized Set<PlantInfo> getOtherPlants() {
        return otherPlants;
    }

    public synchronized boolean addColleaguePlant(PlantInfo otherPlant) {
        System.out.println("Aggiunta centrale " + otherPlant);
        return otherPlants.add(otherPlant);
    }

}
