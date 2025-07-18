package ThermalPowerPlantPackage.gRPC;

import ThermalPowerPlantPackage.PlantInfo;
import ThermalPowerPlantPackage.Presentation.IntroductionRequest;
import ThermalPowerPlantPackage.Presentation.IntroductionResponse;
import ThermalPowerPlantPackage.PresentationServiceGrpc;
import ThermalPowerPlantPackage.ThermalPowerPlant;
import io.grpc.stub.StreamObserver;

class PresentationServiceImpl extends PresentationServiceGrpc.PresentationServiceImplBase {
    private final ThermalPowerPlant mainPlant;

    PresentationServiceImpl(ThermalPowerPlant mainPlant) {
        this.mainPlant = mainPlant;
    }

    @Override
    public void introduce(IntroductionRequest introRequest, StreamObserver<IntroductionResponse> responseObserver) {
        int id = introRequest.getId();
        String ip = introRequest.getIp();
        int port = introRequest.getPort();

        boolean result = mainPlant.addColleaguePlant(new PlantInfo(id, ip, port));

        IntroductionResponse response = IntroductionResponse
                .newBuilder()
                .setSuccess(result)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
