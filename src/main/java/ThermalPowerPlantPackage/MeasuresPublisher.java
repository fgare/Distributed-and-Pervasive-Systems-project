package ThermalPowerPlantPackage;

import org.eclipse.paho.client.mqttv3.*;


class MeasuresPublisher {
    private final MqttClient client;
    private final String broker =  "tcp://localhost:1883";
    private final String clientId;
    private final String topic;
    private final int qos = 2;

    MeasuresPublisher (String topic) throws MqttException {
        clientId = MqttClient.generateClientId();
        client = new MqttClient(broker, clientId);
        this.topic = topic;

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

    void publish (byte[] payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        client.publish(topic, message);
        System.out.println(clientId + " Message " + message + " on topic " + topic + " - Thread PID: " + Thread.currentThread().getId());
    }

}
