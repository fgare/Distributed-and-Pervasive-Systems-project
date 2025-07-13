package ThermalPowerPlantPackage.gRPC;

import ThermalPowerPlantPackage.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class RingClient implements PlantsSetObserver {
    private ThermalPowerPlant thisPlant;
    private PlantInfo successor;
    private ElectionServiceGrpc.ElectionServiceStub stub;

    RingClient(ThermalPowerPlant thisPlant) {
        this.thisPlant = thisPlant;
        this.successor = thisPlant.getRingFollowingPlant();
        // se ci sono solo io nella rete di centrali, non ho un successore e non posso generare un canale
        if (successor == null) return;
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(successor.getIpAddress(), successor.getPort())
                .usePlaintext()
                .build();
        stub = ElectionServiceGrpc.newStub(channel);
    }

    @Override
    public void onPlantAdded (PlantInfo newPlant) {
        PlantInfo currentSuccessor = successor;
        // Nota: non serve synchronized perchè è già presente nel metodo che chiama questo metodo
        PlantInfo newSuccessor = thisPlant.getRingFollowingPlant();
        if (currentSuccessor == newSuccessor) return;

        // se arrivo qui significa che il successore è cambiato
        successor = newPlant;
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(successor.getIpAddress(), successor.getPort())
                .usePlaintext()
                .build();
        stub = ElectionServiceGrpc.newStub(channel);
    }

    void forwardMessage(ElectionRequestOuterClass.ElectionRequest request) {
        stub.handleElection(request, new StreamObserver<ElectionRequestOuterClass.ElectionResponse>() {
            @Override
            public void onNext(ElectionRequestOuterClass.ElectionResponse response) {
                System.err.println("[GRPC Client] Risposta asincrona dal successore: " + response.getSuccess());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("[GRPC Client] Errore durante invio asincrono: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.err.println("[GRPC Client] Invio asincrono completato");
            }
        });
    }
}
