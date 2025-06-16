package ThermalPowerPlant;

import AdministrationServer.IdAlreadyExistsException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Si occupa di registrare una centrale termica presso l'Administration server
 */
public class ThermalInsertionHandler {
    private final ThermalPowerPlant plant;
    private final String serverAddress = "http://localhost:8080";
    private final RestTemplate client;

    public ThermalInsertionHandler(ThermalPowerPlant plant) {
        this.plant = plant;
        client = new RestTemplate();
    }

    /**
     * Contatta l'Administration server e pubblicala nuova centrale
     * @throws IdAlreadyExistsException Se ottiene una risposta di errore dal server. Significa che è già stata registrata una centrale con lo stesso id
     */
    public void publishPlant() throws IdAlreadyExistsException {
        String postPath = "/plants/add";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ThermalPowerPlant> request = new HttpEntity<>(plant, headers);
        ResponseEntity<Void> response = client.postForEntity(serverAddress+postPath, request, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) throw new IdAlreadyExistsException(plant.getId());
    }

}
