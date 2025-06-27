package ThermalPowerPlantPackage;

import SimulatorsPackage.Measurement;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementa la coda per le medie in attesa di essere inviate al server
 */
class ShippingQueue {
    private final ArrayDeque<Measurement> queue;
    private final ScheduledExecutorService scheduler;

    ShippingQueue(String serverAddress, Integer plantId) {
        this.queue = new ArrayDeque<>(10);

        // programma l'esecuzione del thread che pubblica i dati
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new MeasuresPublisher(serverAddress, plantId, this), 10, 10, TimeUnit.SECONDS);
    }

    synchronized void enqueue(Measurement m) {
        queue.addLast(m);
    }

    synchronized Measurement[] getAllAndClean() {
        Measurement[] array = queue.toArray(new Measurement[queue.size()]);
        queue.clear();
        return array;
    }
}
