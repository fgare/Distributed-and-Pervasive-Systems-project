package AdministrationServer;

import java.util.ArrayList;

/**
 * Riceve i dati di inquinamento dal broker MQTT
 */
public class DataReceiver {
    private ArrayList<DataPoint> measurements;

    public DataReceiver(ArrayList<DataPoint> measurements) {
        this.measurements = measurements;
    }

    //TODO implementa metodi MQTT
}
