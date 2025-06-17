package ThermalPowerPlantPackage;

import SimulatorsPackage.Buffer;
import SimulatorsPackage.Measurement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.List;

public class MeasureManager implements Buffer {
    private final Integer windowDimension;
    private final Integer overlapStep;
    private final ArrayDeque<Measurement> measurements;
    private int freshMeasurements = 0;
    private MeasuresPublisher measuresPublisher;

    public MeasureManager(ThermalPowerPlant plant, int windowDim, float overlapFactor) throws IllegalArgumentException {
        if (windowDim < 0) throw new IllegalArgumentException("Window dimension must be non-negative");
        if (overlapFactor < 0 || overlapFactor > 1) throw new IllegalArgumentException("Overlap factor must be between 0 and 1");

        this.windowDimension = windowDim;
        overlapStep = (int)(overlapFactor * windowDim);
        measurements = new ArrayDeque<>(windowDim);

        try {
            measuresPublisher = new MeasuresPublisher("CO2/plant" + plant.getId());
        } catch (MqttException mqtte) {
            mqtte.printStackTrace();
        }
    }

    private double computeAverage(Measurement[] measArray) {
        if (measArray.length == 0) return 0;

        double sum = 0;
        for (Measurement m : measArray) {
            sum += m.getValue();
        }
        return sum / measArray.length;
    }

    @Override
    public void addMeasurement(Measurement m) {
        Measurement[] windowSnapshot; // memorizza lo stato attuale della finestra

        synchronized (this) {
            measurements.addLast(m);
            freshMeasurements++;

            // se si supera la dimensione della finestra, viene eliminato l'elemento più vecchio
            if (measurements.size() > windowDimension) measurements.removeFirst();

            // dopo aver ottenuto overlapStep misure, viene generata una copia dell' array su cui calcolare la media
            if (freshMeasurements >= overlapStep) {
                windowSnapshot = measurements.toArray(new Measurement[measurements.size()]);
            } else return;
        }
        // Se si prosegue è perchè si è entrati nell' if precedente. Si calcola quindi la media dei valori

        double avg = computeAverage(windowSnapshot);
        Measurement avgMeas = new Measurement("0", "CO2avg", avg, System.currentTimeMillis());

        Gson gsonBuilder = new GsonBuilder().create();
        String jsonString = gsonBuilder.toJson(avgMeas);

        // pubblica il valore medio sul broker MQTT
        try {
            measuresPublisher.publish(jsonString.getBytes(StandardCharsets.UTF_8));
        } catch(MqttException mqte) {
            System.err.println("Could not publish measurements to MQTT broker");
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {
        return List.of();
    }
}
