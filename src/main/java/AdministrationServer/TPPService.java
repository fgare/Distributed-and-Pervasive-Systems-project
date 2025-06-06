package AdministrationServer;

import java.util.HashMap;
import org.springframework.stereotype.Service;

@Service
public class TPPService {
    private HashMap<Integer, ThermalPowerPlant> powerPlantsList;

    public TPPService() {
        powerPlantsList = new HashMap<>(10);
    }

    /**
     * Aggiunge una nuova centrale termica alla lista. Se è già presente un'altra centrale con lo stesso nome solleva un'eccezione.
     * @param plant Centrale termica da aggiungere.
     * @throws IdAlreadyExistsException Se è già presente una centrale con lo stesso id.
     */
    public synchronized void add(ThermalPowerPlant plant) throws IdAlreadyExistsException {
        if (powerPlantsList.containsKey(plant.getId()))
            throw new IdAlreadyExistsException("There is another ThermalPowerPlant with ID " + plant.getId());

        powerPlantsList.put(plant.getId(), plant);
    }

}
