package ThermalPowerPlantPackage;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

class GrpcServerStarter {
    private final ThermalPowerPlant mainPlant;

    GrpcServerStarter(ThermalPowerPlant mainPlant) {
        this.mainPlant = mainPlant;
    }

    void startPresentationServer() throws IOException, InterruptedException {
        Server introServer = ServerBuilder.forPort(8000).addService(new PresentationServiceImpl(mainPlant)).build();
        introServer.start();
        System.out.println("Presentation server started, listening on " + introServer.getPort());
        introServer.awaitTermination();
    }

    //TODO metodo per avviare il server per l'elezione
}
