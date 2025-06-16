package ThermalPowerPlant;

import org.eclipse.paho.client.mqttv3.*;

import java.util.TreeSet;

class MeasuresPublisher {
    private MqttClient client;
    private String broker =  "tcp://localhost:1883";
    private String clientId;
    private String topic;
    private int qos = 2;

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


}
