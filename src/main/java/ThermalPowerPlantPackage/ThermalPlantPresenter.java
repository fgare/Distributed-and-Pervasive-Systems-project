package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * Si occupa di registrare una centrale termica presso l'Administration server
 */
class ThermalPlantPresenter {
    private final ThermalPowerPlant plant;
    private final String apiServerIp;
    private final Integer apiServerPort;
    private final RestTemplate client;

    ThermalPlantPresenter(ThermalPowerPlant plant, String apiServerIp, Integer apiServerPort) {
        this.plant = plant;
        this.apiServerIp = apiServerIp;
        this.apiServerPort = apiServerPort;
        client = new RestTemplate();
    }

    /**
     * Contatta l'Administration server e pubblicala nuova centrale
     * @throws IdAlreadyExistsException Se ottiene una risposta di errore dal server. Significa che è già stata registrata una centrale con lo stesso id
     */
    Set<PlantInfo> publishPlant() throws IdAlreadyExistsException {
        String postPath = "/ThermalPowerPlants";
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(PlantInfo.class, new PlantInfoSerializer())
                .setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        String jsonString = gson.toJson(plant);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonString, headers);

        ResponseEntity<String> response = client.postForEntity("http://"+ apiServerIp +":" +apiServerPort +postPath, request, String.class);

        return getPlantsList(response.getBody());
    }

    private Set<PlantInfo> getPlantsList(String jsonString) {
        JsonArray jsonPlants = new JsonParser().parse(jsonString).getAsJsonArray();
        Set<PlantInfo> otherPlants = new HashSet<>(jsonPlants.size());

        for (int i = 0; i < jsonPlants.size(); i++) {
            Integer id = jsonPlants.get(i).getAsJsonObject().get("id").getAsInt();
            String ip;
            try {
                ip = jsonPlants.get(i).getAsJsonObject().get("ip").getAsString();
            } catch (NullPointerException e) {
                ip = "localhost";
            }
            Integer port = jsonPlants.get(i).getAsJsonObject().get("port").getAsInt();
            otherPlants.add(new PlantInfo(id, ip, port));
        }
        return otherPlants;
    }

    public void presentToOtherPlants() {
        Set<PlantInfo> plastsList = plant.getOtherPlants();

        if (plastsList.isEmpty()) {
            System.out.println("There is only one plant in the network");
            return;
        }

        for (PlantInfo otherPlant : plastsList) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(otherPlant.getIpAddress(), otherPlant.getPort())
                    .usePlaintext()
                    .build();
            PresentationServiceGrpc.PresentationServiceBlockingStub stub = PresentationServiceGrpc.newBlockingStub(channel);

            Presentation.IntroductionRequest introReq = Presentation.IntroductionRequest
                    .newBuilder()
                    .setId(plant.getId())
                    .setIp(plant.getIpAddress())
                    .setPort(plant.getPort())
                    .build();

            Presentation.IntroductionResponse introResp;
            introResp = stub.introduce(introReq);
            if (!introResp.getSuccess()) {
                System.err.println("Unable to introduce plant " + plant.getId() + " to " + otherPlant.getId());
                continue;
            }
            System.out.println("Plant " + plant.getId() + " introduced to " + otherPlant.getId());
            channel.shutdown();
        }
    }

}
