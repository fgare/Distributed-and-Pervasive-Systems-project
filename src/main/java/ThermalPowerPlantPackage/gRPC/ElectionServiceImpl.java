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

        PlantInfo old = successor;

        // determina la centrale successiva nell'anello
        PlantInfo nextplant = otherPlantsList.higher(thisPlant);
        // se questa centrale è l'ultima, il metodo higher() ritorna NULL. Bisogna quindi ripartire dalla prima centrale
        if (nextplant == null) nextplant = otherPlantsList.first();
        this.successor = nextplant;
        if (old != successor) {
            channel.shutdown();
            updateChannel();
        }
    }

    public void updateChannel() {
        channel = ManagedChannelBuilder
                .forAddress(successor.getIpAddress(), successor.getPort() + 1)
                .usePlaintext()
                .build();
    }

    public void startElection(int energykWh) {
        myprice = (float) (0.1 + new Random().nextFloat()*0.8);

        System.err.println("ELEZIONE INIZIATA CENTRALE "+ thisPlant.getId());
        
        ElectionRequestOuterClass.ElectionRequest request = ElectionRequestOuterClass.ElectionRequest
                    .newBuilder()
                    .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                    .setStarterId(thisPlant.getId())
                    .setWinningId(thisPlant.getId())
                    .setPrice(myprice)
                    .setQuantity(energykWh)
                    .build();

        updateSuccessor();
        if (successor == null) {
            provideEnergy(request);
            return;
        }

        forwardToNext(request);
    }

    private StreamObserver<ElectionRequestOuterClass.ElectionResponse> newResponseObserver() {
        return new StreamObserver<ElectionRequestOuterClass.ElectionResponse>() {
            @Override
            public void onNext(ElectionRequestOuterClass.ElectionResponse value) {
                System.err.println("Esito comunicazione " + value.getSuccess() + " con " + successor.getId());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Errore nell'inoltro a " + successor.getId() + ": " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.err.println("Comunicazione con " + successor.getId() + " completata");
            }
        };
    }

    @Override
    public void handleElection(ElectionRequestOuterClass.ElectionRequest incomingRequest, StreamObserver<ElectionRequestOuterClass.ElectionResponse> responseObserver) {
        myprice = (float) (0.1 + new Random().nextFloat()*0.8);
        ElectionRequestOuterClass.ElectionRequest newRequest;
        System.err.println("CENTRALE "+ thisPlant.getId() +" RICEVE ELEZIONE");

        if (workingStatus == WorkingStatus.PROVIDING) {
            forwardToNext(incomingRequest);
            responseObserver.onNext(ElectionRequestOuterClass.ElectionResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
            return;
        }

        if (incomingRequest.getStatus() == ElectionRequestOuterClass.ElectionRequest.Status.ELECTION) {
            // la mia elezione è tornata a me. E' stato deciso un vincitore
            if (incomingRequest.getStarterId() == thisPlant.getId() && workingStatus == WorkingStatus.PARTICIPANT) {
                workingStatus = WorkingStatus.FREE;
                // TODO potrebbe aver vinto l'elezione e dover fornire energia
                newRequest = ElectionRequestOuterClass.ElectionRequest
                        .newBuilder()
                        .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTED)
                        .setWinningId(incomingRequest.getWinningId())
                        .setStarterId(incomingRequest.getStarterId())
                        .setPrice(incomingRequest.getPrice())
                        .setQuantity(incomingRequest.getQuantity())
                        .build();
                forwardToNext(newRequest);
            } else
                // verifico se la mia offerta è migliore, inoltro i miei riferimenti
                if (iAmBetter(incomingRequest.getPrice(), incomingRequest.getWinningId())) {
                    // se sono libero mi segno come partecipante e inoltro i miei riferimenti
                    if (workingStatus == WorkingStatus.FREE) {
                        workingStatus = WorkingStatus.PARTICIPANT;
                        newRequest = ElectionRequestOuterClass.ElectionRequest
                                .newBuilder()
                                .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTION)
                                .setWinningId(thisPlant.getId())
                                .setStarterId(incomingRequest.getStarterId())
                                .setPrice(myprice)
                                .setQuantity(incomingRequest.getQuantity())
                                .build();
                        forwardToNext(newRequest);
                    } // altrimenti ignoro, interrompo l'altra elezione
                } else {
                    // se sono peggio, mi metto partecipante e inoltro solamente
                    workingStatus = WorkingStatus.PARTICIPANT;
                    forwardToNext(incomingRequest);
                }
        } else {
            // ELECTED
            System.err.println("CENTRALE "+ incomingRequest.getWinningId() +" VINCE ELEZIONE");

            if (incomingRequest.getWinningId() == thisPlant.getId()) {
                // ho vinto io
                System.err.println("Ho vinto io");
                workingStatus = WorkingStatus.PROVIDING;
                // TODO fornisco energia
            } else {
                forwardToNext(incomingRequest);
            }
        }

        responseObserver.onNext(ElectionRequestOuterClass.ElectionResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    private boolean iAmBetter(float incomingPrice, int incomingId) {
        if (myprice < incomingPrice) return true;
        return myprice == incomingPrice && thisPlant.getId() > incomingId;
    }

    private void forwardToNext(ElectionRequestOuterClass.ElectionRequest request) {
        updateSuccessor();

        try {
            /*ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(successor.getIpAddress(), successor.getPort() + 1)
                    .usePlaintext()
                    .build();*/
            ElectionServiceGrpc.ElectionServiceStub stub = ElectionServiceGrpc.newStub(channel);
            stub.handleElection(request, new StreamObserver<ElectionRequestOuterClass.ElectionResponse>() {
                @Override
                public void onNext(ElectionRequestOuterClass.ElectionResponse value) {
                    System.err.println("Esito comunicazione " + value.getSuccess() + " con " + successor.getId());
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Errore nell'inoltro a " + successor.getId() + ": " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.err.println("Comunicazione con " + successor.getId() + " completata");
                }
            });
        } catch (Exception e) {
            System.err.println("Errore nell'inoltro a " + successor.getId() + ": " + e.getMessage());
        }
    }

    private void provideEnergy(ElectionRequestOuterClass.ElectionRequest request) {
        synchronized (this) {
            workingStatus = WorkingStatus.PROVIDING;
        }
        System.err.println("CENTRALE "+ thisPlant.getId() +" FORNECE ENERGIA");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // resetta il topic MQTT
        scheduler.schedule(new MqttTopicReset("localhost"), 500, TimeUnit.MILLISECONDS);

        int sleepingTime = request.getQuantity();
        scheduler.schedule(() -> {
            synchronized (this) {changeWorkingStatus(WorkingStatus.FREE);}
            scheduler.shutdown();
        }, sleepingTime, TimeUnit.MILLISECONDS);
    }

    private void shutdownChannel(ManagedChannel channel) {
        new Thread(() -> {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
            }
        }).start();
    }



    private enum WorkingStatus {
        FREE, PARTICIPANT, PROVIDING
    }
    
}
