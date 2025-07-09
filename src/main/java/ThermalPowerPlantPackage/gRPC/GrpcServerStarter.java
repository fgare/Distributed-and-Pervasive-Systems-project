package ThermalPowerPlantPackage.gRPC;

import ThermalPowerPlantPackage.EnergyRequestReceiver;
import ThermalPowerPlantPackage.ThermalPowerPlant;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Avvia i 2 server gRPC:
 * 1. Server per la ricezione di riferimenti da altre centrali
 * 2. Server di gestione delle elezioni per le forniture di energia
 */
public class GrpcServerStarter {
    private final ThermalPowerPlant mainPlant;
    private ElectionServiceImpl electionService;

    public GrpcServerStarter(ThermalPowerPlant mainPlant) {
        this.mainPlant = mainPlant;
    }

    public void startPresentationServer() throws IOException, InterruptedException {
        Server introServer = ServerBuilder.forPort(mainPlant.getPort()).addService(new PresentationServiceImpl(mainPlant)).build();
        introServer.start();
        System.out.println("Presentation server started, listening on " + introServer.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (introServer != null) {
                    introServer.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }));
    }

    public void startElectionServer() throws IOException, InterruptedException {
        electionService = new ElectionServiceImpl(mainPlant);
        Server electionServer = ServerBuilder.forPort(mainPlant.getPort()+1).addService(electionService).build();
        electionServer.start();
        new Thread(new EnergyRequestReceiver(electionService)).start();
        System.out.println("Election server started, listening on " + electionServer.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (electionServer != null) {
                    electionServer.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }));

    }

}
