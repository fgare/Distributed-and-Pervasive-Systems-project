package ThermalPowerPlantPackage.gRPC;

import ThermalPowerPlantPackage.ThermalPowerPlant;
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

    void startElectionServer() throws IOException, InterruptedException {
        Server electionServer = ServerBuilder.forPort(9000).addService(new ElectionServiceImpl(mainPlant)).build();
        electionServer.start();
        System.out.println("Election server started, listening on " + electionServer.getPort());
        electionServer.awaitTermination();
    }

}
