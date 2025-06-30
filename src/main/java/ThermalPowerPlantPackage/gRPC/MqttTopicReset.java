package ThermalPowerPlantPackage.gRPC;

import org.eclipse.paho.client.mqttv3.*;

class MqttTopicReset implements Runnable {
    private final String clientId;
    private MqttClient client;
    private final String broker;
    private final String topic = "energyRequest";
    private final int qos = 2;


    MqttTopicReset (String serverIp) {
        this.broker = "tcp://" + serverIp + ":1883";
        clientId = MqttClient.generateClientId();
    }

    private void connect() throws MqttException {
        client = new MqttClient(broker, clientId);

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);
        client.connect(connOpts);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println(clientId + " Connection lost! cause:" + throwable.getMessage());
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {}

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                System.out.println(clientId + " Topic reset - Thread PID: " + Thread.currentThread().getId());
            }
        });
        //System.out.println(clientId + " connected - Thread PID: " + Thread.currentThread().getId());
    }

    private void publishData(byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload);
        message.setRetained(true);
        message.setQos(qos);
        client.publish(topic, message);
        System.out.println(clientId + " Message " + message + " on topic " + topic + " - Thread PID: " + Thread.currentThread().getId());
    }

    @Override
    public void run() {

        try {
            connect();
        } catch (MqttException e) {
            System.err.println("Error connecting to MQTT broker: " + e.getMessage());
            return;
        }

        byte[] payload = new byte[0];

        try {
            publishData(payload);
            client.disconnect();
        } catch (MqttException e) {
            System.err.println("Error publishing data to MQTT broker: " + e.getMessage());
        }

    }
}
