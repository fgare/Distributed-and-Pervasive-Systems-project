package AdministrationServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TPPService {
    private final HashMap<Integer, VirtualThermalPowerPlant> powerPlantsList;

    public TPPService() {
        powerPlantsList = new HashMap<>(10);
    }

    public synchronized List<VirtualThermalPowerPlant> getAllPlants() {
        return new ArrayList<>(powerPlantsList.values());
    }

    /**
     * Aggiunge una nuova centrale termica alla lista.
     * Se è già presente un'altra centrale con lo stesso nome solleva un'eccezione.
     * @param plant Centrale termica da aggiungere.
     * @throws IdAlreadyExistsException Se è già presente una centrale con lo stesso id.
     */
    public synchronized void addPlant(VirtualThermalPowerPlant plant) throws IdAlreadyExistsException {
        if (powerPlantsList.containsKey(plant.getId()))
            throw new IdAlreadyExistsException(plant.getId());

        powerPlantsList.put(plant.getId(), plant);
    }

    /**
     * Calcola la media di inquinamento per ciascuna centrale
     * @param from inizio intervallo
     * @param to fino intervallo
     * @return mappa con (id centrale termica, valore di inquinamento medio)
     */
    public synchronized Map<Integer, Float> averagePollution(Long from, Long to) throws IllegalArgumentException {
        // calcola la dimensione che permette di inserire tutte le centrali senza che la mappa debba ridimensionarsi
        int mapDim = (int) (powerPlantsList.size()/0.75 + 1);
        HashMap<Integer, Float> avgByPlant = new HashMap<>(mapDim);

        for (VirtualThermalPowerPlant plant: powerPlantsList.values()) {
            Float value = plant.getAverageMeasurementBetween(from, to);
            avgByPlant.put(plant.getId(), value);
        }

        return avgByPlant;
    }

}
