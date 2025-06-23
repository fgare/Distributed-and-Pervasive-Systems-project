package AdministrationServerPackage;

import SimulatorsPackage.Measurement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.*;

import java.util.TreeSet;

/**
 * Riceve i dati di inquinamento dal broker MQTT
 */
public class DataReceiver implements Runnable {
    private MqttClient client;
    private final String mqttTopic;
    private final TreeSet<Measurement> measurements;

    public DataReceiver(TreeSet<Measurement> measurements, Integer plantID) {
        mqttTopic = "CO2/plant" + plantID;
        // TODO possibile miglioramento. Una singola istanza che si iscrive a CO2/#
        this.measurements = measurements;
    }

    /**
     * Prova la connessione al broker MQTT e completa l'iscrizione al topic
     * In caso di errore esegue tentativi in sequenza dopo aver atteso un tempo specificato.
     * @throws InterruptedException se il thread viene terminato
     */
    private void connect() throws InterruptedException {
        String broker =  "tcp://localhost:1883";
        // prova la connessione al broker MQTT ogni 5 secondi
        while (client == null) {
            try {
                client = new MqttClient(broker, MqttClient.generateClientId());
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setAutomaticReconnect(true);
                connOpts.setConnectionTimeout(10);
                connOpts.setKeepAliveInterval(10);
                client.connect();
            } catch (MqttException e) {
                Thread.sleep(5000);
            }
        }
        System.out.println("Mqtt client " + client.getClientId() + " connected to broker - Thread PID: " + Thread.currentThread().getId());

        client.setCallback(new MqttCallback() {
                               @Override
                               public void connectionLost(Throwable cause) {
                                   System.out.println(client.getClientId() + " Connection lost! cause:" + cause.getMessage()+ "- Thread PID: " + Thread.currentThread().getId());
                               }

                               @Override
                               public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                                   Gson gsonBuilder = new GsonBuilder().create();
                                   Measurement meas = gsonBuilder.fromJson(String.valueOf(mqttMessage.getPayload()), Measurement.class);

                                   // aggiunge la nuova misura nell'insieme
                                   synchronized (measurements) {
                                       measurements.add(meas);
                                   }

                                   System.out.println(client.getClientId() +" received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                                           "\n\tTime:    " + meas.getTimestamp() +
                                           "\n\tTopic:   " + topic +
                                           "\n\tMessage: " + meas.getValue() +
                                           "\n\tQoS:     " + mqttMessage.getQos() + "\n");
                               }

                               @Override
                               public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
                           }
        );

        // iscrizione al topic mqtt
        try {
            client.subscribe(mqttTopic , 2);
            System.out.println(client.getClientId() + " Subscribed to topics : " + mqttTopic);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
