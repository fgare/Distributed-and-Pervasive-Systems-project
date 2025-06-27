package AdministrationServerPackage;

import java.util.*;

import com.google.gson.*;
import org.springframework.stereotype.Service;

@Service
public class TPPService {
    private final HashMap<Integer, VirtualThermalPowerPlant> powerPlantsList;
    private final Gson gson;

    public TPPService() {
        powerPlantsList = new HashMap<>(10);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(VirtualThermalPowerPlant.class, new VirtualThermalPowerPlantSerializer());
        this.gson = gsonBuilder.setPrettyPrinting().create();
    }

    public synchronized String getAllPlants() {
        JsonArray jsonArray = new JsonArray();
        for(VirtualThermalPowerPlant vp : powerPlantsList.values()) {
            JsonObject currentObject = new JsonObject();
            currentObject.addProperty("id", vp.getId());
            currentObject.addProperty("ip", vp.getIpAddress());
            currentObject.addProperty("port", vp.getPort());
            jsonArray.add(currentObject);
        }

        return gson.toJson(jsonArray);
    }

    /**
     * Aggiunge una nuova centrale termica alla lista.
     * Se è già presente un'altra centrale con lo stesso nome solleva un'eccezione.
     * @param plant Centrale termica da aggiungere.
     * @return Lista delle centrali presenti al momento della chiamata (senza la centrale che si vuole aggiungere)
     * @throws IdAlreadyExistsException Se è già presente una centrale con lo stesso id.
     */
    public synchronized String addPlant(VirtualThermalPowerPlant plant) throws IdAlreadyExistsException {
        if (powerPlantsList.containsKey(plant.getId()))
            throw new IdAlreadyExistsException(plant.getId());

        String currentPlantsList = getAllPlants();

        // aggiunge la nuova centrale
        powerPlantsList.put(plant.getId(), plant);

        return currentPlantsList;
    }

    /**
     * Calcola la media di inquinamento per ciascuna centrale
     * @param from inizio intervallo
     * @param to fine intervallo
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
