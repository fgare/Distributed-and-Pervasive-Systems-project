package AdministrationServerPackage;

import java.util.*;

import ThermalPowerPlantPackage.ThermalPowerPlant;
import com.google.gson.*;
import org.springframework.stereotype.Service;

@Service
public class TPPService {
    private final HashMap<Integer, VirtualThermalPowerPlant> powerPlantsList;
    private final Gson gson;

    public TPPService(Gson gson) {
        powerPlantsList = new HashMap<>(10);
        this.gson = gson;
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
    public synchronized String addPlant(VirtualThermalPowerPlant plant) throws IdAlreadyExistsException {
        if (powerPlantsList.containsKey(plant.getId()))
            throw new IdAlreadyExistsException(plant.getId());

        HashMap<Integer, VirtualThermalPowerPlant> oldMap = (HashMap<Integer, VirtualThermalPowerPlant>) powerPlantsList.clone();
        // genera il json con l'elenco delle centrali registrate
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(VirtualThermalPowerPlant.class, new VirtualThermalPowerPlantSerializer());
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.create();

        JsonArray jsonArray = new JsonArray();
        for (VirtualThermalPowerPlant p : oldMap.values()) {
            JsonElement currentObject = gson.toJsonTree(p);
            jsonArray.add(currentObject);
        }

        // aggiunge la nuova centrale
        powerPlantsList.put(plant.getId(), plant);

        return gson.toJson(jsonArray);
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
