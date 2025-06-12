package AdministrationServer;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;

/**
 * Riceve i dati di inquinamento dal broker MQTT
 */
public class DataReceiver {
    MqttClient client;
    private ArrayList<DataPoint> measurements;

    public DataReceiver(ArrayList<DataPoint> measurements) {
        this.measurements = measurements;
    }

    private void connect() {
        String broker =  "tcp://localhost:1883";
    }

    //TODO implementa metodi MQTT
}
