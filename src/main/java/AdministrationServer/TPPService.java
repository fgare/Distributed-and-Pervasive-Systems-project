package AdministrationServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ThemeResolver;

@Service
public class TPPService {
    private final ThemeResolver themeResolver;
    private final HashMap<Integer, ThermalPowerPlant> powerPlantsList;

    public TPPService(ThemeResolver themeResolver) {
        powerPlantsList = new HashMap<>(10);
        this.themeResolver = themeResolver;
    }

    public synchronized List<ThermalPowerPlant> getAll() {
        return new ArrayList<>(powerPlantsList.values());
    }

    /**
     * Aggiunge una nuova centrale termica alla lista.
     * Se è già presente un'altra centrale con lo stesso nome solleva un'eccezione.
     * @param plant Centrale termica da aggiungere.
     * @throws IdAlreadyExistsException Se è già presente una centrale con lo stesso id.
     */
    public synchronized void add(ThermalPowerPlant plant) throws IdAlreadyExistsException {
        if (powerPlantsList.containsKey(plant.getId()))
            throw new IdAlreadyExistsException("There is another ThermalPowerPlant with ID " + plant.getId());

        powerPlantsList.put(plant.getId(), plant);
    }

}
