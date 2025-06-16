package AdministrationServer;

import org.eclipse.paho.client.mqttv3.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeSet;

/**
 * Riceve i dati di inquinamento dal broker MQTT
 */
public class DataReceiver implements Runnable {
    private MqttClient client;
    private final String mqttTopic;
    private final TreeSet<DataPoint> measurements;

    public DataReceiver(TreeSet<DataPoint> measurements, Integer plantID) {
        mqttTopic = "co2/plant" + plantID;
        this.measurements = measurements;
    }

    /**
     * Prova la connessione al broker MQTT e completa l'iscrizione al topic
     * In caso di errore esegue tentativi in sequenza dopo aver atteso un tempo specificato.
     * @throws InterruptedException se il thread viene terminato
     */
    private void connect() throws InterruptedException {
        String broker =  "tcp://localhost:1883";
        System.out.println("Trying connection to MQTT broker");
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
                Thread.sleep(5*1000);
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
                                   Long timestamp = Duration.between(LocalTime.MIDNIGHT, LocalDateTime.now()).getSeconds(); //TODO  devono essere millisecondi
                                   Integer receivedValue = Integer.valueOf(new String(mqttMessage.getPayload()));

                                   // aggiunge la nuova misura nell'insieme
                                   synchronized (measurements) {
                                       measurements.add(new DataPoint(timestamp, receivedValue));
                                   }

                                   System.out.println(client.getClientId() +" received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                                           "\n\tTime:    " + timestamp +
                                           "\n\tTopic:   " + topic +
                                           "\n\tMessage: " + receivedValue +
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
