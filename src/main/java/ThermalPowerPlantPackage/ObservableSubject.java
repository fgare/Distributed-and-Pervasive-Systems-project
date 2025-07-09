package ThermalPowerPlantPackage;

import java.util.HashSet;
import java.util.Set;

abstract class ObservableSubject {
    private final Set<PlantsSetObserver> observers;

    ObservableSubject() {
        observers = new HashSet<>();
    }

    void registerObserver(PlantsSetObserver observer) {
        observers.add(observer);
    }

    void removeObserver(PlantsSetObserver observer) {
        observers.remove(observer);
    }

    void notifyObservers() {
        for (PlantsSetObserver plant : observers) {
            //TODO
        }
    }

}
