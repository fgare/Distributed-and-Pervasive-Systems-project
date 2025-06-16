package ThermalPowerPlant;

import Simulators.Buffer;
import Simulators.Measurement;
import io.opencensus.stats.Measure;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
        double sum = 0;
        for (Measurement measurement : measArray) {
            sum += measurement.getValue();
        }
        return sum / measArray.length;
    }

    @Override
    public void addMeasurement(Measurement m) {
        synchronized (measurements) {
            measurements.addLast(m);
            freshMeasurements++;

            if (measurements.size() > windowDimension) measurements.removeFirst();

            if (freshMeasurements >= overlapStep) {
                computeAverage(measurements.toArray(new Measurement[measurements.size()]));
            }
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {
        return List.of();
    }
}
