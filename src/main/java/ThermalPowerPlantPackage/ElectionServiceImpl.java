package ThermalPowerPlantPackage;

import io.grpc.stub.StreamObserver;

class ElectionServiceImpl extends ElectionServiceGrpc.ElectionServiceImplBase {
    private ThermalPowerPlant plant;


    ElectionServiceImpl(ThermalPowerPlant tp) {

    }

    @Override
    public void handleElection(RequestOuterClass.Request request, StreamObserver<RequestOuterClass.Request> responseObserver) {
        if (this.plant.getId() > request.getId()) {
            System.out.println("Il mio ID (" + this.plant.getId() + ") è maggiore dell'ID ricevuto (" + request.getId() + ")");

            // Costruisci una nuova richiesta con il proprio ID e stato ELECTION
            RequestOuterClass.Request newRequest = RequestOuterClass.Request.newBuilder()
                    .setId(this.plant.getId())
                    .setOfferedPrice(request.getOfferedPrice()) // Manteniamo il prezzo offerto dalla richiesta originale
                    .setStatus(RequestOuterClass.Request.Status.ELECTION)
                    .build();

            // Invia la nuova richiesta come risposta
            responseObserver.onNext(newRequest);
        } else {
            System.out.println("Il mio ID (" + this.plant.getId() + ") non è maggiore dell'ID ricevuto (" + request.getId() + ")");
        }

        responseObserver.onCompleted();
    }
    
}
