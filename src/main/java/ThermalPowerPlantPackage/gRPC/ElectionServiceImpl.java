package ThermalPowerPlantPackage.gRPC;

import ThermalPowerPlantPackage.ElectionRequestOuterClass;
import ThermalPowerPlantPackage.ElectionServiceGrpc;
import ThermalPowerPlantPackage.OtherPlant;
import ThermalPowerPlantPackage.ThermalPowerPlant;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class ElectionServiceImpl extends ElectionServiceGrpc.ElectionServiceImplBase {
    private final ThermalPowerPlant thisPlant;
    private OtherPlant successor; // informazioni sulla centrale successore, in ordine di ID
    private WorkingStatus workingStatus = WorkingStatus.FREE;
    private ManagedChannel channel; // canale di comunicazione con il successore
    private ElectionServiceGrpc.ElectionServiceStub stub;
    private StreamObserver<ElectionRequestOuterClass.ElectionRequest> responseObserver;


    ElectionServiceImpl(ThermalPowerPlant tp) {
        this.thisPlant = tp;
        this.responseObserver = new StreamObserver<ElectionRequestOuterClass.ElectionRequest>() {
            @Override
            public void onNext(ElectionRequestOuterClass.ElectionRequest value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    synchronized void changeWorkingStatus(WorkingStatus newStatus) {
        workingStatus = newStatus;
    }

    private void searchSuccessor() {
        TreeSet<OtherPlant> otherPlantsList = (TreeSet<OtherPlant>) thisPlant.getOtherPlants();
        // determina la centrale successiva nell'anello
        OtherPlant nextplant = otherPlantsList.higher(thisPlant.getPlantAsOtherPlant());
        // se questa centrale è l'ultima, il metodo higher() ritorna NULL. Bisogna quindi ripartire dalla prima centrale
        if (nextplant == null) nextplant = otherPlantsList.first();
        this.successor = nextplant;
    }

    private void openChannel() {
        channel = ManagedChannelBuilder
                .forTarget(successor.getIpAddress() + ":" + successor.getPort())
                .usePlaintext()
                .build();
        stub = ElectionServiceGrpc.newStub(channel);
    }

    void startElection() {
        TreeSet<OtherPlant> otherPlantsList = (TreeSet<OtherPlant>) thisPlant.getOtherPlants();
        // solo la centrale con id minore può avviare l'elezione
        if ( !thisPlant.getId().equals(otherPlantsList.first().getId()) ) return;

        float myOfferedPrice = (float) (0.1 + new Random().nextFloat()*0.8);

        ElectionRequestOuterClass.ElectionRequest request = ElectionRequestOuterClass.ElectionRequest
                .newBuilder()
                .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                .setStarterId(thisPlant.getId())
                .setWinningId(thisPlant.getId())
                .setPrice(myOfferedPrice)
                .build();

        handleElection(request, null);
    }

    @Override
    public synchronized void handleElection(ElectionRequestOuterClass.ElectionRequest request, StreamObserver<ElectionRequestOuterClass.ElectionRequest> responseObserver) {
        ElectionRequestOuterClass.ElectionRequest newRequest = null;

        // se sto già fornendo energia, inoltro la richiesta e ignoro
        if (workingStatus == WorkingStatus.PROVIDING) {
            forward(request);
            return;
        }

        float myOfferedPrice = (float) (0.1 + new Random().nextFloat()*0.8);

        if (request.getStatus() == ElectionRequestOuterClass.ElectionRequest.Status.ELECTION) {
            // se il mio prezzo è più alto, inoltro
            if (request.getPrice() < myOfferedPrice) {
                workingStatus = WorkingStatus.PARTICIPANT;
                forward(request);
                return;
            } else
            // se il mio prezzo è migliore e sono NON PARTECIPANTE, sono io il vincitore temporaneo
            if (request.getPrice() > myOfferedPrice && workingStatus == WorkingStatus.FREE) {
                workingStatus = WorkingStatus.PARTICIPANT;
                newRequest = ElectionRequestOuterClass.ElectionRequest
                        .newBuilder()
                        .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                        .setStarterId(request.getStarterId())
                        .setWinningId(thisPlant.getId())
                        .setPrice(myOfferedPrice)
                        .build();
                forward(newRequest);
                return;
            } else
            // se il mio prezzo è migliore e sono già PARTECIPANTE, ignoro
            if (request.getPrice() > myOfferedPrice && workingStatus == WorkingStatus.PARTICIPANT) {
                return;
            }

            // se il prezzo della richiesta non è minore e non è maggiore del mio prezzo, è per forza uguale
            // confronto gli ID delle centrali, il più alto vince
            if (request.getWinningId() > thisPlant.getId()) {
                forward(request);
                return;
            }

            // il mio plant ID è maggiore, ho vinto io. Comunico la vittoria
            request = ElectionRequestOuterClass.ElectionRequest
                    .newBuilder()
                    .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                    .setStarterId(request.getStarterId())
                    .setWinningId(thisPlant.getId())
                    .setPrice(myOfferedPrice)
                    .build();
            workingStatus = WorkingStatus.FREE;
            forward(request);
            return;
        }

        // Status ELECTED e l'ID vincitore coincide con il mio
        if (request.getWinningId() == thisPlant.getId()) {
            workingStatus = WorkingStatus.PROVIDING;
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> changeWorkingStatus(WorkingStatus.FREE), 10, TimeUnit.SECONDS);
            scheduler.schedule(() -> scheduler.shutdown(), 12, TimeUnit.SECONDS);
            return;
        }

        // Status ELECTED e ID vincitore non è il mio
        forward(request);

    }

    private void forward(ElectionRequestOuterClass.ElectionRequest request) {
        stub.handleElection(request, responseObserver);
        responseObserver.onCompleted();
    }

    private enum WorkingStatus {
        FREE, PARTICIPANT, PROVIDING
    }
    
}
