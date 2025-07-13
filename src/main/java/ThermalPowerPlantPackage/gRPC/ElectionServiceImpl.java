package ThermalPowerPlantPackage.gRPC;

import ThermalPowerPlantPackage.ElectionRequestOuterClass;
import ThermalPowerPlantPackage.ElectionServiceGrpc;
import ThermalPowerPlantPackage.ThermalPowerPlant;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ElectionServiceImpl extends ElectionServiceGrpc.ElectionServiceImplBase {
    private final ThermalPowerPlant thisPlant;
    private final RingClient successor;
    private WorkingStatus workingStatus = WorkingStatus.FREE;
    private ManagedChannel channel; // canale di comunicazione con il successore
    private ElectionServiceGrpc.ElectionServiceStub stub;
    private float myprice;


    ElectionServiceImpl(ThermalPowerPlant tp, RingClient successor) {
        this.thisPlant = tp;
        this.successor = successor;
    }

    synchronized void changeWorkingStatus(WorkingStatus newStatus) {
        workingStatus = newStatus;
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

        if (successor == null) {
            provideEnergy(request);
            return;
        }

        forwardToNext(request);
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
                // inoltro il messaggio che comunica il vincitore
                newRequest = ElectionRequestOuterClass.ElectionRequest
                        .newBuilder()
                        .setStatus(ElectionRequestOuterClass.ElectionRequest.Status.ELECTED)
                        .setWinningId(incomingRequest.getWinningId())
                        .setStarterId(incomingRequest.getStarterId())
                        .setPrice(incomingRequest.getPrice())
                        .setQuantity(incomingRequest.getQuantity())
                        .build();
                forwardToNext(newRequest);
                // se il vincitore sono io, inizio a fornire energia
                if (incomingRequest.getWinningId() == thisPlant.getId()) {
                    provideEnergy(incomingRequest);
                }
            } else
                // verifico se la mia offerta è migliore, inoltro i miei riferimenti
                if (iAmBetter(incomingRequest.getPrice(), incomingRequest.getWinningId())) {
                    // se ho un'offerta migliore e sono libero, mi segno come partecipante e inoltro i miei riferimenti
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

            // se arriva ELECTED e sono FREE significa che non ho partecipato all'elezione, inoltro e basta
            if (workingStatus == WorkingStatus.FREE) {
                forwardToNext(incomingRequest);
            } else
                // sono partecipante e ho vinto io, allora inizio a fornire energia
                if (incomingRequest.getWinningId() == thisPlant.getId() && workingStatus == WorkingStatus.PARTICIPANT) {
                    System.err.println("Ho vinto io");
                    provideEnergy(incomingRequest); // fornisce energia
            } else {
                // ho partecipato ma ho perso l'elezione, torno FREE e inoltro
                workingStatus = WorkingStatus.FREE;
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
        Context newContext = Context.current().fork();
        Context origContext = newContext.attach();
        try {
            successor.forwardMessage(request);
        } finally {
            newContext.detach(origContext);
        }
    }

    private void provideEnergy(ElectionRequestOuterClass.ElectionRequest request) {
        workingStatus = WorkingStatus.PROVIDING;
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


    private enum WorkingStatus {
        FREE, PARTICIPANT, PROVIDING
    }
    
}
