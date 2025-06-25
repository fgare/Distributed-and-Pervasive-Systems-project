package AdministrationClientPackage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class APIClient {
    private final RestTemplate client;
    private final String serverAddress;

    APIClient(String serverAddress) {
        this.serverAddress = serverAddress;
        client = new RestTemplate();
    }

    String getPlantsList() throws RestClientException {
        String getPath = "/ThermalPowerPlants";
        ResponseEntity<String> response = client.getForEntity("http://"+serverAddress+getPath, String.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = new JsonParser().parse(response.getBody());
        return gson.toJson(jsonElement);
    }


}
