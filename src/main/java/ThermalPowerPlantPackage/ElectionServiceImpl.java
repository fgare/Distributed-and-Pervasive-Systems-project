package ThermalPowerPlantPackage;

import io.grpc.stub.StreamObserver;

public class ElectionServiceImpl extends ElectionServiceGrpc.ElectionServiceImplBase {
    private int plantID;


    public ElectionServiceImpl(int myId) {

    }

    @Override
    public void handleElection(RequestOuterClass.Request request, StreamObserver<RequestOuterClass.Request> responseObserver) {
        if (this.plantID > request.getId()) {
            System.out.println("Il mio ID (" + this.plantID + ") è maggiore dell'ID ricevuto (" + request.getId() + ")");

            // Costruisci una nuova richiesta con il proprio ID e stato ELECTION
            RequestOuterClass.Request newRequest = RequestOuterClass.Request.newBuilder()
                    .setId(this.plantID)
                    .setOfferedPrice(request.getOfferedPrice()) // Manteniamo il prezzo offerto dalla richiesta originale
                    .setStatus(RequestOuterClass.Request.Status.ELECTION)
                    .build();

            // Invia la nuova richiesta come risposta
            responseObserver.onNext(newRequest);
        } else {
            System.out.println("Il mio ID (" + this.plantID + ") non è maggiore dell'ID ricevuto (" + request.getId() + ")");
        }

        responseObserver.onCompleted();
    }

    private enum
}
