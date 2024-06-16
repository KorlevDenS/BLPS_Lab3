package org.korolev.dens.blps_lab1.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.korolev.dens.blps_lab1.configuration.RabbitConfig;
import org.korolev.dens.blps_lab1.requests.StatsMessage;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    private final RabbitConfig.MQTTGateway mqttGateway;

    public MessageProducer(RabbitConfig.MQTTGateway mqttGateway) {
        this.mqttGateway = mqttGateway;
    }

    public void sendMessage(StatsMessage messageObj) {
        ObjectMapper jsonMapper = new ObjectMapper();
        try {
            mqttGateway.sendToMqtt(jsonMapper.writeValueAsString(messageObj));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
