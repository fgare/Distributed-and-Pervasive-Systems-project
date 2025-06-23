package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Si occupa di registrare una centrale termica presso l'Administration server
 */
class ThermalPlantPresenter {
    private final ThermalPowerPlant plant;
    private final String serverAddress;
    private final RestTemplate client;

    ThermalPlantPresenter(ThermalPowerPlant plant, String serverAddress, Integer serverPort) {
        this.plant = plant;
        this.serverAddress = serverAddress + ":" + serverPort;
        client = new RestTemplate();
    }

    /**
     * Contatta l'Administration server e pubblicala nuova centrale
     * @throws IdAlreadyExistsException Se ottiene una risposta di errore dal server. Significa che è già stata registrata una centrale con lo stesso id
     */
    List<OtherPlant> publishPlant() throws IdAlreadyExistsException {
        String postPath = "/ThermalPowerPlants/add";
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(ThermalPowerPlant.class, new ThermalPowerPlantSerializer())
                .setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        String jsonString = gson.toJson(plant);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonString, headers);
        ResponseEntity<String> response = client.postForEntity("http://"+serverAddress+postPath, request, String.class);
        // verifica che il codice di risposta corrisponda a successo
        if (!response.getStatusCode().is2xxSuccessful()) throw new IdAlreadyExistsException(plant.getId());

        return getPlantsList(response.getBody());
    }

    private ArrayList<OtherPlant> getPlantsList(String jsonString) {
        JsonArray jsonPlants = new JsonParser().parse(jsonString).getAsJsonArray();
        ArrayList<OtherPlant> otherPlants = new ArrayList<>(jsonPlants.size());

        for (int i = 0; i < jsonPlants.size(); i++) {
            Integer id = jsonPlants.get(i).getAsJsonObject().get("id").getAsInt();
            String ip = jsonPlants.get(i).getAsJsonObject().get("ip").getAsString();
            Integer port = jsonPlants.get(i).getAsJsonObject().get("port").getAsInt();
            otherPlants.add(new OtherPlant(id, ip, port));
        }
        return otherPlants;
    }

}
