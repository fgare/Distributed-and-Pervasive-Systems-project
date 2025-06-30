package ThermalPowerPlantPackage;

import ThermalPowerPlantPackage.gRPC.ElectionServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EnergyRequestReceiver implements Runnable {
    private MqttClient client;
    private final String topic = "energyRequest";
    private final ElectionServiceImpl electionService;

    public EnergyRequestReceiver(ElectionServiceImpl electionService) {
        this.electionService = electionService;
    }

    private void connect() throws InterruptedException {
        String broker = "tcp://localhost:1883";
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
        System.out.println("ENERGY REQUEST connected to broker - Thread PID: " + Thread.currentThread().getId());

        client.setCallback(new MqttCallback() {
                               @Override
                               public void connectionLost(Throwable cause) {
                                   System.out.println("ENERGY REQUEST Connection lost! cause:" + cause.getMessage()+ "- Thread PID: " + Thread.currentThread().getId());
                               }

                               @Override
                               public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                                   System.out.println("Ricevuta richiesta di fornitura energia");
                                   Gson gson = new GsonBuilder().create();
                                   JsonObject obj = new JsonParser().parse(new String(mqttMessage.getPayload())).getAsJsonObject();
                                   int amount = obj.get("energy").getAsInt(); // quantità di energia richiesta

                                   // avvio l'elezione dopo un tempo casuale, così che non partano tutte nello stesso momento
                                   ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                                   scheduler.schedule(() -> {
                                       electionService.startElection(amount);
                                       scheduler.shutdown();
                                       }, new Random().nextInt(500), TimeUnit.MILLISECONDS);

                                   System.out.println("ENERGY REQUEST received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                                           "\n\tTime:    " + obj.get("timestamp").getAsLong() +
                                           "\n\tTopic:   " + topic +
                                           "\n\tMessage: " + obj.get("energy").getAsInt() + " kWh" +
                                           "\n\tQoS:     " + mqttMessage.getQos() + "\n");
                               }

                               @Override
                               public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
                           }
        );

        // iscrizione al topic mqtt
        try {
            client.subscribe(topic , 2);
            System.out.println("ENERGY REQUEST Subscribed to topics : " + topic);
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

    public static void main(String[] args) {
        new Thread(new EnergyRequestReceiver(null)).start();
    }
}
