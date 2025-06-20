package ThermalPowerPlantPackage;

import io.grpc.ServerBuilder;

import java.io.IOException;

public class ElectionServer {

    public static void main(String[] args) {
        try {
            io.grpc.Server server = ServerBuilder.forPort(9000).addService(new ElectionServiceImpl()).build();
            server.start();
            System.out.println("GRPC Server started, listening on " + server.getPort());
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
