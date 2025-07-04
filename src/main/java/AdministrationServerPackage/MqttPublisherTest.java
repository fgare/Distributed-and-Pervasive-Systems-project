package AdministrationServerPackage;

import org.eclipse.paho.client.mqttv3.*;

public class MqttPublisherTest {
    public static void main(String[] args) {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "co2/plant1";
        int qos = 2;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            //Connect the client
            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

            //Callback
            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    //Not used Here
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:" + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    //Until the delivery is completed, messages with QoS 1 or 2 are retained from the client
                    //Delivery for a message is completed when all acknowledgments have been received
                    //When the callback returns from deliveryComplete to the main thread, the client removes the retained messages with QoS 1 or 2.
                    if (token.isComplete()) {
                        System.out.println(clientId + " Message delivered - Thread PID: " + Thread.currentThread().getId());
                    }
                }
            });

            int count = 10;
            while (count-- > 0) {
                //Create a random number between 0 and 10
                String payload = String.valueOf((int) (Math.random() * 100));
                MqttMessage message = new MqttMessage(payload.getBytes());

                //Set the QoS on the Message
                message.setQos(qos);
                //System.out.println(clientId + " Publishing message: " + payload + " ...");
                client.publish(topic, message);
                System.out.println(clientId + " Message " + message + " on topic " + topic + " - Thread PID: " + Thread.currentThread().getId());

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (client.isConnected())
                client.disconnect();
            System.out.println("Publisher " + clientId + " disconnected - Thread PID: " + Thread.currentThread().getId());



        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}
