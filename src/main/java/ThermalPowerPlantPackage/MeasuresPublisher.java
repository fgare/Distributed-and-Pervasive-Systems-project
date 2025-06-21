package ThermalPowerPlantPackage;

import SimulatorsPackage.Measurement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.paho.client.mqttv3.*;

import java.nio.charset.StandardCharsets;


class MeasuresPublisher implements Runnable {
    private MqttClient client;
    private final String clientId;
    private final String broker;
    private final String topic;
    private final int qos = 2;
    private final ShippingQueue queue;
    private final Integer plantId;

    MeasuresPublisher (String serverIp, Integer plantId, ShippingQueue queue) throws MqttException {
        this.broker = "tcp://" + serverIp + ":1883";
        this.topic = "plants/plant" + plantId;
        this.queue = queue;
        this.plantId = plantId;
        clientId = MqttClient.generateClientId();
        connect();
        System.out.println(clientId + " connected - Thread PID: " + Thread.currentThread().getId());
    }

    void connect() throws MqttException {
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
    }

    private void publishData(byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        client.publish(topic, message);
        System.out.println(clientId + " Message " + message + " on topic " + topic + " - Thread PID: " + Thread.currentThread().getId());
    }

    /**
     * Costruisce la stringa JSON, body della risposta
     * @param data array di misure da inviare
     * @return stringa JSON
     */
    private String buildPayload(Measurement[] data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // costruisce il json da pubblicare
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("plantId", plantId);
        jsonObject.addProperty("timestamp", System.currentTimeMillis());

        JsonArray jsonArray = new JsonArray();
        for (Measurement m : data) {
            jsonArray.add(gson.toJson(m));
        }
        jsonObject.add("data", jsonArray);

        return jsonObject.toString();
    }

    @Override
    public void run() {
        // recupera i dati
        Measurement[] data = queue.getAllAndClean();
        if (data == null || data.length == 0) return;

        String payload = buildPayload(data);

        try {
            publishData(payload.getBytes(StandardCharsets.UTF_8));
            client.disconnect(); // terminata la pubblicazione, si disconnette dal broker
        } catch (MqttException e) {
            System.err.println("Error publishing data to MQTT broker: " + e.getMessage());
        }
    }
}
