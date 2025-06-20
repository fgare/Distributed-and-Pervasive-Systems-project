package ThermalPowerPlantPackage;

import SimulatorsPackage.Measurement;

import java.util.ArrayDeque;

/**
 * Implementa la coda per le medie in attesa di essere inviate al server
 */
class ShippingQueue {
    private final ArrayDeque<Measurement> queue;

    ShippingQueue(String serverAddress) {
        this.queue = new ArrayDeque<>(10);
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
