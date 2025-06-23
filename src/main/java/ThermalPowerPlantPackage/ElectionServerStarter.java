package ThermalPowerPlantPackage;

import io.grpc.ServerBuilder;

import java.io.IOException;

class ElectionServerStarter {

    void run() {
        try {
            io.grpc.Server server = ServerBuilder.forPort(9000).addService(new ElectionServiceImpl(null)).build();
            server.start();
            System.out.println("GRPC Server started, listening on " + server.getPort());
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ElectionServerStarter().run();
    }

}
