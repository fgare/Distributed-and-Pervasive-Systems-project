package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import SimulatorsPackage.PollutionSensor;
import ThermalPowerPlantPackage.Pollution.Window;
import ThermalPowerPlantPackage.gRPC.GrpcServerStarter;
import ThermalPowerPlantPackage.gRPC.RingClient;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;


public class ThermalPowerPlant extends PlantInfo {
    private final Window window;
    private final TreeSet<PlantInfo> otherPlants;
    private final RingClient ringClient;


    ThermalPowerPlant(Integer plantId, String clientAddress, Integer clientPort, String serverAddress, Integer serverPort) throws IdAlreadyExistsException, IOException, InterruptedException {
        super(plantId, clientAddress, clientPort);
        otherPlants = new TreeSet<>(
                new ThermalPlantPresenter(this, serverAddress, serverPort).publishPlant()
        ); // prova a registrare la nuova centrale sul server

        // avvia server Grpc
        ringClient = new GrpcServerStarter(this).startElectionServer();
        Thread.sleep(500);
        new GrpcServerStarter(this).startPresentationServer();
        new ThermalPlantPresenter(this, serverAddress, serverPort).presentToOtherPlants();

        this.window = new Window(this, 8, (float) 0.5);
        new PollutionSensor(window).start(); // avvia il simulatore di inquinamento
    }

    ThermalPowerPlant(Integer id, Integer port) throws IdAlreadyExistsException, IOException, InterruptedException {
        this(id, "localhost", port, "localhost", 8080);
    }

    public Set<PlantInfo> getOtherPlants() {
        synchronized (otherPlants) {
            return otherPlants;
        }
    }

    public boolean addColleaguePlant(PlantInfo otherPlant) {
        boolean outcome;
        synchronized (otherPlants) {
            outcome = otherPlants.add(otherPlant);
            if (outcome) {
                ringClient.onPlantAdded(otherPlant);
                System.out.println("Aggiunta centrale " + otherPlant);
                return true;
            }
        }
        return false;
    }

    /**
     * Ritorna la centrale successiva nell'anello
     * @return la centrale con id immediatamente successivo nell'insieme, null se ci sono solo io.
     */
    public PlantInfo getRingFollowingPlant() {
        synchronized (otherPlants) {
            PlantInfo next = otherPlants.higher(this);
            if (next == null) {
                // significa che non c'è un elemento con id superiore
                try {
                    next = otherPlants.first();
                } catch (NoSuchElementException e) {
                    return null;
                }
            }
            // se first restituisce me, significa che ci sono solo io nell'anello. Ritorno null
            if (next == this) return null;
            return next;
        }
    }

}
