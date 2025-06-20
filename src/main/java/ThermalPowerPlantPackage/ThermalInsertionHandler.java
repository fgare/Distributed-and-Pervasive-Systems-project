package ThermalPowerPlantPackage;

import AdministrationServerPackage.IdAlreadyExistsException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Si occupa di registrare una centrale termica presso l'Administration server
 */
public class ThermalInsertionHandler {
    private final ThermalPowerPlant plant;
    private final String serverAddress;
    private final RestTemplate client;

    public ThermalInsertionHandler(ThermalPowerPlant plant, String serverAddress, Integer serverPort) {
        this.plant = plant;
        this.serverAddress = serverAddress + ":" + serverPort;
        client = new RestTemplate();
    }

    /**
     * Contatta l'Administration server e pubblicala nuova centrale
     * @throws IdAlreadyExistsException Se ottiene una risposta di errore dal server. Significa che è già stata registrata una centrale con lo stesso id
     */
    public void publishPlant() throws IdAlreadyExistsException {
        String postPath = "/ThermalPowerPlants/add";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(plant.toJsonString(), headers);
        ResponseEntity<Void> response = client.postForEntity(serverAddress+postPath, request, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new IdAlreadyExistsException(plant.getId());
        //TODO riceve una stringa JSON
    }

    public static void main(String[] args) throws IdAlreadyExistsException, IOException {

    }

}
