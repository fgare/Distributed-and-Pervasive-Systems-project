package ThermalPowerPlantPackage;

import org.eclipse.paho.client.mqttv3.*;


class EnergyRequestReceiver implements Runnable {
    private MqttClient client;
    private final String topic = "RenewableEnergyRequest";

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
        System.out.println("Mqtt client " + client.getClientId() + " connected to broker - Thread PID: " + Thread.currentThread().getId());

        client.setCallback(new MqttCallback() {
                               @Override
                               public void connectionLost(Throwable cause) {
                                   System.out.println(client.getClientId() + " Connection lost! cause:" + cause.getMessage()+ "- Thread PID: " + Thread.currentThread().getId());
                               }

                               @Override
                               public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                                   //TODO implementare

                                   /*System.out.println(client.getClientId() +" received a Message! - Callback - Thread PID: " + Thread.currentThread().getId() +
                                           "\n\tTime:    " + meas.getTimestamp() +
                                           "\n\tTopic:   " + topic +
                                           "\n\tMessage: " + meas.getValue() +
                                           "\n\tQoS:     " + mqttMessage.getQos() + "\n");*/
                               }

                               @Override
                               public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
                           }
        );

        // iscrizione al topic mqtt
        try {
            client.subscribe(topic , 2);
            System.out.println(client.getClientId() + " Subscribed to topics : " + topic);
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
