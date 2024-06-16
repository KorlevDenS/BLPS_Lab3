package org.korolev.dens.blps_lab1.configuration;


import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageHandler;

@Configuration
public class RabbitConfig {

    //TODO move to properties
    private String rabbitmqHost = "tcp://localRabbitMQ:1883";

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{rabbitmqHost});
        options.setUserName("user");
        options.setPassword("password".toCharArray());
        options.setMaxInflight(100);

        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler("javaTestClient", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("queue");
        messageHandler.setDefaultQos(1);
        return messageHandler;
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MQTTGateway {
        void sendToMqtt(String data);
    }

}
