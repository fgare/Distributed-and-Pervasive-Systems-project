package RenewableEnergyProviderPackage;

import SimulatorsPackage.Measurement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;
import java.util.Random;

class RequestPublisher implements Runnable {
    private Integer energyQuantity = 0;
    private final String clientId;
    private MqttClient client;
    private final String broker;
    private final String topic = "RenewableEnergyRequest";
    private final int qos = 2;


    RequestPublisher(String serverIp) {
        this.broker = "tcp://" + serverIp + ":1883";
        clientId = MqttClient.generateClientId();
    }

    private void connect() throws MqttException {
        client = new MqttClient(broker, clientId);

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        client.connect(connOpts);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println(clientId + " Connection lost! cause:" + throwable.getMessage());
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                System.out.println(clientId + " Message delivered - Thread PID: " + Thread.currentThread().getId());
            }
        });
        System.out.println(clientId + " connected - Thread PID: " + Thread.currentThread().getId());
    }

    private void publishData(byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        client.publish(topic, message);
        System.out.println(clientId + " Message " + message + " on topic " + topic + " - Thread PID: " + Thread.currentThread().getId());
    }

    private String buildPayload() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // costruisce il json da pubblicare
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("energy", energyQuantity);
        jsonObject.addProperty("timestamp", System.currentTimeMillis());
        return jsonObject.toString();
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (MqttException e) {
            System.err.println("Error connecting to MQTT broker: " + e.getMessage());
            return;
        }

        energyQuantity = 5000 + new Random().nextInt(10000);
        String payload = buildPayload();

        try {
            publishData(payload.getBytes(StandardCharsets.UTF_8));
            client.disconnect();
        } catch (MqttException e) {
            System.err.println("Error publishing data to MQTT broker: " + e.getMessage());
        }

    }
}
