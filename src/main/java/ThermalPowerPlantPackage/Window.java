package ThermalPowerPlantPackage;

import SimulatorsPackage.Buffer;
import SimulatorsPackage.Measurement;
import SimulatorsPackage.PollutionSensor;
import SimulatorsPackage.Simulator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

class Window implements Buffer {
    private final Integer windowDim;
    private final Integer overlapStep; // numero di valori che devono sovrapporsi tra una finestra e la successiva
    private final ArrayDeque<Measurement> measArray;
    private int freshMeas = 0;
    private final ShippingQueue shippingQueue;

    Window(ThermalPowerPlant plant, int windowDim, float overlapFactor) throws IllegalArgumentException {
        if (windowDim < 0) throw new IllegalArgumentException("Window dimension must be non-negative");
        if (overlapFactor < 0 || overlapFactor > 1) throw new IllegalArgumentException("Overlap factor must be between 0 and 1");

        this.windowDim = windowDim;
        this.overlapStep = (int)(overlapFactor * windowDim);
        this.measArray = new ArrayDeque<>(windowDim);
        this.shippingQueue = new ShippingQueue(plant.getIpAddress(), plant.getId());
    }

    private double computeAverage(Measurement[] array) {
        if (array== null || array.length == 0) return 0;

        double sum = 0;
        for (Measurement m : array) {
            sum += m.getValue();
        }
        return sum / array.length;
    }

    @Override
    public void addMeasurement(Measurement m) {
        Measurement[] windowSnapshot; // memorizza lo stato attuale della finestra

        synchronized (this) {
            measArray.addLast(m);
            freshMeas++;
            System.out.println(this);

            // se si supera la dimensione della finestra, viene eliminato l'elemento più vecchio
            if (measArray.size() > windowDim) measArray.removeFirst();

            // dopo aver ottenuto overlapStep misure, viene generata una copia dell' array su cui calcolare la media
            if (freshMeas >= overlapStep) {
                windowSnapshot = measArray.toArray(new Measurement[measArray.size()]);
            } else return;
        }
        // Se si prosegue è perchè si è entrati nell' if precedente. Si calcola quindi la media dei valori

        double avg = computeAverage(windowSnapshot);
        Measurement avgMeas = new Measurement("0", "CO2avg", avg, System.currentTimeMillis());
        shippingQueue.enqueue(avgMeas);
    }

    @Override
    public List<Measurement> readAllAndClean() {
        List<Measurement> measurementsList = new ArrayList<>(measArray);
        measArray.clear();
        freshMeas = 0;
        return measurementsList;
    }

    @Override
    public String toString() {
        return "Window [" + measArray.size() + ", " + freshMeas + "]";
    }

}
