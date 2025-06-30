package ThermalPowerPlantPackage.gRPC;

import ThermalPowerPlantPackage.ElectionRequestOuterClass;
import ThermalPowerPlantPackage.ElectionServiceGrpc;
import ThermalPowerPlantPackage.PlantInfo;
import ThermalPowerPlantPackage.ThermalPowerPlant;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ElectionServiceImpl extends ElectionServiceGrpc.ElectionServiceImplBase {
    private final ThermalPowerPlant thisPlant;
    private PlantInfo successor; // informazioni sulla centrale successore, in ordine di ID
    private WorkingStatus workingStatus = WorkingStatus.FREE;
    private ManagedChannel channel; // canale di comunicazione con il successore
    private ElectionServiceGrpc.ElectionServiceStub stub;
    private float myprice;


    ElectionServiceImpl(ThermalPowerPlant tp) {
        this.thisPlant = tp;
    }

    synchronized void changeWorkingStatus(WorkingStatus newStatus) {
        workingStatus = newStatus;
    }

    private void updateSuccessor() {
        TreeSet<PlantInfo> otherPlantsList = (TreeSet<PlantInfo>) thisPlant.getOtherPlants();
        if (otherPlantsList == null || otherPlantsList.isEmpty()) {
            this.successor = null;
            return;
        }

        // determina la centrale successiva nell'anello
        PlantInfo nextplant = otherPlantsList.higher(thisPlant);
        // se questa centrale è l'ultima, il metodo higher() ritorna NULL. Bisogna quindi ripartire dalla prima centrale
        if (nextplant == null) nextplant = otherPlantsList.first();
        this.successor = nextplant;
    }

    private void openChannel() {
        if (successor == null) return;

        // se il canale è già aperto non fa nulla
        //if (channel != null && !channel.isShutdown() && !channel.isTerminated()) return;

        channel = ManagedChannelBuilder
                .forTarget(successor.getIpAddress() + ":" + (successor.getPort()+1))
                .usePlaintext()
                .build();
        stub = ElectionServiceGrpc.newStub(channel);
    }

    public void startElection(int energykWh) {
        System.err.println("ELEZIONE INIZIATA CENTRALE "+ thisPlant.getId());
        myprice = (float) (0.1 + new Random().nextFloat()*0.8);

        ElectionRequestOuterClass.ElectionRequest request = ElectionRequestOuterClass.ElectionRequest
                    .newBuilder()
                    .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                    .setStarterId(thisPlant.getId())
                    .setWinningId(thisPlant.getId())
                    .setPrice(myprice)
                    .setQuantity(energykWh)
                    .build();

        forwardToNext(request, newResponseObserver());
    }

    private StreamObserver<ElectionRequestOuterClass.ElectionResponse> newResponseObserver() {
        return new StreamObserver<ElectionRequestOuterClass.ElectionResponse>() {
            @Override
            public void onNext(ElectionRequestOuterClass.ElectionResponse value) {
                System.err.println("Comunicazione riuscita");
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    @Override
    public synchronized void handleElection(ElectionRequestOuterClass.ElectionRequest incomingRequest, StreamObserver<ElectionRequestOuterClass.ElectionResponse> responseObserver) {
        ElectionRequestOuterClass.ElectionRequest newRequest = null;

        // se sto fornendo energia non partecipo all'elezione
        if (workingStatus == WorkingStatus.PROVIDING) {
            forwardToNext(incomingRequest, newResponseObserver());
            responseObserver.onNext(ElectionRequestOuterClass.ElectionResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
            return;
        }

        switch (incomingRequest.getStatus()) {
            case ELECTION:
                boolean iWin = isBetter(incomingRequest.getPrice(), incomingRequest.getWinningId());

                // se il mio prezzo è più alto, inoltro
                if (incomingRequest.getPrice() < myprice) {
                    workingStatus = WorkingStatus.PARTICIPANT;
                    forwardToNext(incomingRequest, responseObserver);
                    break;
                } else
                    // se il mio prezzo è migliore e sono NON PARTECIPANTE, sono io il vincitore temporaneo
                    if (incomingRequest.getPrice() > myprice && workingStatus == WorkingStatus.FREE) {
                        workingStatus = WorkingStatus.PARTICIPANT;
                        newRequest = ElectionRequestOuterClass.ElectionRequest
                                .newBuilder()
                                .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                                .setStarterId(incomingRequest.getStarterId())
                                .setWinningId(thisPlant.getId())
                                .setPrice(myprice)
                                .setQuantity(incomingRequest.getQuantity())
                                .build();
                        forwardToNext(newRequest, responseObserver);
                        break;
                    } else
                        // se il mio prezzo è migliore e sono già PARTECIPANTE, ignoro. Significa che sto già partecipando a un'altra elezione
                        if (incomingRequest.getPrice() > myprice && workingStatus == WorkingStatus.PARTICIPANT) {
                            break;
                        }

                // se il prezzo della richiesta non è minore e non è maggiore del mio prezzo, è per forza uguale
                // confronto gli ID delle centrali, il più alto vince
                if (incomingRequest.getWinningId() > thisPlant.getId()) {
                    forwardToNext(incomingRequest, responseObserver);
                    break;
                }

                // Il mio plant ID è maggiore, ho vinto io. Comunico la vittoria
                incomingRequest = ElectionRequestOuterClass.ElectionRequest
                        .newBuilder()
                        .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                        .setStarterId(incomingRequest.getStarterId())
                        .setWinningId(thisPlant.getId())
                        .setPrice(myprice)
                        .setQuantity(incomingRequest.getQuantity())
                        .build();
                workingStatus = WorkingStatus.FREE;
                forwardToNext(incomingRequest, responseObserver);
                break;

            case ELECTED:
                // Status ELECTED e l'ID vincitore non è il mio. Inoltro il messaggio
                if (incomingRequest.getWinningId() != thisPlant.getId()) {
                    forwardToNext(incomingRequest, responseObserver);
                    break;
                }

                // Status ELECTED e ID è il mio. Inizio la fornitura
                System.err.println("ELEZIONE VINCE CENTRALE "+ thisPlant.getId());
                provideEnergy(incomingRequest);
                break;
        }
    }

    private synchronized boolean isBetter(float incomingPrice, int incomingId) {
        if (myprice < incomingPrice) return true;
        return myprice == incomingPrice && thisPlant.getId() > incomingId;
    }

    private synchronized void forwardToNext(ElectionRequestOuterClass.ElectionRequest request, StreamObserver<ElectionRequestOuterClass.ElectionResponse> responseObserver) {
        updateSuccessor();
        try {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(successor.getIpAddress(), successor.getPort() + 1)
                    .usePlaintext()
                    .build();
            ElectionServiceGrpc.ElectionServiceStub stub = ElectionServiceGrpc.newStub(channel);
            stub.handleElection(request, newResponseObserver());
            responseObserver.onNext(ElectionRequestOuterClass.ElectionResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
            channel.shutdown();
        } catch (Exception e) {
            System.err.println("Errore nell'inoltro a " + successor.getId() + ": " + e.getMessage());
        }
    }

    private void provideEnergy(ElectionRequestOuterClass.ElectionRequest request) {
        synchronized (this) {
            workingStatus = WorkingStatus.PROVIDING;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // resetta il topic MQTT
        scheduler.schedule(new MqttTopicReset("localhost"), 500, TimeUnit.MILLISECONDS);

        int sleepingTime = request.getQuantity();
        scheduler.schedule(() -> {
            synchronized (this) {changeWorkingStatus(WorkingStatus.FREE);}
            scheduler.shutdown();
        }, sleepingTime, TimeUnit.MILLISECONDS);
    }



    private enum WorkingStatus {
        FREE, PARTICIPANT, PROVIDING
    }
    
}
